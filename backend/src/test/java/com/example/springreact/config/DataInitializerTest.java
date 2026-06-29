package com.example.springreact.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.springreact.model.Role;
import com.example.springreact.model.User;
import com.example.springreact.repository.RoleRepository;
import com.example.springreact.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class DataInitializerTest {

  @Mock private UserRepository userRepository;
  @Mock private RoleRepository roleRepository;
  @Mock private PasswordEncoder passwordEncoder;

  @Captor private ArgumentCaptor<Role> roleCaptor;
  @Captor private ArgumentCaptor<User> userCaptor;

  private DataInitializer dataInitializer;

  @BeforeEach
  void setUp() {
    dataInitializer = new DataInitializer(userRepository, roleRepository, passwordEncoder);
  }

  @Test
  void shouldCreateAdminAndUserRolesWhenTheyDoNotExist() throws Exception {
    when(roleRepository.findByName("ADMIN")).thenReturn(Optional.empty());
    when(roleRepository.findByName("USER")).thenReturn(Optional.empty());
    when(passwordEncoder.encode(anyString()))
        .thenAnswer(invocation -> "encoded_" + invocation.getArgument(0));
    when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.empty());
    when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());

    dataInitializer.initDatabase().run();

    verify(roleRepository, times(2)).save(roleCaptor.capture());
    assertEquals("ADMIN", roleCaptor.getAllValues().get(0).getName());
    assertEquals("USER", roleCaptor.getAllValues().get(1).getName());

    verify(userRepository, times(2)).save(userCaptor.capture());
    User savedAdmin = userCaptor.getAllValues().get(0);
    assertEquals("admin@example.com", savedAdmin.getEmail());
    assertFalse(savedAdmin.getRequiresPasswordChange());
    User savedUser = userCaptor.getAllValues().get(1);
    assertEquals("user@example.com", savedUser.getEmail());
    assertFalse(savedUser.getRequiresPasswordChange());
  }

  @Test
  void shouldNotCreateRolesWhenTheyAlreadyExist() throws Exception {
    Role existingAdmin = new Role(1L, "ADMIN", "Admin");
    Role existingUser = new Role(2L, "USER", "User");
    when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(existingAdmin));
    when(roleRepository.findByName("USER")).thenReturn(Optional.of(existingUser));
    when(passwordEncoder.encode(anyString()))
        .thenAnswer(invocation -> "encoded_" + invocation.getArgument(0));
    when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.empty());
    when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());

    dataInitializer.initDatabase().run();

    verify(roleRepository, never()).save(any());
    verify(userRepository, times(2)).save(any());
  }

  @Test
  void shouldNotCreateUsersWhenTheyAlreadyExist() throws Exception {
    Role adminRole = new Role(1L, "ADMIN", "Admin");
    Role userRole = new Role(2L, "USER", "User");
    when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(adminRole));
    when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
    when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(new User()));
    when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(new User()));

    dataInitializer.initDatabase().run();

    verify(userRepository, never()).save(any());
  }

  @Test
  void shouldEncodePasswordsForDefaultUsers() throws Exception {
    when(roleRepository.findByName("ADMIN"))
        .thenReturn(Optional.of(new Role(1L, "ADMIN", "Admin")));
    when(roleRepository.findByName("USER")).thenReturn(Optional.of(new Role(2L, "USER", "User")));
    when(passwordEncoder.encode(anyString()))
        .thenAnswer(invocation -> "encoded_" + invocation.getArgument(0));
    when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.empty());
    when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());

    dataInitializer.initDatabase().run();

    verify(passwordEncoder).encode("admin123");
    verify(passwordEncoder).encode("user123");
  }
}
