package com.example.springreact.controller;

import com.example.springreact.repository.UserRepository;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

  private final UserRepository userRepository;

  @GetMapping
  public ResponseEntity<?> getProfile() {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();

    return userRepository
        .findByEmail(email)
        .map(
            user -> {
              Map<String, Object> profile = new HashMap<>();
              profile.put("id", user.getId());
              profile.put("name", user.getName());
              profile.put("email", user.getEmail());
              profile.put("role", user.getRole());
              profile.put("themePreference", user.getThemePreference());
              profile.put("vigencia", user.getVigencia());
              return ResponseEntity.ok(profile);
            })
        .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }

  @PutMapping("/theme")
  public ResponseEntity<?> updateTheme(@RequestBody @NonNull Map<String, String> body) {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    String theme = body.get("theme");

    if (!"light".equals(theme) && !"dark".equals(theme)) {
      return ResponseEntity.badRequest()
          .body(Map.of("message", "Tema inválido. Use 'light' o 'dark'"));
    }

    return userRepository
        .findByEmail(email)
        .map(
            user -> {
              user.setThemePreference(theme);
              userRepository.save(user);
              return ResponseEntity.ok(
                  Map.of("message", "Preferencia actualizada", "themePreference", theme));
            })
        .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }
}
