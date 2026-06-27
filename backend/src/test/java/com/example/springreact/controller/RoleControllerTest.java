package com.example.springreact.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.springreact.model.Role;
import com.example.springreact.repository.RoleRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoleControllerTest {

  @Mock private RoleRepository roleRepository;

  private RoleController controller;

  @BeforeEach
  void setUp() {
    controller = new RoleController(roleRepository);
  }

  @Test
  void getAllRoles_shouldReturnRolesOrdered() {
    Role userRole = new Role(2L, "USER", "User");
    Role adminRole = new Role(1L, "ADMIN", "Admin");
    when(roleRepository.findAllOrderedAdminLast()).thenReturn(List.of(userRole, adminRole));

    List<Role> result = controller.getAllRoles();

    assertEquals(2, result.size());
    assertEquals("USER", result.get(0).getName());
    assertEquals("ADMIN", result.get(1).getName());
  }

  @Test
  void getAllRoles_shouldReturnEmptyListWhenNoRoles() {
    when(roleRepository.findAllOrderedAdminLast()).thenReturn(List.of());

    List<Role> result = controller.getAllRoles();

    assertTrue(result.isEmpty());
  }
}
