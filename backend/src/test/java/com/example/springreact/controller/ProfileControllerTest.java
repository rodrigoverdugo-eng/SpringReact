package com.example.springreact.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.springreact.model.Role;
import com.example.springreact.model.User;
import com.example.springreact.service.ProfileService;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"null", "unchecked"})
class ProfileControllerTest {

  @Mock private ProfileService profileService;
  @Mock private Authentication authentication;
  @Mock private SecurityContext securityContext;

  private ProfileController controller;
  private User testUser;

  @BeforeEach
  void setUp() {
    controller = new ProfileController(profileService);
    Role role = new Role(1L, "ADMIN", "Administrator");
    testUser = new User("Test User", "test@example.com", "encoded", role);
    testUser.setId(1L);
    testUser.setThemePreference("light");
    SecurityContextHolder.setContext(securityContext);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("test@example.com");
  }

  @Test
  void getProfile_shouldReturn200WithProfileDataWhenUserFound() {
    Map<String, Object> profile = new HashMap<>();
    profile.put("id", 1L);
    profile.put("name", "Test User");
    profile.put("email", "test@example.com");
    profile.put("themePreference", "light");

    when(profileService.getProfile("test@example.com")).thenReturn(profile);

    ResponseEntity<?> result = controller.getProfile();

    assertEquals(HttpStatus.OK, result.getStatusCode());
    Map<String, Object> body = (Map<String, Object>) result.getBody();
    assertNotNull(body);
    assertEquals(1L, body.get("id"));
    assertEquals("Test User", body.get("name"));
    assertEquals("test@example.com", body.get("email"));
    assertEquals("light", body.get("themePreference"));
  }

  @Test
  void getProfile_shouldPropagate404WhenUserNotFound() {
    when(profileService.getProfile("test@example.com"))
        .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

    assertThrows(ResponseStatusException.class, () -> controller.getProfile());
  }

  @Test
  void updateTheme_shouldReturn200WhenThemeIsDark() {
    when(profileService.updateTheme("test@example.com", "dark")).thenReturn("dark");

    ResponseEntity<?> result = controller.updateTheme(Map.of("theme", "dark"));

    assertEquals(HttpStatus.OK, result.getStatusCode());
    Map<String, Object> body = (Map<String, Object>) result.getBody();
    assertNotNull(body);
    assertEquals("dark", body.get("themePreference"));
    assertEquals("Preferencia actualizada", body.get("message"));
  }

  @Test
  void updateTheme_shouldReturn200WhenThemeIsLight() {
    when(profileService.updateTheme("test@example.com", "light")).thenReturn("light");

    ResponseEntity<?> result = controller.updateTheme(Map.of("theme", "light"));

    assertEquals(HttpStatus.OK, result.getStatusCode());
    Map<String, Object> body = (Map<String, Object>) result.getBody();
    assertEquals("light", body.get("themePreference"));
  }

  @Test
  void updateTheme_shouldPropagate400WhenThemeIsInvalid() {
    when(profileService.updateTheme("test@example.com", "blue"))
        .thenThrow(
            new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Tema inválido. Use 'light' o 'dark'"));

    assertThrows(
        ResponseStatusException.class, () -> controller.updateTheme(Map.of("theme", "blue")));
  }

  @Test
  void updateTheme_shouldPropagate404WhenUserNotFound() {
    when(profileService.updateTheme("test@example.com", "dark"))
        .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

    assertThrows(
        ResponseStatusException.class, () -> controller.updateTheme(Map.of("theme", "dark")));
  }
}
