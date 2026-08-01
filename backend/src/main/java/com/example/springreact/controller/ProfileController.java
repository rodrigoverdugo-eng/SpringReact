package com.example.springreact.controller;

import com.example.springreact.service.ProfileService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

  private final ProfileService profileService;

  @GetMapping
  public ResponseEntity<?> getProfile() {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    return ResponseEntity.ok(profileService.getProfile(email));
  }

  @PutMapping("/theme")
  public ResponseEntity<?> updateTheme(@RequestBody @NonNull Map<String, String> body) {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    String theme = profileService.updateTheme(email, body.get("theme"));
    return ResponseEntity.ok(
        Map.of("message", "Preferencia actualizada", "themePreference", theme));
  }
}
