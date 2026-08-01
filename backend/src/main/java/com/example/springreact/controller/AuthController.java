package com.example.springreact.controller;

import com.example.springreact.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/login")
  public ResponseEntity<?> login(
      @RequestBody @NonNull Map<String, String> credentials,
      HttpServletRequest request,
      HttpServletResponse response) {
    String email = credentials.get("email");
    String password = credentials.get("password");

    Map<String, Object> result = authService.login(email, password, request);

    if (result.containsKey("status")) {
      int status = (int) result.get("status");
      return ResponseEntity.status(status).body(Map.of("message", result.get("message")));
    }

    String refreshCookie = (String) result.remove("refreshCookie");
    if (refreshCookie != null) {
      response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie);
    }

    return ResponseEntity.ok(result);
  }

  @PostMapping("/refresh")
  public ResponseEntity<?> refreshToken(HttpServletRequest request) {
    Map<String, Object> result = authService.refreshToken(request);

    if (result.containsKey("status")) {
      int status = (int) result.get("status");
      return ResponseEntity.status(status).body(Map.of("message", result.get("message")));
    }

    return ResponseEntity.ok(result);
  }

  @PostMapping("/logout")
  public ResponseEntity<?> logout(HttpServletResponse response) {
    response.addHeader(HttpHeaders.SET_COOKIE, authService.createLogoutCookie().toString());
    return ResponseEntity.ok(Map.of("message", "Logout exitoso"));
  }

  @PostMapping("/change-password")
  public ResponseEntity<?> changePassword(@RequestBody @NonNull Map<String, String> request) {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    String currentPassword = request.get("currentPassword");
    String newPassword = request.get("newPassword");

    String error = authService.changePassword(email, currentPassword, newPassword);

    if (error == null) {
      return ResponseEntity.ok(Map.of("message", "Contraseña actualizada exitosamente"));
    }

    HttpStatus status = HttpStatus.BAD_REQUEST;
    if ("Contraseña actual incorrecta".equals(error)) {
      status = HttpStatus.UNAUTHORIZED;
    } else if ("Usuario inactivo. Contacte al administrador.".equals(error)) {
      status = HttpStatus.FORBIDDEN;
    } else if ("Usuario no encontrado".equals(error)) {
      status = HttpStatus.NOT_FOUND;
    }

    return ResponseEntity.status(status).body(Map.of("message", error));
  }
}
