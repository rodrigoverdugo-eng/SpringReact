package com.example.springreact.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.springreact.model.Role;
import com.example.springreact.model.User;
import com.example.springreact.repository.UserRepository;
import java.util.Map;
import java.util.Optional;
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

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"null", "unchecked"})
class ProfileControllerTest {

  @Mock private UserRepository userRepository;
  @Mock private Authentication authentication;
  @Mock private SecurityContext securityContext;

  private ProfileController controller;
  private User testUser;

  @BeforeEach
  void setUp() {
    controller = new ProfileController(userRepository);
    Role role = new Role(1L, "ADMIN", "Administrator");
    testUser = new User("Test User", "test@example.com", "encoded", role);
    testUser.setId(1L);
    testUser.setThemePreference("light");
    SecurityContextHolder.setContext(securityContext);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("test@example.com");
  }

  // --- GET PROFILE ---

  @Test
  void getProfile_shouldReturn200WithProfileDataWhenUserFound() {
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

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
  void getProfile_shouldReturn404WhenUserNotFound() {
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

    ResponseEntity<?> result = controller.getProfile();

    assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
  }

  // --- UPDATE THEME ---

  @Test
  void updateTheme_shouldReturn200WhenThemeIsDark() {
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
    when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

    ResponseEntity<?> result = controller.updateTheme(Map.of("theme", "dark"));

    assertEquals(HttpStatus.OK, result.getStatusCode());
    Map<String, Object> body = (Map<String, Object>) result.getBody();
    assertNotNull(body);
    assertEquals("dark", body.get("themePreference"));
    assertEquals("Preferencia actualizada", body.get("message"));
    verify(userRepository).save(testUser);
  }

  @Test
  void updateTheme_shouldReturn200WhenThemeIsLight() {
    testUser.setThemePreference("dark");
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
    when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

    ResponseEntity<?> result = controller.updateTheme(Map.of("theme", "light"));

    assertEquals(HttpStatus.OK, result.getStatusCode());
    Map<String, Object> body = (Map<String, Object>) result.getBody();
    assertEquals("light", body.get("themePreference"));
  }

  @Test
  void updateTheme_shouldReturn400WhenThemeIsInvalid() {
    ResponseEntity<?> result = controller.updateTheme(Map.of("theme", "blue"));

    assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    Map<String, Object> body = (Map<String, Object>) result.getBody();
    assertNotNull(body);
    assertTrue(body.get("message").toString().contains("Tema inválido"));
    verifyNoInteractions(userRepository);
  }

  @Test
  void updateTheme_shouldReturn404WhenUserNotFound() {
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

    ResponseEntity<?> result = controller.updateTheme(Map.of("theme", "dark"));

    assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
  }
}
