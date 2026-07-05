package com.example.springreact.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"null", "unchecked"})
class UserControllerTest {

  @Mock private UserRepository userRepository;
  @Mock private RoleRepository roleRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private UserLoginHistoryRepository loginHistoryRepository;

  @Captor private ArgumentCaptor<User> userCaptor;

  private UserController controller;
  private Role adminRole;
  private Role userRole;
  private User adminUser;
  private User standardUser;

  @BeforeEach
  void setUp() {
    controller =
        new UserController(userRepository, roleRepository, passwordEncoder, loginHistoryRepository);
    adminRole = new Role(1L, "ADMIN", "Administrator");
    userRole = new Role(2L, "USER", "User");
    adminUser = new User("Admin", "admin@example.com", "encoded", adminRole);
    adminUser.setId(1L);
    standardUser = new User("User", "user@example.com", "encoded", userRole);
    standardUser.setId(2L);
  }

  // --- GET ALL USERS ---

  @Test
  void getAllUsers_shouldReturnAllUsers() {
    when(userRepository.findAll()).thenReturn(List.of(adminUser, standardUser));

    List<User> result = controller.getAllUsers();

    assertEquals(2, result.size());
  }

  // --- GET USER BY ID ---

  @Test
  void getUserById_shouldReturnUserWhenFound() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

