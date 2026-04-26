package com.example.springreact.controller;

import com.example.springreact.model.Role;
import com.example.springreact.repository.RoleRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

  private final RoleRepository roleRepository;

  // Obtener todos los roles
  @GetMapping
  public List<Role> getAllRoles() {
    return roleRepository.findAllOrderedAdminLast();
  }
}
