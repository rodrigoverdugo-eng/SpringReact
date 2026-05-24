package com.example.springreact.controller;

import com.example.springreact.model.User;
import com.example.springreact.model.UserLoginHistory;
import com.example.springreact.repository.UserLoginHistoryRepository;
import com.example.springreact.repository.UserRepository;
import com.example.springreact.security.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final UserRepository userRepository;
  private final UserLoginHistoryRepository loginHistoryRepository;
  private final JwtService jwtService;
  private final PasswordEncoder passwordEncoder;

  @Value("${app.security.cookie.secure:false}")
  private boolean cookieSecure;

  // Login
  @PostMapping("/login")
  public ResponseEntity<?> login(
      @RequestBody @NonNull Map<String, String> credentials,
      HttpServletRequest request,
      HttpServletResponse response) {
    String email = credentials.get("email");
    String password = credentials.get("password");

    Optional<User> userOpt = userRepository.findByEmail(email);

    if (userOpt.isEmpty()) {
      MDC.put("event", "LOGIN_FAILED");
      MDC.put("reason", "user_not_found");
      MDC.put("email", email);
      MDC.put("client_ip", getClientIp(request));
      log.warn("Intento de login con email no registrado");
      MDC.clear();
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Map.of("message", "Credenciales inválidas"));
    }

    User user = userOpt.get();

    // Verificar si el usuario está activo
    if (Boolean.FALSE.equals(user.getVigencia())) {
      MDC.put("event", "LOGIN_FAILED");
      MDC.put("reason", "user_inactive");
      MDC.put("email", email);
      MDC.put("client_ip", getClientIp(request));
      log.warn("Intento de login con usuario inactivo");
      MDC.clear();
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(Map.of("message", "Usuario inactivo. Contacte al administrador."));
    }

    // Verificar password con BCrypt
    if (!passwordEncoder.matches(password, user.getPassword())) {
      MDC.put("event", "LOGIN_FAILED");
      MDC.put("reason", "bad_credentials");
      MDC.put("email", email);
      MDC.put("client_ip", getClientIp(request));
      log.warn("Intento de login con contraseña incorrecta");
      MDC.clear();
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Map.of("message", "Credenciales inválidas"));
    }

    // Obtener el último acceso previo antes de registrar el nuevo
    LocalDateTime now = LocalDateTime.now();
    Optional<UserLoginHistory> previousLogin =
        loginHistoryRepository.findTopByUserIdOrderByLoginAtDesc(user.getId());
    LocalDateTime lastLoginAt = previousLogin.map(UserLoginHistory::getLoginAt).orElse(now);

    // Registrar el nuevo login
    String ipAddress = getClientIp(request);
    loginHistoryRepository.save(new UserLoginHistory(user, now, ipAddress));

    // Generar tokens JWT con el rol incluido
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

    // Retornar datos del usuario con tokens
    Map<String, Object> responseBody = new HashMap<>();
    responseBody.put("id", user.getId());
    responseBody.put("name", user.getName());
    responseBody.put("email", user.getEmail());
    responseBody.put("role", user.getRole());
    responseBody.put("accessToken", accessToken);
    responseBody.put("requiresPasswordChange", user.getRequiresPasswordChange());
    responseBody.put("vigencia", user.getVigencia());
    responseBody.put("lastLoginAt", lastLoginAt);
    responseBody.put("message", "Login exitoso");

    // Enviar refreshToken como cookie httpOnly (no expuesto a JS)
    ResponseCookie refreshCookie =
        ResponseCookie.from("refreshToken", refreshToken != null ? refreshToken : "")
            .httpOnly(true)
            .secure(cookieSecure)
            .sameSite("Strict")
            .path("/api/auth/refresh")
            .maxAge(7 * 24 * 60 * 60)
            .build();
    response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

    return ResponseEntity.ok(responseBody);
  }

  // Refresh token
  @PostMapping("/refresh")
  public ResponseEntity<?> refreshToken(HttpServletRequest request) {
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
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Map.of("message", "Refresh token requerido"));
    }

    try {
      // Validar que sea un refresh token y no esté expirado
      if (!jwtService.isRefreshToken(refreshToken) || jwtService.isTokenExpired(refreshToken)) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("message", "Refresh token inválido o expirado"));
      }

      // Extraer email del token
      String email = jwtService.extractEmail(refreshToken);

      // Buscar usuario
      Optional<User> userOpt = userRepository.findByEmail(email);
      if (userOpt.isEmpty()) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("message", "Usuario no encontrado"));
      }

      User user = userOpt.get();

      // Verificar si el usuario está activo
      if (Boolean.FALSE.equals(user.getVigencia())) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(Map.of("message", "Usuario inactivo. Contacte al administrador."));
      }

      // Generar nuevo access token (incluyendo el rol)
      String newAccessToken =
          jwtService.generateAccessToken(
              user.getEmail(), user.getId(), user.getName(), user.getRole().getName());

      Map<String, Object> refreshResponse = new HashMap<>();
      refreshResponse.put("accessToken", newAccessToken);
      refreshResponse.put("message", "Token renovado");

      return ResponseEntity.ok(refreshResponse);

    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Map.of("message", "Error al renovar token"));
    }
  }

  // Logout
  @PostMapping("/logout")
  public ResponseEntity<?> logout(HttpServletResponse response) {
    ResponseCookie clearCookie =
        ResponseCookie.from("refreshToken", "")
            .httpOnly(true)
            .secure(cookieSecure)
            .sameSite("Strict")
            .path("/api/auth/refresh")
            .maxAge(0)
            .build();
    response.addHeader(HttpHeaders.SET_COOKIE, clearCookie.toString());
    return ResponseEntity.ok(Map.of("message", "Logout exitoso"));
  }

  // Cambiar contraseña
  @PostMapping("/change-password")
  public ResponseEntity<?> changePassword(@RequestBody @NonNull Map<String, String> request) {
    String email = request.get("email");
    String currentPassword = request.get("currentPassword");
    String newPassword = request.get("newPassword");

    if (email == null || currentPassword == null || newPassword == null) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(Map.of("message", "Todos los campos son requeridos"));
    }

    Optional<User> userOpt = userRepository.findByEmail(email);

    if (userOpt.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(Map.of("message", "Usuario no encontrado"));
    }

    User user = userOpt.get();

    // Verificar si el usuario está activo
    if (Boolean.FALSE.equals(user.getVigencia())) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(Map.of("message", "Usuario inactivo. Contacte al administrador."));
    }

    // Verificar contraseña actual
    if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Map.of("message", "Contraseña actual incorrecta"));
    }

    // Actualizar contraseña y marcar que ya no requiere cambio
    user.setPassword(passwordEncoder.encode(newPassword));
    user.setRequiresPasswordChange(false);
    userRepository.save(user);

    MDC.put("event", "PASSWORD_CHANGED");
    MDC.put("email", email);
    log.info("Contraseña actualizada");
    MDC.clear();

    return ResponseEntity.ok(Map.of("message", "Contraseña actualizada exitosamente"));
  }

  private String getClientIp(HttpServletRequest request) {
    // X-Real-IP: set by Nginx/Railway with the original client IP
    String realIp = request.getHeader("X-Real-IP");
    if (realIp != null && !realIp.isBlank()) {
      return realIp.trim();
    }
    // X-Forwarded-For: "client, proxy1, proxy2" — first entry is the original client
    String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isBlank()) {
      return forwarded.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }
}
