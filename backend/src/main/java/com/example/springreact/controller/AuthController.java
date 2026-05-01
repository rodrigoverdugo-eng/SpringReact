package com.example.springreact.controller;

import com.example.springreact.model.User;
import com.example.springreact.model.UserLoginHistory;
import com.example.springreact.repository.UserLoginHistoryRepository;
import com.example.springreact.repository.UserRepository;
import com.example.springreact.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final UserRepository userRepository;
  private final UserLoginHistoryRepository loginHistoryRepository;
  private final JwtService jwtService;
  private final PasswordEncoder passwordEncoder;

  // Login
  @PostMapping("/login")
  public ResponseEntity<?> login(
      @RequestBody @NonNull Map<String, String> credentials, HttpServletRequest request) {
    String email = credentials.get("email");
    String password = credentials.get("password");

    Optional<User> userOpt = userRepository.findByEmail(email);

    if (userOpt.isEmpty()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Map.of("message", "Credenciales inválidas"));
    }

    User user = userOpt.get();

    // Verificar si el usuario está activo
    if (Boolean.FALSE.equals(user.getVigencia())) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(Map.of("message", "Usuario inactivo. Contacte al administrador."));
    }

    // Verificar password con BCrypt
    if (!passwordEncoder.matches(password, user.getPassword())) {
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

    // Retornar datos del usuario con tokens
    Map<String, Object> response = new HashMap<>();
    response.put("id", user.getId());
    response.put("name", user.getName());
    response.put("email", user.getEmail());
    response.put("role", user.getRole());
    response.put("accessToken", accessToken);
    response.put("refreshToken", refreshToken);
    response.put("requiresPasswordChange", user.getRequiresPasswordChange());
    response.put("vigencia", user.getVigencia());
    response.put("lastLoginAt", lastLoginAt);
    response.put("message", "Login exitoso");

    return ResponseEntity.ok(response);
  }

  // Refresh token
  @PostMapping("/refresh")
  public ResponseEntity<?> refreshToken(@RequestBody @NonNull Map<String, String> request) {
    String refreshToken = request.get("refreshToken");

    if (refreshToken == null || refreshToken.isEmpty()) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
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

      Map<String, Object> response = new HashMap<>();
      response.put("accessToken", newAccessToken);
      response.put("message", "Token renovado");

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Map.of("message", "Error al renovar token"));
    }
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

    return ResponseEntity.ok(Map.of("message", "Contraseña actualizada exitosamente"));
  }

  private String getClientIp(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isBlank()) {
      return forwarded.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }
}
