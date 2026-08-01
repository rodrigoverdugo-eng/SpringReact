package com.example.springreact.service;

import static org.junit.jupiter.api.Assertions.*;
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
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class ProfileServiceTest {

  @Mock private UserRepository userRepository;

  private ProfileService profileService;
  private User testUser;

  @BeforeEach
  void setUp() {
    profileService = new ProfileService(userRepository);
    Role role = new Role(1L, "ADMIN", "Administrator");
    testUser = new User("Test User", "test@example.com", "encoded", role);
    testUser.setId(1L);
    testUser.setThemePreference("light");
  }

  @Test
  void getProfile_shouldReturnProfileDataWhenUserFound() {
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
    Map<String, Object> profile = profileService.getProfile("test@example.com");
    assertEquals(1L, profile.get("id"));
    assertEquals("Test User", profile.get("name"));
    assertEquals("test@example.com", profile.get("email"));
    assertEquals("light", profile.get("themePreference"));
  }

  @Test
  void getProfile_shouldThrow404WhenUserNotFound() {
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
    assertThrows(
        ResponseStatusException.class, () -> profileService.getProfile("test@example.com"));
  }

  @Test
  void updateTheme_shouldReturnThemeWhenDark() {
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
    when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
    String result = profileService.updateTheme("test@example.com", "dark");
    assertEquals("dark", result);
    verify(userRepository).save(testUser);
  }

  @Test
  void updateTheme_shouldReturnThemeWhenLight() {
    testUser.setThemePreference("dark");
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
    when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
    String result = profileService.updateTheme("test@example.com", "light");
    assertEquals("light", result);
  }

  @Test
  void updateTheme_shouldThrow400WhenThemeIsInvalid() {
    assertThrows(
        ResponseStatusException.class,
        () -> profileService.updateTheme("test@example.com", "blue"));
    verifyNoInteractions(userRepository);
  }

  @Test
  void updateTheme_shouldThrow404WhenUserNotFound() {
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
    assertThrows(
        ResponseStatusException.class,
        () -> profileService.updateTheme("test@example.com", "dark"));
  }
}
