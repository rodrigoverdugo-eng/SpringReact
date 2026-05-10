package com.example.springreact.controller;

import com.example.springreact.model.Role;
import com.example.springreact.model.User;
import com.example.springreact.model.UserLoginHistory;
import com.example.springreact.repository.RoleRepository;
import com.example.springreact.repository.UserLoginHistoryRepository;
import com.example.springreact.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserLoginHistoryRepository loginHistoryRepository;

  // Obtener todos los usuarios
  @GetMapping
  public List<User> getAllUsers() {
    return userRepository.findAll();
  }

  // Obtener usuario por ID
  @GetMapping("/{id}")
  public ResponseEntity<User> getUserById(@PathVariable @NonNull Long id) {
    return userRepository
        .findById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  // Crear nuevo usuario
  @PostMapping
  public ResponseEntity<?> createUser(@RequestBody @NonNull User user) {
    // Verificar si el email ya existe
    if (userRepository.findByEmail(user.getEmail()).isPresent()) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(Map.of("message", "El email ya está registrado"));
    }

    // Hashear password antes de guardar
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    // Establecer que requiere cambio de contraseña
    user.setRequiresPasswordChange(true);
    // Si no tiene rol asignado o el rol solo tiene ID, buscar el rol completo
    if (user.getRole() == null || user.getRole().getName() == null) {
      Role role = null;
      if (user.getRole() != null) {
        Long roleId = user.getRole().getId();
        if (roleId != null) {
          role = roleRepository.findById(roleId).orElse(null);
        }
      }
      // Si no se encontró, asignar rol USER por defecto
      if (role == null) {
        role = roleRepository.findByName("USER").orElse(null);
      }
      user.setRole(role);
    }
    User saved = userRepository.save(user);
    MDC.put("event", "USER_CREATED");
    MDC.put("user_id", String.valueOf(saved.getId()));
    MDC.put("email", saved.getEmail());
    log.info("Usuario creado");
    MDC.clear();
    return ResponseEntity.ok(saved);
  }

  // Actualizar usuario
  @PutMapping("/{id}")
  public ResponseEntity<?> updateUser(
      @PathVariable @NonNull Long id, @RequestBody @NonNull User userDetails) {
    return userRepository
        .findById(id)
        .map(
            user -> {
              // Verificar si el nuevo email ya existe en otro usuario
              if (!user.getEmail().equals(userDetails.getEmail())) {
                if (userRepository.findByEmail(userDetails.getEmail()).isPresent()) {
                  return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                      .body(Map.of("message", "El email ya está registrado"));
                }
              }

              user.setName(userDetails.getName());
              user.setEmail(userDetails.getEmail());
              user.setVigencia(userDetails.getVigencia());
              if (userDetails.getRequiresPasswordChange() != null) {
                user.setRequiresPasswordChange(userDetails.getRequiresPasswordChange());
              }
              // Actualizar rol si se proporciona
              if (userDetails.getRole() != null) {
                Long roleId = userDetails.getRole().getId();
                if (roleId != null) {
                  Role role = roleRepository.findById(roleId).orElse(null);
                  if (role != null) {
                    user.setRole(role);
                  }
                }
              }
              if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
                // Hashear password antes de actualizar
                user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
              }
              User updated = userRepository.save(user);
              MDC.put("event", "USER_UPDATED");
              MDC.put("user_id", String.valueOf(updated.getId()));
              MDC.put("email", updated.getEmail());
              log.info("Usuario actualizado");
              MDC.clear();
              return ResponseEntity.ok(updated);
            })
        .orElse(ResponseEntity.notFound().build());
  }

  // Eliminar usuario
  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteUser(@PathVariable @NonNull Long id) {
    return userRepository
        .findById(id)
        .map(
            (@NonNull User user) -> {
              userRepository.delete(user);
              MDC.put("event", "USER_DELETED");
              MDC.put("user_id", String.valueOf(user.getId()));
              MDC.put("email", user.getEmail());
              log.info("Usuario eliminado");
              MDC.clear();
              return ResponseEntity.ok().build();
            })
        .orElse(ResponseEntity.notFound().build());
  }

  // Historial de logins de un usuario (últimos 20)
  @GetMapping("/{id}/login-history")
  public ResponseEntity<?> getLoginHistory(@PathVariable @NonNull Long id) {
    if (!userRepository.existsById(id)) {
      return ResponseEntity.notFound().build();
    }
    List<UserLoginHistory> history =
        loginHistoryRepository.findByUserIdOrderByLoginAtDesc(id, PageRequest.of(0, 20));
    List<Map<String, Object>> result =
        history.stream()
            .map(
                h -> {
                  Map<String, Object> item = new HashMap<>();
                  item.put("id", h.getId());
                  item.put("loginAt", h.getLoginAt());
                  item.put("ipAddress", h.getIpAddress());
                  return item;
                })
            .collect(Collectors.toList());
    return ResponseEntity.ok(result);
  }

  // Último acceso de todos los usuarios
  @GetMapping("/last-login")
  public ResponseEntity<?> getAllLastLogins() {
    List<Object[]> rows = loginHistoryRepository.findLastLoginPerUser();
    Map<Long, LocalDateTime> lastLoginMap = new HashMap<>();
    for (Object[] row : rows) {
      lastLoginMap.put((Long) row[0], (LocalDateTime) row[1]);
    }
    List<User> users = userRepository.findAll();
    List<Map<String, Object>> result =
        users.stream()
            .map(
                u -> {
                  Map<String, Object> item = new HashMap<>();
                  item.put("userId", u.getId());
                  item.put("lastLoginAt", lastLoginMap.get(u.getId()));
                  return item;
                })
            .collect(Collectors.toList());
    return ResponseEntity.ok(result);
  }
}
