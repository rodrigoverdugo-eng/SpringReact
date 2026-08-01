package com.example.springreact.controller;

import com.example.springreact.model.User;
import com.example.springreact.service.UserService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @GetMapping
  public List<User> getAllUsers() {
    return userService.getAllUsers();
  }

  @GetMapping("/{id}")
  public ResponseEntity<User> getUserById(@PathVariable @NonNull Long id) {
    User user = userService.getUserById(id);
    if (user == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(user);
  }

  @PostMapping
  public ResponseEntity<?> createUser(@RequestBody @NonNull User user) {
    String error = userService.createUser(user);
    if (error != null) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", error));
    }
    return ResponseEntity.ok(user);
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> updateUser(
      @PathVariable @NonNull Long id, @RequestBody @NonNull User userDetails) {
    String error = userService.updateUser(id, userDetails);
    if (error == null) {
      return ResponseEntity.ok(userDetails);
    }
    if ("Usuario no encontrado".equals(error)) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", error));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteUser(@PathVariable @NonNull Long id) {
    if (userService.deleteUser(id)) {
      return ResponseEntity.ok().build();
    }
    return ResponseEntity.notFound().build();
  }

  @GetMapping("/{id}/login-history")
  public ResponseEntity<?> getLoginHistory(@PathVariable @NonNull Long id) {
    List<Map<String, Object>> history = userService.getLoginHistory(id);
    if (history == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(history);
  }

  @GetMapping("/last-login")
  public ResponseEntity<?> getAllLastLogins() {
    return ResponseEntity.ok(userService.getAllLastLogins());
  }
}
