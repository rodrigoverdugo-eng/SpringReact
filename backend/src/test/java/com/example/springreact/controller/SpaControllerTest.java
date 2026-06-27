package com.example.springreact.controller;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SpaControllerTest {

  private final SpaController controller = new SpaController();

  @Test
  void forward_shouldReturnForwardToIndexHtml() {
    String result = controller.forward("dashboard", null);

    assertEquals("forward:/index.html", result);
  }
}
