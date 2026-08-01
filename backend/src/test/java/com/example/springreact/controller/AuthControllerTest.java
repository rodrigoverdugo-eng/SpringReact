package com.example.springreact.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.springreact.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class AuthControllerTest {

  @Mock private AuthService authService;
  @Mock private Authentication authentication;
  @Mock private SecurityContext securityContext;

  private AuthController controller;

  @BeforeEach
  void setUp() {
    controller = new AuthController(authService);
    SecurityContextHolder.setContext(securityContext);
  }

  // --- LOGIN ---

  @Test
  void login_shouldReturn401WhenEmailNotFound() {
    Map<String, Object> serviceResult = new HashMap<>();
    serviceResult.put("status", 401);
    serviceResult.put("message", "Credenciales inválidas");

    when(authService.login(eq("unknown@example.com"), eq("pass123"), any()))
        .thenReturn(serviceResult);

    Map<String, String> credentials = Map.of("email", "unknown@example.com", "password", "pass123");
    ResponseEntity<?> response =
        controller.login(
            credentials, mock(HttpServletRequest.class), mock(HttpServletResponse.class));

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    assertEquals("Credenciales inválidas", ((Map<?, ?>) response.getBody()).get("message"));
  }

  @Test
  void login_shouldReturn403WhenUserInactive() {
    Map<String, Object> serviceResult = new HashMap<>();
    serviceResult.put("status", 403);
    serviceResult.put("message", "Usuario inactivo. Contacte al administrador.");

    when(authService.login(eq("test@example.com"), eq("pass123"), any())).thenReturn(serviceResult);

    Map<String, String> credentials = Map.of("email", "test@example.com", "password", "pass123");
    ResponseEntity<?> response =
        controller.login(
            credentials, mock(HttpServletRequest.class), mock(HttpServletResponse.class));

    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    assertEquals(
        "Usuario inactivo. Contacte al administrador.",
        ((Map<?, ?>) response.getBody()).get("message"));
  }

  @Test
  void login_shouldReturn401WhenPasswordIncorrect() {
    Map<String, Object> serviceResult = new HashMap<>();
    serviceResult.put("status", 401);
    serviceResult.put("message", "Credenciales inválidas");

    when(authService.login(eq("test@example.com"), eq("wrongpass"), any()))
        .thenReturn(serviceResult);

    Map<String, String> credentials = Map.of("email", "test@example.com", "password", "wrongpass");
    ResponseEntity<?> response =
        controller.login(
            credentials, mock(HttpServletRequest.class), mock(HttpServletResponse.class));

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    assertEquals("Credenciales inválidas", ((Map<?, ?>) response.getBody()).get("message"));
  }

  @Test
  void login_shouldSucceedWithValidCredentials() {
    Map<String, Object> serviceResult = new HashMap<>();
    serviceResult.put("id", 1L);
    serviceResult.put("name", "Test User");
    serviceResult.put("email", "test@example.com");
    serviceResult.put("accessToken", "access-token");
    serviceResult.put(
        "refreshCookie", "refreshToken=abc; Path=/api/auth/refresh; HttpOnly; SameSite=Strict");
    serviceResult.put("lastLoginAt", LocalDateTime.now());

    when(authService.login(eq("test@example.com"), eq("correctpass"), any()))
        .thenReturn(serviceResult);

    Map<String, String> credentials =
        Map.of("email", "test@example.com", "password", "correctpass");
    MockHttpServletResponse response = new MockHttpServletResponse();
    ResponseEntity<?> result =
        controller.login(credentials, mock(HttpServletRequest.class), response);

    assertEquals(HttpStatus.OK, result.getStatusCode());
    Map<?, ?> body = (Map<?, ?>) result.getBody();
    assertEquals(1L, body.get("id"));
    assertEquals("Test User", body.get("name"));
    assertEquals("access-token", body.get("accessToken"));
    assertNull(body.get("refreshCookie"));
    assertNotNull(response.getHeader("Set-Cookie"));
  }

  @Test
  void login_shouldReturnPreviousLastLoginAt() {
    LocalDateTime previousLogin = LocalDateTime.of(2025, 1, 1, 10, 0);
    Map<String, Object> serviceResult = new HashMap<>();
    serviceResult.put("id", 1L);
    serviceResult.put("lastLoginAt", previousLogin);
    serviceResult.put("refreshCookie", "refreshToken=abc; HttpOnly");

    when(authService.login(eq("test@example.com"), eq("correctpass"), any()))
        .thenReturn(serviceResult);

    ResponseEntity<?> result =
        controller.login(
            Map.of("email", "test@example.com", "password", "correctpass"),
            mock(HttpServletRequest.class),
            mock(HttpServletResponse.class));

    Map<?, ?> body = (Map<?, ?>) result.getBody();
    assertEquals(previousLogin, body.get("lastLoginAt"));
  }

  // --- REFRESH ---

  @Test
  void refreshToken_shouldReturn401WhenNoCookie() {
    Map<String, Object> serviceResult = new HashMap<>();
    serviceResult.put("status", 401);
    serviceResult.put("message", "Refresh token requerido");

    when(authService.refreshToken(any())).thenReturn(serviceResult);

    ResponseEntity<?> result = controller.refreshToken(new MockHttpServletRequest());
    assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
    assertEquals("Refresh token requerido", ((Map<?, ?>) result.getBody()).get("message"));
  }

  @Test
  void refreshToken_shouldSucceed() {
    Map<String, Object> serviceResult = new HashMap<>();
    serviceResult.put("accessToken", "new-access-token");
    serviceResult.put("message", "Token renovado");

    when(authService.refreshToken(any())).thenReturn(serviceResult);

    MockHttpServletRequest request = new MockHttpServletRequest();
    ResponseEntity<?> result = controller.refreshToken(request);
    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertEquals("new-access-token", ((Map<?, ?>) result.getBody()).get("accessToken"));
  }

  // --- LOGOUT ---

  @Test
  void logout_shouldClearCookie() {
    ResponseCookie cookie =
        ResponseCookie.from("refreshToken", "")
            .httpOnly(true)
            .maxAge(0)
            .path("/api/auth/refresh")
            .build();
    when(authService.createLogoutCookie()).thenReturn(cookie);

    MockHttpServletResponse response = new MockHttpServletResponse();
    ResponseEntity<?> result = controller.logout(response);

    assertEquals(HttpStatus.OK, result.getStatusCode());
    String setCookie = response.getHeader("Set-Cookie");
    assertNotNull(setCookie);
    assertTrue(setCookie.contains("refreshToken="));
    assertTrue(setCookie.contains("Max-Age=0"));
  }

  // --- CHANGE PASSWORD ---

  @Test
  void changePassword_shouldReturn200WhenSuccessful() {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("user@example.com");
    when(authService.changePassword("user@example.com", "correct", "NewPass1!")).thenReturn(null);

    ResponseEntity<?> result =
        controller.changePassword(Map.of("currentPassword", "correct", "newPassword", "NewPass1!"));

    assertEquals(HttpStatus.OK, result.getStatusCode());
  }

  @Test
  void changePassword_shouldReturn400WhenPasswordDoesNotMeetPolicy() {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("user@example.com");
    when(authService.changePassword("user@example.com", "correct", "NoSymbol1"))
        .thenReturn(
            "La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula, un número y un símbolo");

    ResponseEntity<?> result =
        controller.changePassword(Map.of("currentPassword", "correct", "newPassword", "NoSymbol1"));

    assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
  }

  @Test
  void changePassword_shouldReturn401WhenCurrentPasswordIncorrect() {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("user@example.com");
    when(authService.changePassword("user@example.com", "wrong", "NewPass1!"))
        .thenReturn("Contraseña actual incorrecta");

    ResponseEntity<?> result =
        controller.changePassword(Map.of("currentPassword", "wrong", "newPassword", "NewPass1!"));

    assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
  }

  @Test
  void changePassword_shouldReturn400WhenFieldsMissing() {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("user@example.com");
    when(authService.changePassword("user@example.com", "old", null))
        .thenReturn("Todos los campos son requeridos");

    ResponseEntity<?> result = controller.changePassword(Map.of("currentPassword", "old"));
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
  }

  @Test
  void changePassword_shouldReturn404WhenUserNotFound() {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("unknown@example.com");
    when(authService.changePassword("unknown@example.com", "old", "NewPass1!"))
        .thenReturn("Usuario no encontrado");

    ResponseEntity<?> result =
        controller.changePassword(Map.of("currentPassword", "old", "newPassword", "NewPass1!"));
    assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
  }

  @Test
  void changePassword_shouldReturn403WhenUserInactive() {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("user@example.com");
    when(authService.changePassword("user@example.com", "old", "NewPass1!"))
        .thenReturn("Usuario inactivo. Contacte al administrador.");

    ResponseEntity<?> result =
        controller.changePassword(Map.of("currentPassword", "old", "newPassword", "NewPass1!"));
    assertEquals(HttpStatus.FORBIDDEN, result.getStatusCode());
  }

  @Test
  void changePassword_shouldReturn400WhenPasswordTooShort() {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("user@example.com");
    when(authService.changePassword("user@example.com", "correct", "Aa1!"))
        .thenReturn(
            "La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula, un número y un símbolo");

    ResponseEntity<?> result =
        controller.changePassword(Map.of("currentPassword", "correct", "newPassword", "Aa1!"));
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
  }

  @Test
  void changePassword_shouldReturn400WhenPasswordMissingUppercase() {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("user@example.com");
    when(authService.changePassword("user@example.com", "correct", "noupperr1!"))
        .thenReturn(
            "La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula, un número y un símbolo");

    ResponseEntity<?> result =
        controller.changePassword(
            Map.of("currentPassword", "correct", "newPassword", "noupperr1!"));
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
  }

  @Test
  void changePassword_shouldReturn400WhenPasswordMissingLowercase() {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("user@example.com");
    when(authService.changePassword("user@example.com", "correct", "NOLOWER1!"))
        .thenReturn(
            "La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula, un número y un símbolo");

    ResponseEntity<?> result =
        controller.changePassword(Map.of("currentPassword", "correct", "newPassword", "NOLOWER1!"));
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
  }

  @Test
  void changePassword_shouldReturn400WhenPasswordMissingDigit() {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("user@example.com");
    when(authService.changePassword("user@example.com", "correct", "NoDigit!!"))
        .thenReturn(
            "La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula, un número y un símbolo");

    ResponseEntity<?> result =
        controller.changePassword(Map.of("currentPassword", "correct", "newPassword", "NoDigit!!"));
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
  }
}
