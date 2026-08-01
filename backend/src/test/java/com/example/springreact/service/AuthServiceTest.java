package com.example.springreact.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.example.springreact.model.Role;
import com.example.springreact.model.User;
import com.example.springreact.model.UserLoginHistory;
import com.example.springreact.repository.UserLoginHistoryRepository;
import com.example.springreact.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class AuthServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private UserLoginHistoryRepository loginHistoryRepository;
  @Mock private JwtService jwtService;
  @Mock private PasswordEncoder passwordEncoder;

  @Captor private ArgumentCaptor<User> userCaptor;
  @Captor private ArgumentCaptor<UserLoginHistory> loginHistoryCaptor;

  private AuthService authService;
  private Role adminRole;
  private Role userRole;

  @BeforeEach
  void setUp() {
    authService =
        new AuthService(userRepository, loginHistoryRepository, jwtService, passwordEncoder);
    adminRole = new Role(1L, "ADMIN", "Administrator");
    userRole = new Role(2L, "USER", "User");
    ReflectionTestUtils.setField(authService, "cookieSecure", false);
  }

  // --- LOGIN ---

  @Test
  void login_shouldReturn401WhenEmailNotFound() {
    when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());
    Map<String, Object> result =
        authService.login("unknown@example.com", "pass123", mock(HttpServletRequest.class));
    assertEquals(401, result.get("status"));
    assertEquals("Credenciales inválidas", result.get("message"));
  }

  @Test
  void login_shouldReturn403WhenUserInactive() {
    User inactiveUser = new User("Test", "test@example.com", "encoded", adminRole);
    inactiveUser.setVigencia(false);
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(inactiveUser));
    Map<String, Object> result =
        authService.login("test@example.com", "pass123", mock(HttpServletRequest.class));
    assertEquals(403, result.get("status"));
    assertEquals("Usuario inactivo. Contacte al administrador.", result.get("message"));
  }

  @Test
  void login_shouldReturn401WhenPasswordIncorrect() {
    User user = new User("Test", "test@example.com", "encoded", adminRole);
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("wrongpass", "encoded")).thenReturn(false);
    Map<String, Object> result =
        authService.login("test@example.com", "wrongpass", mock(HttpServletRequest.class));
    assertEquals(401, result.get("status"));
    assertEquals("Credenciales inválidas", result.get("message"));
  }

  @Test
  void login_shouldSucceedWithValidCredentials() {
    User user = new User("Test User", "test@example.com", "encoded", adminRole);
    user.setId(1L);
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteAddr("192.168.1.1");

    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("correctpass", "encoded")).thenReturn(true);
    when(loginHistoryRepository.findTopByUserIdOrderByLoginAtDesc(1L)).thenReturn(Optional.empty());
    when(jwtService.generateAccessToken("test@example.com", 1L, "Test User", "ADMIN"))
        .thenReturn("access-token");
    when(jwtService.generateRefreshToken("test@example.com")).thenReturn("refresh-token");

    Map<String, Object> result = authService.login("test@example.com", "correctpass", request);

    assertNull(result.get("status"));
    assertEquals(1L, result.get("id"));
    assertEquals("Test User", result.get("name"));
    assertEquals("test@example.com", result.get("email"));
    assertEquals("access-token", result.get("accessToken"));
    assertNotNull(result.get("lastLoginAt"));
    assertNotNull(result.get("refreshCookie"));

    verify(loginHistoryRepository).save(loginHistoryCaptor.capture());
    UserLoginHistory saved = loginHistoryCaptor.getValue();
    assertEquals("192.168.1.1", saved.getIpAddress());
  }

  @Test
  void login_shouldReturnPreviousLastLoginAt() {
    User user = new User("Test User", "test@example.com", "encoded", adminRole);
    user.setId(1L);
    LocalDateTime previousLogin = LocalDateTime.of(2025, 1, 1, 10, 0);
    UserLoginHistory history = new UserLoginHistory(user, previousLogin, "10.0.0.1");

    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("correctpass", "encoded")).thenReturn(true);
    when(loginHistoryRepository.findTopByUserIdOrderByLoginAtDesc(1L))
        .thenReturn(Optional.of(history));
    when(jwtService.generateAccessToken(anyString(), any(), anyString(), anyString()))
        .thenReturn("access-token");
    when(jwtService.generateRefreshToken(anyString())).thenReturn("refresh-token");

    Map<String, Object> result =
        authService.login("test@example.com", "correctpass", mock(HttpServletRequest.class));
    assertEquals(previousLogin, result.get("lastLoginAt"));
  }

  // --- REFRESH ---

  @Test
  void refreshToken_shouldReturn401WhenNoCookie() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    Map<String, Object> result = authService.refreshToken(request);
    assertEquals(401, result.get("status"));
    assertEquals("Refresh token requerido", result.get("message"));
  }

  @Test
  void refreshToken_shouldReturn401WhenCookieEmpty() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setCookies(new Cookie("refreshToken", ""));
    Map<String, Object> result = authService.refreshToken(request);
    assertEquals(401, result.get("status"));
  }

  @Test
  void refreshToken_shouldReturn401WhenTokenInvalid() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setCookies(new Cookie("refreshToken", "invalid-token"));
    when(jwtService.isRefreshToken("invalid-token")).thenReturn(false);
    Map<String, Object> result = authService.refreshToken(request);
    assertEquals(401, result.get("status"));
    assertEquals("Refresh token inválido o expirado", result.get("message"));
  }

  @Test
  void refreshToken_shouldReturn401WhenTokenExpired() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setCookies(new Cookie("refreshToken", "expired-token"));
    when(jwtService.isRefreshToken("expired-token")).thenReturn(true);
    when(jwtService.isTokenExpired("expired-token")).thenReturn(true);
    Map<String, Object> result = authService.refreshToken(request);
    assertEquals(401, result.get("status"));
  }

  @Test
  void refreshToken_shouldReturn401WhenUserNotFound() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setCookies(new Cookie("refreshToken", "valid-refresh-token"));
    when(jwtService.isRefreshToken("valid-refresh-token")).thenReturn(true);
    when(jwtService.isTokenExpired("valid-refresh-token")).thenReturn(false);
    when(jwtService.extractEmail("valid-refresh-token")).thenReturn("unknown@example.com");
    when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());
    Map<String, Object> result = authService.refreshToken(request);
    assertEquals(401, result.get("status"));
  }

  @Test
  void refreshToken_shouldReturn403WhenUserInactive() {
    User inactiveUser = new User("User", "user@example.com", "encoded", userRole);
    inactiveUser.setVigencia(false);
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setCookies(new Cookie("refreshToken", "valid-refresh-token"));
    when(jwtService.isRefreshToken("valid-refresh-token")).thenReturn(true);
    when(jwtService.isTokenExpired("valid-refresh-token")).thenReturn(false);
    when(jwtService.extractEmail("valid-refresh-token")).thenReturn("user@example.com");
    when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(inactiveUser));
    Map<String, Object> result = authService.refreshToken(request);
    assertEquals(403, result.get("status"));
  }

  @Test
  void refreshToken_shouldSucceed() {
    User user = new User("User", "user@example.com", "encoded", userRole);
    user.setId(2L);
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setCookies(new Cookie("refreshToken", "valid-refresh-token"));
    when(jwtService.isRefreshToken("valid-refresh-token")).thenReturn(true);
    when(jwtService.isTokenExpired("valid-refresh-token")).thenReturn(false);
    when(jwtService.extractEmail("valid-refresh-token")).thenReturn("user@example.com");
    when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
    when(jwtService.generateAccessToken("user@example.com", 2L, "User", "USER"))
        .thenReturn("new-access-token");
    Map<String, Object> result = authService.refreshToken(request);
    assertEquals("new-access-token", result.get("accessToken"));
    assertEquals("Token renovado", result.get("message"));
    assertNull(result.get("status"));
  }

  // --- CHANGE PASSWORD ---

  @Test
  void changePassword_shouldReturnErrorWhenFieldsMissing() {
    String error = authService.changePassword("test@example.com", "old", null);
    assertEquals("Todos los campos son requeridos", error);
  }

  @Test
  void changePassword_shouldReturnErrorWhenUserNotFound() {
    when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());
    String error = authService.changePassword("unknown@example.com", "old", "NewPass1!");
    assertEquals("Usuario no encontrado", error);
  }

  @Test
  void changePassword_shouldReturnErrorWhenUserInactive() {
    User inactiveUser = new User("User", "user@example.com", "encoded", userRole);
    inactiveUser.setVigencia(false);
    when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(inactiveUser));
    String error = authService.changePassword("user@example.com", "old", "NewPass1!");
    assertEquals("Usuario inactivo. Contacte al administrador.", error);
  }

  @Test
  void changePassword_shouldReturnErrorWhenCurrentPasswordIncorrect() {
    User user = new User("User", "user@example.com", "encoded", userRole);
    when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);
    String error = authService.changePassword("user@example.com", "wrong", "NewPass1!");
    assertEquals("Contraseña actual incorrecta", error);
  }

  @Test
  void changePassword_shouldReturnErrorWhenPasswordDoesNotMeetPolicy() {
    User user = new User("User", "user@example.com", "encoded", userRole);
    when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("correct", "encoded")).thenReturn(true);
    String error = authService.changePassword("user@example.com", "correct", "NoSymbol1");
    assertTrue(error.contains("al menos 8 caracteres"));
  }

  @Test
  void changePassword_shouldSucceed() {
    User user = new User("User", "user@example.com", "encoded", userRole);
    when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("correct", "encoded")).thenReturn(true);
    when(passwordEncoder.encode("NewPass1!")).thenReturn("new-encoded");
    String error = authService.changePassword("user@example.com", "correct", "NewPass1!");
    assertNull(error);
    verify(userRepository).save(userCaptor.capture());
    User saved = userCaptor.getValue();
    assertEquals("new-encoded", saved.getPassword());
    assertFalse(saved.getRequiresPasswordChange());
  }

  // --- CLIENT IP ---

  @Test
  void login_shouldExtractClientIpFromXRealIPHeader() {
    User user = new User("Test", "test@example.com", "encoded", adminRole);
    user.setId(1L);
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("X-Real-IP", "10.10.10.10");

    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("pass", "encoded")).thenReturn(true);
    when(loginHistoryRepository.findTopByUserIdOrderByLoginAtDesc(1L)).thenReturn(Optional.empty());
    when(jwtService.generateAccessToken(anyString(), any(), anyString(), anyString()))
        .thenReturn("access-token");
    when(jwtService.generateRefreshToken(anyString())).thenReturn("refresh-token");

    authService.login("test@example.com", "pass", request);
    verify(loginHistoryRepository).save(loginHistoryCaptor.capture());
    assertEquals("10.10.10.10", loginHistoryCaptor.getValue().getIpAddress());
  }

  @Test
  void login_shouldExtractClientIpFromXForwardedForHeader() {
    User user = new User("Test", "test@example.com", "encoded", adminRole);
    user.setId(1L);
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("X-Forwarded-For", "1.2.3.4, 5.6.7.8");

    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("pass", "encoded")).thenReturn(true);
    when(loginHistoryRepository.findTopByUserIdOrderByLoginAtDesc(1L)).thenReturn(Optional.empty());
    when(jwtService.generateAccessToken(anyString(), any(), anyString(), anyString()))
        .thenReturn("access-token");
    when(jwtService.generateRefreshToken(anyString())).thenReturn("refresh-token");

    authService.login("test@example.com", "pass", request);
    verify(loginHistoryRepository).save(loginHistoryCaptor.capture());
    assertEquals("1.2.3.4", loginHistoryCaptor.getValue().getIpAddress());
  }
}
