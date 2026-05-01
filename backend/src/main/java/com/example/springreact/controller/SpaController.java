package com.example.springreact.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

  /**
   * Reenvía al index.html cualquier ruta que no sea API ni recurso estático, permitiendo que React
   * Router maneje el enrutamiento del lado del cliente. Solo coincide cuando el último segmento NO
   * tiene punto, evitando interceptar archivos estáticos como .css o .js.
   */
  @GetMapping(value = "/{path:[^\\.]*}", produces = "text/html")
  public String forward(HttpServletRequest request) {
    return "forward:/index.html";
  }
}
