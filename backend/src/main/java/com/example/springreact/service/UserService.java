package com.example.springreact.service;

import com.example.springreact.model.Role;
import com.example.springreact.model.User;
import com.example.springreact.model.UserLoginHistory;
import com.example.springreact.repository.RoleRepository;
import com.example.springreact.repository.UserLoginHistoryRepository;
import com.example.springreact.repository.UserRepository;
import com.example.springreact.util.PasswordValidator;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserLoginHistoryRepository loginHistoryRepository;

  public List<User> getAllUsers() {
    return userRepository.findAll();
  }

  public User getUserById(Long id) {
    Objects.requireNonNull(id);
    return userRepository.findById(id).orElse(null);
  }

  public String createUser(User user) {
    if (userRepository.findByEmail(user.getEmail()).isPresent()) {
      return "El email ya está registrado";
    }
    if (!PasswordValidator.isValid(user.getPassword())) {
      return PasswordValidator.ERROR_MESSAGE;
    }
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    user.setRequiresPasswordChange(true);
    if (user.getRole() == null || user.getRole().getName() == null) {
      Role role = null;
      if (user.getRole() != null) {
        Long roleId = user.getRole().getId();
        if (roleId != null) {
          role = roleRepository.findById(roleId).orElse(null);
        }
      }
      if (role == null) {
        role = roleRepository.findByName("USER").orElse(null);
      }
      user.setRole(role);
    }
    userRepository.save(user);
    MDC.put("event", "USER_CREATED");
    MDC.put("user_id", String.valueOf(user.getId()));
    MDC.put("email", user.getEmail());
    log.info("Usuario creado");
    MDC.clear();
    return null;
  }

  public String updateUser(Long id, User userDetails) {
    Objects.requireNonNull(id);
    User user = userRepository.findById(id).orElse(null);
    if (user == null) {
      return "Usuario no encontrado";
    }
    if (!user.getEmail().equals(userDetails.getEmail())) {
      if (userRepository.findByEmail(userDetails.getEmail()).isPresent()) {
        return "El email ya está registrado";
      }
    }
    user.setName(userDetails.getName());
    user.setEmail(userDetails.getEmail());
    user.setVigencia(userDetails.getVigencia());
    if (userDetails.getRequiresPasswordChange() != null) {
      user.setRequiresPasswordChange(userDetails.getRequiresPasswordChange());
    }
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
      if (!PasswordValidator.isValid(userDetails.getPassword())) {
        return PasswordValidator.ERROR_MESSAGE;
      }
      user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
    }
    userRepository.save(user);
    MDC.put("event", "USER_UPDATED");
    MDC.put("user_id", String.valueOf(user.getId()));
    MDC.put("email", user.getEmail());
    log.info("Usuario actualizado");
    MDC.clear();
    return null;
  }

  @Transactional
  public boolean deleteUser(Long id) {
    Objects.requireNonNull(id);
    return userRepository
        .findById(id)
        .map(
            user -> {
              loginHistoryRepository.deleteByUserId(user.getId());
              userRepository.delete(user);
              MDC.put("event", "USER_DELETED");
              MDC.put("user_id", String.valueOf(user.getId()));
              MDC.put("email", user.getEmail());
              log.info("Usuario eliminado");
              MDC.clear();
              return true;
            })
        .orElse(false);
  }

  public List<Map<String, Object>> getLoginHistory(Long userId) {
    Objects.requireNonNull(userId);
    if (!userRepository.existsById(userId)) {
      return null;
    }
    List<UserLoginHistory> history =
        loginHistoryRepository.findByUserIdOrderByLoginAtDesc(userId, PageRequest.of(0, 20));
    return history.stream()
        .map(
            h -> {
              Map<String, Object> item = new HashMap<>();
              item.put("id", h.getId());
              item.put("loginAt", h.getLoginAt());
              item.put("ipAddress", h.getIpAddress());
              return item;
            })
        .collect(Collectors.toList());
  }

  public List<Map<String, Object>> getAllLastLogins() {
    List<Object[]> rows = loginHistoryRepository.findLastLoginPerUser();
    Map<Long, LocalDateTime> lastLoginMap = new HashMap<>();
    for (Object[] row : rows) {
      lastLoginMap.put((Long) row[0], (LocalDateTime) row[1]);
    }
    return userRepository.findAll().stream()
        .map(
            u -> {
              Map<String, Object> item = new HashMap<>();
              item.put("userId", u.getId());
              item.put("lastLoginAt", lastLoginMap.get(u.getId()));
              return item;
            })
        .collect(Collectors.toList());
  }
}