    ResponseEntity<User> result = controller.getUserById(1L);

    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertEquals("admin@example.com", result.getBody().getEmail());
  }

  @Test
  void getUserById_shouldReturn404WhenNotFound() {
    when(userRepository.findById(99L)).thenReturn(Optional.empty());

    ResponseEntity<User> result = controller.getUserById(99L);

    assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
  }

  // --- CREATE USER ---

  @Test
  void createUser_shouldReturn400WhenEmailAlreadyExists() {
    User newUser = new User("New", "admin@example.com", "pass", adminRole);
    when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));

    ResponseEntity<?> result = controller.createUser(newUser);

    assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    assertEquals("El email ya está registrado", ((Map<?, ?>) result.getBody()).get("message"));
  }

  @Test
  void createUser_shouldEncodePasswordAndSetRequiresPasswordChange() {
    User newUser = new User("New", "new@example.com", "RawPass1!", adminRole);
    when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
    when(passwordEncoder.encode("RawPass1!")).thenReturn("encoded-pass");
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    ResponseEntity<?> result = controller.createUser(newUser);

    assertEquals(HttpStatus.OK, result.getStatusCode());
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
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    controller.createUser(newUser);

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
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    controller.createUser(partialRole);

    verify(userRepository).save(userCaptor.capture());
    assertEquals(adminRole, userCaptor.getValue().getRole());
  }

  // --- UPDATE USER ---

  @Test
  void updateUser_shouldReturn404WhenNotFound() {
    when(userRepository.findById(99L)).thenReturn(Optional.empty());

    ResponseEntity<?> result = controller.updateUser(99L, new User());

    assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
  }

  @Test
  void updateUser_shouldReturn400WhenEmailAlreadyExists() {
    User updatedDetails = new User("Admin", "user@example.com", null, adminRole);

    when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
    when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(standardUser));

    ResponseEntity<?> result = controller.updateUser(1L, updatedDetails);

    assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    assertEquals("El email ya está registrado", ((Map<?, ?>) result.getBody()).get("message"));
  }

  @Test
  void updateUser_shouldUpdateFields() {
    Role updatedRole = new Role(2L, "USER", "User");
    User updatedDetails = new User("New Name", "new@example.com", "NewPass1!", updatedRole);
    updatedDetails.setVigencia(false);
    updatedDetails.setRequiresPasswordChange(true);

    when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
    when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
    when(roleRepository.findById(2L)).thenReturn(Optional.of(userRole));
    when(passwordEncoder.encode("NewPass1!")).thenReturn("encoded-new");
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    ResponseEntity<?> result = controller.updateUser(1L, updatedDetails);

    assertEquals(HttpStatus.OK, result.getStatusCode());
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
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    controller.updateUser(1L, updatedDetails);

    verify(userRepository).save(userCaptor.capture());
    assertEquals("encoded", userCaptor.getValue().getPassword());
  }

  @Test
  void createUser_shouldReturn400WhenPasswordInvalid() {
    // Contraseña sin símbolo → falla isPasswordValid
    User newUser = new User("New", "new@example.com", "NoSymbol1", adminRole);
    when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());

    ResponseEntity<?> result = controller.createUser(newUser);

    assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    assertTrue(
        ((Map<?, ?>) result.getBody()).get("message").toString().contains("al menos 8 caracteres"));
  }

  @Test
  void updateUser_shouldReturn400WhenPasswordInvalid() {
    // Usar email diferente para que pase la validación de email y llegue a la de contraseña
    User updatedDetails = new User("Admin", "admin@example.com", "noSymbol1", adminRole);

    when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
    // Mismo email que el existente → no llama a findByEmail
    ResponseEntity<?> result = controller.updateUser(1L, updatedDetails);

    assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
  }

  @Test
  void updateUser_shouldSkipRoleUpdateWhenRoleIdIsNull() {
    // roleId == null → no busca en roleRepository
    User updatedDetails = new User("Admin", "admin@example.com", null, new Role(null, null, null));

    when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
    // Mismo email → findByEmail no se llama
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    ResponseEntity<?> result = controller.updateUser(1L, updatedDetails);

    assertEquals(HttpStatus.OK, result.getStatusCode());
    verifyNoInteractions(roleRepository);
  }

  // --- DELETE USER ---

  @Test
  void deleteUser_shouldReturn404WhenNotFound() {
    when(userRepository.findById(99L)).thenReturn(Optional.empty());

    ResponseEntity<?> result = controller.deleteUser(99L);

    assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
  }

  @Test
  void deleteUser_shouldDeleteLoginHistoryAndUser() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

    ResponseEntity<?> result = controller.deleteUser(1L);

    assertEquals(HttpStatus.OK, result.getStatusCode());
    verify(loginHistoryRepository).deleteByUserId(1L);
    verify(userRepository).delete(adminUser);
  }

  // --- LOGIN HISTORY ---

  @Test
  void getLoginHistory_shouldReturn404WhenUserNotFound() {
    when(userRepository.existsById(99L)).thenReturn(false);

    ResponseEntity<?> result = controller.getLoginHistory(99L);

    assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
  }

  @Test
  void getLoginHistory_shouldReturnHistory() {
    LocalDateTime loginAt = LocalDateTime.of(2025, 6, 1, 12, 0);
    UserLoginHistory history = new UserLoginHistory(1L, adminUser, loginAt, "10.0.0.1");

    when(userRepository.existsById(1L)).thenReturn(true);
    when(loginHistoryRepository.findByUserIdOrderByLoginAtDesc(1L, PageRequest.of(0, 20)))
        .thenReturn(List.of(history));

    ResponseEntity<?> result = controller.getLoginHistory(1L);

    assertEquals(HttpStatus.OK, result.getStatusCode());
    List<Map<String, Object>> body = (List<Map<String, Object>>) result.getBody();
    assertEquals(1, body.size());
    assertEquals(loginAt, body.get(0).get("loginAt"));
    assertEquals("10.0.0.1", body.get(0).get("ipAddress"));
  }

  // --- LAST LOGIN ---

  @Test
  void getAllLastLogins_shouldReturnMap() {
    LocalDateTime lastLogin = LocalDateTime.of(2025, 6, 1, 15, 30);
    when(loginHistoryRepository.findLastLoginPerUser())
        .thenReturn(List.<Object[]>of(new Object[] {1L, lastLogin}));
    when(userRepository.findAll()).thenReturn(List.of(adminUser, standardUser));

    ResponseEntity<?> result = controller.getAllLastLogins();

    assertEquals(HttpStatus.OK, result.getStatusCode());
    List<Map<String, Object>> body = (List<Map<String, Object>>) result.getBody();
    assertEquals(2, body.size());
    Map<String, Object> adminEntry = body.get(0);
    assertEquals(1L, adminEntry.get("userId"));
    assertEquals(lastLogin, adminEntry.get("lastLoginAt"));
  }
}
