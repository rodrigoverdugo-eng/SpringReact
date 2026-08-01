package com.example.springreact.service;

import com.example.springreact.model.User;
import com.example.springreact.model.UserLoginHistory;
import com.example.springreact.repository.UserLoginHistoryRepository;
import com.example.springreact.repository.UserRepository;
import com.example.springreact.util.PasswordValidator;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final UserLoginHistoryRepository loginHistoryRepository;
  private final JwtService jwtService;
  private final PasswordEncoder passwordEncoder;

  @Value("${app.security.cookie.secure:false}")
  private boolean cookieSecure;

  public Map<String, Object> login(String email, String password, HttpServletRequest request) {
    Optional<User> userOpt = userRepository.findByEmail(email);

    if (userOpt.isEmpty()) {
      MDC.put("event", "LOGIN_FAILED");
      MDC.put("reason", "user_not_found");
      MDC.put("email", email);
      MDC.put("client_ip", getClientIp(request));
      log.warn("Intento de login con email no registrado");
      MDC.clear();
      Map<String, Object> error = new HashMap<>();
      error.put("status", 401);
      error.put("message", "Credenciales inválidas");
      return error;
    }

    User user = userOpt.get();

    if (Boolean.FALSE.equals(user.getVigencia())) {
      MDC.put("event", "LOGIN_FAILED");
      MDC.put("reason", "user_inactive");
      MDC.put("email", email);
      MDC.put("client_ip", getClientIp(request));
      log.warn("Intento de login con usuario inactivo");
      MDC.clear();
      Map<String, Object> error = new HashMap<>();
      error.put("status", 403);
      error.put("message", "Usuario inactivo. Contacte al administrador.");
      return error;
    }

    if (!passwordEncoder.matches(password, user.getPassword())) {
      MDC.put("event", "LOGIN_FAILED");
      MDC.put("reason", "bad_credentials");
      MDC.put("email", email);
      MDC.put("client_ip", getClientIp(request));
      log.warn("Intento de login con contraseña incorrecta");
      MDC.clear();
      Map<String, Object> error = new HashMap<>();
      error.put("status", 401);
      error.put("message", "Credenciales inválidas");
      return error;
    }

    LocalDateTime now = LocalDateTime.now();
    Optional<UserLoginHistory> previousLogin =
        loginHistoryRepository.findTopByUserIdOrderByLoginAtDesc(user.getId());
    LocalDateTime lastLoginAt = previousLogin.map(h -> h.getLoginAt()).orElse(now);

    String ipAddress = getClientIp(request);
    loginHistoryRepository.save(new UserLoginHistory(user, now, ipAddress));

    String accessToken =
        jwtService.generateAccessToken(
            user.getEmail(), user.getId(), user.getName(), user.getRole().getName());
    String refreshToken = jwtService.generateRefreshToken(user.getEmail());

    MDC.put("event", "LOGIN_SUCCESS");
    MDC.put("email", user.getEmail());
    MDC.put("role", user.getRole().getName());
    MDC.put("client_ip", ipAddress);
    log.info("Login exitoso");
    MDC.clear();

    Map<String, Object> responseBody = new HashMap<>();
    responseBody.put("id", user.getId());
    responseBody.put("name", user.getName());
    responseBody.put("email", user.getEmail());
    responseBody.put("role", user.getRole());
    responseBody.put("accessToken", accessToken);
    responseBody.put("requiresPasswordChange", user.getRequiresPasswordChange());
    responseBody.put("vigencia", user.getVigencia());
    responseBody.put("lastLoginAt", lastLoginAt);
    responseBody.put("themePreference", user.getThemePreference());
    responseBody.put("message", "Login exitoso");
    responseBody.put("refreshCookie", createRefreshCookie(refreshToken).toString());
    return responseBody;
  }

  public Map<String, Object> refreshToken(HttpServletRequest request) {
    String refreshToken = null;
    if (request.getCookies() != null) {
      for (Cookie cookie : request.getCookies()) {
        if ("refreshToken".equals(cookie.getName())) {
          refreshToken = cookie.getValue();
          break;
        }
      }
    }

    if (refreshToken == null || refreshToken.isEmpty()) {
      Map<String, Object> error = new HashMap<>();
      error.put("status", 401);
      error.put("message", "Refresh token requerido");
      return error;
    }

    try {
      if (!jwtService.isRefreshToken(refreshToken) || jwtService.isTokenExpired(refreshToken)) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", 401);
        error.put("message", "Refresh token inválido o expirado");
        return error;
      }

      String email = jwtService.extractEmail(refreshToken);
      Optional<User> userOpt = userRepository.findByEmail(email);

      if (userOpt.isEmpty()) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", 401);
        error.put("message", "Usuario no encontrado");
        return error;
      }

      User user = userOpt.get();

      if (Boolean.FALSE.equals(user.getVigencia())) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", 403);
        error.put("message", "Usuario inactivo. Contacte al administrador.");
        return error;
      }

      String newAccessToken =
          jwtService.generateAccessToken(
              user.getEmail(), user.getId(), user.getName(), user.getRole().getName());

      Map<String, Object> response = new HashMap<>();
      response.put("accessToken", newAccessToken);
      response.put("message", "Token renovado");
      return response;

    } catch (Exception e) {
      Map<String, Object> error = new HashMap<>();
      error.put("status", 401);
      error.put("message", "Error al renovar token");
      return error;
    }
  }

  public ResponseCookie createLogoutCookie() {
    return ResponseCookie.from("refreshToken", "")
        .httpOnly(true)
        .secure(cookieSecure)
        .sameSite("Strict")
        .path("/api/auth/refresh")
        .maxAge(0)
        .build();
  }

  public String changePassword(String email, String currentPassword, String newPassword) {
    if (currentPassword == null || newPassword == null) {
      return "Todos los campos son requeridos";
    }

    Optional<User> userOpt = userRepository.findByEmail(email);
    if (userOpt.isEmpty()) {
      return "Usuario no encontrado";
    }

    User user = userOpt.get();

    if (Boolean.FALSE.equals(user.getVigencia())) {
      return "Usuario inactivo. Contacte al administrador.";
    }

    if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
      return "Contraseña actual incorrecta";
    }

    if (!PasswordValidator.isValid(newPassword)) {
      return PasswordValidator.ERROR_MESSAGE;
    }

    user.setPassword(passwordEncoder.encode(newPassword));
    user.setRequiresPasswordChange(false);
    userRepository.save(user);

    MDC.put("event", "PASSWORD_CHANGED");
    MDC.put("email", email);
    log.info("Contraseña actualizada");
    MDC.clear();

    return null;
  }

  private ResponseCookie createRefreshCookie(String refreshToken) {
    return ResponseCookie.from("refreshToken", refreshToken != null ? refreshToken : "")
        .httpOnly(true)
        .secure(cookieSecure)
        .sameSite("Strict")
        .path("/api/auth/refresh")
        .maxAge(7 * 24 * 60 * 60)
        .build();
  }

  private String getClientIp(HttpServletRequest request) {
    String realIp = request.getHeader("X-Real-IP");
    if (realIp != null && !realIp.isBlank()) {
      return realIp.trim();
    }
    String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isBlank()) {
      return forwarded.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }
}
