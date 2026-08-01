package com.example.springreact.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.example.springreact.model.Role;
import com.example.springreact.model.User;
import com.example.springreact.model.UserLoginHistory;
import com.example.springreact.repository.RoleRepository;
import com.example.springreact.repository.UserLoginHistoryRepository;
import com.example.springreact.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
class UserServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private RoleRepository roleRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private UserLoginHistoryRepository loginHistoryRepository;

  @Captor private ArgumentCaptor<User> userCaptor;

  private UserService userService;
  private Role adminRole;
  private Role userRole;
  private User adminUser;
  private User standardUser;

  @BeforeEach
  void setUp() {
    userService =
        new UserService(userRepository, roleRepository, passwordEncoder, loginHistoryRepository);
    adminRole = new Role(1L, "ADMIN", "Administrator");
    userRole = new Role(2L, "USER", "User");
    adminUser = new User("Admin", "admin@example.com", "encoded", adminRole);
    adminUser.setId(1L);
    standardUser = new User("User", "user@example.com", "encoded", userRole);
    standardUser.setId(2L);
  }

  @Test
  void getAllUsers_shouldReturnAllUsers() {
    when(userRepository.findAll()).thenReturn(List.of(adminUser, standardUser));
    List<User> result = userService.getAllUsers();
    assertEquals(2, result.size());
  }

  @Test
  void getUserById_shouldReturnUserWhenFound() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
    User result = userService.getUserById(1L);
    assertEquals("admin@example.com", result.getEmail());
  }

  @Test
  void getUserById_shouldReturnNullWhenNotFound() {
    when(userRepository.findById(99L)).thenReturn(Optional.empty());
    assertNull(userService.getUserById(99L));
  }

  @Test
  void createUser_shouldReturnErrorWhenEmailExists() {
    User newUser = new User("New", "admin@example.com", "Pass1!word", adminRole);
    when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));
    String error = userService.createUser(newUser);
    assertEquals("El email ya está registrado", error);
  }

  @Test
  void createUser_shouldReturnErrorWhenPasswordInvalid() {
    User newUser = new User("New", "new@example.com", "weak", adminRole);
    when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
    String error = userService.createUser(newUser);
    assertTrue(error.contains("al menos 8 caracteres"));
  }

  @Test
  void createUser_shouldEncodePasswordAndSetRequiresPasswordChange() {
    User newUser = new User("New", "new@example.com", "RawPass1!", adminRole);
    when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
    when(passwordEncoder.encode("RawPass1!")).thenReturn("encoded-pass");
    when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
    String error = userService.createUser(newUser);
    assertNull(error);
    verify(userRepository).save(userCaptor.capture());
    User saved = userCaptor.getValue();
    assertEquals("encoded-pass", saved.getPassword());
    assertTrue(saved.getRequiresPasswordChange());
  }

  @Test
  void createUser_shouldAssignUserRoleWhenNoRoleProvided() {
    User newUser = new User("New", "new@example.com", "Pass1!word", null);
    when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
    when(passwordEncoder.encode("Pass1!word")).thenReturn("enc");
    when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
    when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
    userService.createUser(newUser);
    verify(userRepository).save(userCaptor.capture());
    assertEquals(userRole, userCaptor.getValue().getRole());
  }

  @Test
  void createUser_shouldLookupRoleByIdWhenOnlyIdProvided() {
    User partialRole = new User();
    partialRole.setRole(new Role(99L, null, null));
    partialRole.setName("New");
    partialRole.setEmail("new@example.com");
    partialRole.setPassword("Pass1!word");
    when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
    when(passwordEncoder.encode("Pass1!word")).thenReturn("enc");
    when(roleRepository.findById(99L)).thenReturn(Optional.of(adminRole));
    when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
    userService.createUser(partialRole);
    verify(userRepository).save(userCaptor.capture());
    assertEquals(adminRole, userCaptor.getValue().getRole());
  }

  @Test
  void updateUser_shouldReturnErrorWhenNotFound() {
    when(userRepository.findById(99L)).thenReturn(Optional.empty());
    String error = userService.updateUser(99L, new User());
    assertEquals("Usuario no encontrado", error);
  }

  @Test
  void updateUser_shouldReturnErrorWhenEmailAlreadyExists() {
    User updatedDetails = new User("Admin", "user@example.com", null, adminRole);
    when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
    when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(standardUser));
    String error = userService.updateUser(1L, updatedDetails);
    assertEquals("El email ya está registrado", error);
  }

  @Test
  void updateUser_shouldUpdateFields() {
    User updatedDetails = new User("New Name", "new@example.com", "NewPass1!", userRole);
    updatedDetails.setVigencia(false);
    updatedDetails.setRequiresPasswordChange(true);

    when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
    when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
    when(roleRepository.findById(2L)).thenReturn(Optional.of(userRole));
    when(passwordEncoder.encode("NewPass1!")).thenReturn("encoded-new");
    when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

    String error = userService.updateUser(1L, updatedDetails);
    assertNull(error);
    verify(userRepository).save(userCaptor.capture());
    User saved = userCaptor.getValue();
    assertEquals("New Name", saved.getName());
    assertEquals("new@example.com", saved.getEmail());
    assertFalse(saved.getVigencia());
    assertTrue(saved.getRequiresPasswordChange());
    assertEquals("encoded-new", saved.getPassword());
  }

  @Test
  void updateUser_shouldNotUpdatePasswordWhenNull() {
    User updatedDetails = new User("Admin", "admin@example.com", null, adminRole);
    when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
    when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
    userService.updateUser(1L, updatedDetails);
    verify(userRepository).save(userCaptor.capture());
    assertEquals("encoded", userCaptor.getValue().getPassword());
  }

  @Test
  void deleteUser_shouldReturnFalseWhenNotFound() {
    when(userRepository.findById(99L)).thenReturn(Optional.empty());
    assertFalse(userService.deleteUser(99L));
  }

  @Test
  void deleteUser_shouldDeleteLoginHistoryAndUser() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
    assertTrue(userService.deleteUser(1L));
    verify(loginHistoryRepository).deleteByUserId(1L);
    verify(userRepository).delete(adminUser);
  }

  @Test
  void getLoginHistory_shouldReturnNullWhenUserNotFound() {
    when(userRepository.existsById(99L)).thenReturn(false);
    assertNull(userService.getLoginHistory(99L));
  }

  @Test
  void getLoginHistory_shouldReturnHistory() {
    LocalDateTime loginAt = LocalDateTime.of(2025, 6, 1, 12, 0);
    UserLoginHistory history = new UserLoginHistory(1L, adminUser, loginAt, "10.0.0.1");
    when(userRepository.existsById(1L)).thenReturn(true);
    when(loginHistoryRepository.findByUserIdOrderByLoginAtDesc(eq(1L), any()))
        .thenReturn(List.of(history));
    List<Map<String, Object>> result = userService.getLoginHistory(1L);
    assertEquals(1, result.size());
    assertEquals(loginAt, result.get(0).get("loginAt"));
    assertEquals("10.0.0.1", result.get(0).get("ipAddress"));
  }

  @Test
  void getAllLastLogins_shouldReturnMap() {
    LocalDateTime lastLogin = LocalDateTime.of(2025, 6, 1, 15, 30);
    when(loginHistoryRepository.findLastLoginPerUser())
        .thenReturn(List.<Object[]>of(new Object[] {1L, lastLogin}));
    when(userRepository.findAll()).thenReturn(List.of(adminUser, standardUser));
    List<Map<String, Object>> result = userService.getAllLastLogins();
    assertEquals(2, result.size());
    assertEquals(1L, result.get(0).get("userId"));
    assertEquals(lastLogin, result.get(0).get("lastLoginAt"));
  }
}
