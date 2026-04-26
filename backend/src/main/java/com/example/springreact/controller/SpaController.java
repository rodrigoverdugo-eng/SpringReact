package com.example.springreact.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

  /**
   * Maneja todas las rutas del frontend que no coincidan con endpoints de API Esto permite que
   * React Router maneje el enrutamiento del lado del cliente
   */
  @GetMapping(value = {"/", "/login", "/dashboard/**", "/users/**", "/roles/**"})
  public String forward() {
    return "forward:/index.html";
  }
}
