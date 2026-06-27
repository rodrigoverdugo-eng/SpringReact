package com.example.springreact.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.example.springreact.model.Role;
import com.example.springreact.model.User;
import com.example.springreact.model.UserLoginHistory;
import com.example.springreact.repository.UserLoginHistoryRepository;
import com.example.springreact.repository.UserRepository;
import com.example.springreact.security.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

  @Mock private UserRepository userRepository;
  @Mock private UserLoginHistoryRepository loginHistoryRepository;
  @Mock private JwtService jwtService;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private Authentication authentication;
  @Mock private SecurityContext securityContext;

  @Captor private ArgumentCaptor<User> userCaptor;
  @Captor private ArgumentCaptor<UserLoginHistory> loginHistoryCaptor;

  private AuthController controller;
  private Role adminRole;
  private Role userRole;

  @BeforeEach
  void setUp() {
    controller =
        new AuthController(userRepository, loginHistoryRepository, jwtService, passwordEncoder);
    adminRole = new Role(1L, "ADMIN", "Administrator");
    userRole = new Role(2L, "USER", "User");
    SecurityContextHolder.setContext(securityContext);
  }

  // --- LOGIN ---

  @Test
  void login_shouldReturn401WhenEmailNotFound() {
    Map<String, String> credentials = Map.of("email", "unknown@example.com", "password", "pass123");
    when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

    ResponseEntity<?> response =
        controller.login(
            credentials, mock(HttpServletRequest.class), mock(HttpServletResponse.class));

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    assertEquals("Credenciales inválidas", ((Map<?, ?>) response.getBody()).get("message"));
  }

  @Test
  void login_shouldReturn403WhenUserInactive() {
    User inactiveUser = new User("Test", "test@example.com", "encoded", adminRole);
    inactiveUser.setVigencia(false);
    Map<String, String> credentials = Map.of("email", "test@example.com", "password", "pass123");

    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(inactiveUser));

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
    User user = new User("Test", "test@example.com", "encoded", adminRole);
    Map<String, String> credentials = Map.of("email", "test@example.com", "password", "wrongpass");

    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("wrongpass", "encoded")).thenReturn(false);

    ResponseEntity<?> response =
        controller.login(
            credentials, mock(HttpServletRequest.class), mock(HttpServletResponse.class));

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    assertEquals("Credenciales inválidas", ((Map<?, ?>) response.getBody()).get("message"));
  }

  @Test
  void login_shouldSucceedWithValidCredentials() {
    User user = new User("Test User", "test@example.com", "encoded", adminRole);
    user.setId(1L);
    Map<String, String> credentials =
        Map.of("email", "test@example.com", "password", "correctpass");
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteAddr("192.168.1.1");
    MockHttpServletResponse response = new MockHttpServletResponse();

    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("correctpass", "encoded")).thenReturn(true);
    when(loginHistoryRepository.findTopByUserIdOrderByLoginAtDesc(1L)).thenReturn(Optional.empty());
    when(jwtService.generateAccessToken("test@example.com", 1L, "Test User", "ADMIN"))
        .thenReturn("access-token");
    when(jwtService.generateRefreshToken("test@example.com")).thenReturn("refresh-token");

    ResponseEntity<?> result = controller.login(credentials, request, response);

    assertEquals(HttpStatus.OK, result.getStatusCode());
    Map<?, ?> body = (Map<?, ?>) result.getBody();
    assertEquals(1L, body.get("id"));
    assertEquals("Test User", body.get("name"));
    assertEquals("test@example.com", body.get("email"));
    assertEquals("access-token", body.get("accessToken"));
    assertEquals("ADMIN", ((Role) body.get("role")).getName());
    assertNotNull(body.get("lastLoginAt"));

    verify(loginHistoryRepository).save(loginHistoryCaptor.capture());
    UserLoginHistory saved = loginHistoryCaptor.getValue();
    assertEquals("192.168.1.1", saved.getIpAddress());
    assertNotNull(response.getHeader("Set-Cookie"));
    assertTrue(response.getHeader("Set-Cookie").contains("refreshToken"));
  }

  @Test
  void login_shouldReturnPreviousLastLoginAt() {
    User user = new User("Test User", "test@example.com", "encoded", adminRole);
    user.setId(1L);
    LocalDateTime previousLogin = LocalDateTime.of(2025, 1, 1, 10, 0);
    UserLoginHistory history = new UserLoginHistory(user, previousLogin, "10.0.0.1");
    Map<String, String> credentials =
        Map.of("email", "test@example.com", "password", "correctpass");

    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("correctpass", "encoded")).thenReturn(true);
    when(loginHistoryRepository.findTopByUserIdOrderByLoginAtDesc(1L))
        .thenReturn(Optional.of(history));
    when(jwtService.generateAccessToken(anyString(), any(), anyString(), anyString()))
        .thenReturn("access-token");
    when(jwtService.generateRefreshToken(anyString())).thenReturn("refresh-token");

    ResponseEntity<?> result =
        controller.login(
            credentials, mock(HttpServletRequest.class), mock(HttpServletResponse.class));

    Map<?, ?> body = (Map<?, ?>) result.getBody();
    assertEquals(previousLogin, body.get("lastLoginAt"));
  }

  // --- REFRESH ---

  @Test
  void refreshToken_shouldReturn401WhenNoCookie() {
    MockHttpServletRequest request = new MockHttpServletRequest();

    ResponseEntity<?> result = controller.refreshToken(request);

    assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
    assertEquals("Refresh token requerido", ((Map<?, ?>) result.getBody()).get("message"));
  }

  @Test
  void refreshToken_shouldReturn401WhenCookieEmpty() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setCookies(new Cookie("refreshToken", ""));

    ResponseEntity<?> result = controller.refreshToken(request);

    assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
  }

  @Test
  void refreshToken_shouldReturn401WhenTokenInvalid() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setCookies(new Cookie("refreshToken", "invalid-token"));

    when(jwtService.isRefreshToken("invalid-token")).thenReturn(false);

    ResponseEntity<?> result = controller.refreshToken(request);

    assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
    assertEquals(
        "Refresh token inválido o expirado", ((Map<?, ?>) result.getBody()).get("message"));
  }

  @Test
  void refreshToken_shouldReturn401WhenTokenExpired() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setCookies(new Cookie("refreshToken", "expired-token"));

    when(jwtService.isRefreshToken("expired-token")).thenReturn(true);
    when(jwtService.isTokenExpired("expired-token")).thenReturn(true);

    ResponseEntity<?> result = controller.refreshToken(request);

    assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
  }

  @Test
  void refreshToken_shouldReturn401WhenUserNotFound() {
    String refreshTokenValue = "valid-refresh-token";
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setCookies(new Cookie("refreshToken", refreshTokenValue));

    when(jwtService.isRefreshToken(refreshTokenValue)).thenReturn(true);
    when(jwtService.isTokenExpired(refreshTokenValue)).thenReturn(false);
    when(jwtService.extractEmail(refreshTokenValue)).thenReturn("unknown@example.com");
    when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

    ResponseEntity<?> result = controller.refreshToken(request);

    assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
  }

  @Test
  void refreshToken_shouldReturn403WhenUserInactive() {
    String refreshTokenValue = "valid-refresh-token";
    User inactiveUser = new User("User", "user@example.com", "encoded", userRole);
    inactiveUser.setVigencia(false);

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setCookies(new Cookie("refreshToken", refreshTokenValue));

    when(jwtService.isRefreshToken(refreshTokenValue)).thenReturn(true);
    when(jwtService.isTokenExpired(refreshTokenValue)).thenReturn(false);
    when(jwtService.extractEmail(refreshTokenValue)).thenReturn("user@example.com");
    when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(inactiveUser));

    ResponseEntity<?> result = controller.refreshToken(request);

    assertEquals(HttpStatus.FORBIDDEN, result.getStatusCode());
  }

  @Test
  void refreshToken_shouldSucceed() {
    String refreshTokenValue = "valid-refresh-token";
    User user = new User("User", "user@example.com", "encoded", userRole);
    user.setId(2L);

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setCookies(new Cookie("refreshToken", refreshTokenValue));

    when(jwtService.isRefreshToken(refreshTokenValue)).thenReturn(true);
    when(jwtService.isTokenExpired(refreshTokenValue)).thenReturn(false);
    when(jwtService.extractEmail(refreshTokenValue)).thenReturn("user@example.com");
    when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
    when(jwtService.generateAccessToken("user@example.com", 2L, "User", "USER"))
        .thenReturn("new-access-token");

    ResponseEntity<?> result = controller.refreshToken(request);

    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertEquals("new-access-token", ((Map<?, ?>) result.getBody()).get("accessToken"));
  }

  // --- LOGOUT ---

  @Test
  void logout_shouldClearCookie() {
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
  void changePassword_shouldReturn400WhenFieldsMissing() {
    Map<String, String> body = Map.of("currentPassword", "old");
    when(securityContext.getAuthentication()).thenReturn(authentication);

    ResponseEntity<?> result = controller.changePassword(Map.of("currentPassword", "old"));

    assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
  }

  @Test
  void changePassword_shouldReturn404WhenUserNotFound() {
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("unknown@example.com");
    when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

    ResponseEntity<?> result =
        controller.changePassword(Map.of("currentPassword", "old", "newPassword", "new"));

    assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
  }

  @Test
  void changePassword_shouldReturn403WhenUserInactive() {
    User inactiveUser = new User("User", "user@example.com", "encoded", userRole);
    inactiveUser.setVigencia(false);

    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("user@example.com");
    when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(inactiveUser));

    ResponseEntity<?> result =
        controller.changePassword(Map.of("currentPassword", "old", "newPassword", "new"));

    assertEquals(HttpStatus.FORBIDDEN, result.getStatusCode());
  }

  @Test
  void changePassword_shouldReturn401WhenCurrentPasswordIncorrect() {
    User user = new User("User", "user@example.com", "encoded", userRole);

    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("user@example.com");
    when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

    ResponseEntity<?> result =
        controller.changePassword(Map.of("currentPassword", "wrong", "newPassword", "new"));

    assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
  }

  @Test
  void changePassword_shouldSucceed() {
    User user = new User("User", "user@example.com", "encoded", userRole);

    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("user@example.com");
    when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("correct", "encoded")).thenReturn(true);
    when(passwordEncoder.encode("newPass")).thenReturn("new-encoded");

    ResponseEntity<?> result =
        controller.changePassword(Map.of("currentPassword", "correct", "newPassword", "newPass"));

    assertEquals(HttpStatus.OK, result.getStatusCode());
    verify(userRepository).save(userCaptor.capture());
    User saved = userCaptor.getValue();
    assertEquals("new-encoded", saved.getPassword());
    assertFalse(saved.getRequiresPasswordChange());
  }
}
