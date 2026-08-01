package com.example.springreact.service;

import com.example.springreact.repository.UserRepository;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ProfileService {

  private final UserRepository userRepository;

  public Map<String, Object> getProfile(String email) {
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
              return profile;
            })
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
  }

  public String updateTheme(String email, String theme) {
    if (!"light".equals(theme) && !"dark".equals(theme)) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Tema inválido. Use 'light' o 'dark'");
    }
    return userRepository
        .findByEmail(email)
        .map(
            user -> {
              user.setThemePreference(theme);
              userRepository.save(user);
              return theme;
            })
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
  }
}
