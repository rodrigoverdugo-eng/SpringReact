package com.example.springreact.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.springreact.model.Role;
import com.example.springreact.model.User;
import com.example.springreact.service.UserService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

  @Mock private UserService userService;

  private UserController controller;
  private Role adminRole;
  private Role userRole;
  private User adminUser;
  private User standardUser;

  @BeforeEach
  void setUp() {
    controller = new UserController(userService);
    adminRole = new Role(1L, "ADMIN", "Administrator");
    userRole = new Role(2L, "USER", "User");
    adminUser = new User("Admin", "admin@example.com", "encoded", adminRole);
    adminUser.setId(1L);
    standardUser = new User("User", "user@example.com", "encoded", userRole);
    standardUser.setId(2L);
  }

  @Test
  void getAllUsers_shouldReturnAllUsers() {
    when(userService.getAllUsers()).thenReturn(List.of(adminUser, standardUser));
    List<User> result = controller.getAllUsers();
    assertEquals(2, result.size());
  }

  @Test
  void getUserById_shouldReturnUserWhenFound() {
    when(userService.getUserById(1L)).thenReturn(adminUser);
    ResponseEntity<User> result = controller.getUserById(1L);
    assertEquals(HttpStatus.OK, result.getStatusCode());
    User body = result.getBody();
    assertNotNull(body);
    assertEquals("admin@example.com", body.getEmail());
  }

  @Test
  void getUserById_shouldReturn404WhenNotFound() {
    when(userService.getUserById(99L)).thenReturn(null);
    ResponseEntity<User> result = controller.getUserById(99L);
    assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
  }

  @Test
  void createUser_shouldReturn400WhenEmailAlreadyExists() {
    User newUser = new User("New", "admin@example.com", "pass", adminRole);
    when(userService.createUser(newUser)).thenReturn("El email ya está registrado");
    ResponseEntity<?> result = controller.createUser(newUser);
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    Object body = result.getBody();
    assertNotNull(body);
    assertEquals("El email ya está registrado", ((Map<?, ?>) body).get("message"));
  }

  @Test
  void createUser_shouldReturn200WhenSuccessful() {
    User newUser = new User("New", "new@example.com", "RawPass1!", adminRole);
    when(userService.createUser(newUser)).thenReturn(null);
    ResponseEntity<?> result = controller.createUser(newUser);
    assertEquals(HttpStatus.OK, result.getStatusCode());
  }

  @Test
  void updateUser_shouldReturn404WhenNotFound() {
    when(userService.updateUser(eq(99L), any())).thenReturn("Usuario no encontrado");
    ResponseEntity<?> result = controller.updateUser(99L, new User());
    assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
  }

  @Test
  void updateUser_shouldReturn400WhenEmailAlreadyExists() {
    User updatedDetails = new User("Admin", "user@example.com", null, adminRole);
    when(userService.updateUser(1L, updatedDetails)).thenReturn("El email ya está registrado");
    ResponseEntity<?> result = controller.updateUser(1L, updatedDetails);
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
  }

  @Test
  void updateUser_shouldReturn200WhenSuccessful() {
    User updatedDetails = new User("New Name", "new@example.com", "NewPass1!", adminRole);
    when(userService.updateUser(1L, updatedDetails)).thenReturn(null);
    ResponseEntity<?> result = controller.updateUser(1L, updatedDetails);
    assertEquals(HttpStatus.OK, result.getStatusCode());
  }

  @Test
  void deleteUser_shouldReturn404WhenNotFound() {
    when(userService.deleteUser(99L)).thenReturn(false);
    ResponseEntity<?> result = controller.deleteUser(99L);
    assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
  }

  @Test
  void deleteUser_shouldReturn200WhenSuccessful() {
    when(userService.deleteUser(1L)).thenReturn(true);
    ResponseEntity<?> result = controller.deleteUser(1L);
    assertEquals(HttpStatus.OK, result.getStatusCode());
  }

  @Test
  void getLoginHistory_shouldReturn404WhenUserNotFound() {
    when(userService.getLoginHistory(99L)).thenReturn(null);
    ResponseEntity<?> result = controller.getLoginHistory(99L);
    assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
  }

  @Test
  void getAllLastLogins_shouldReturn200() {
    when(userService.getAllLastLogins()).thenReturn(List.of());
    ResponseEntity<?> result = controller.getAllLastLogins();
    assertEquals(HttpStatus.OK, result.getStatusCode());
  }
}
