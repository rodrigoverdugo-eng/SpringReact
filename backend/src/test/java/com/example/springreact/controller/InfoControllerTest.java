package com.example.springreact.controller;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class InfoControllerTest {

  private InfoController controller;

  @BeforeEach
  void setUp() {
    controller = new InfoController();
    ReflectionTestUtils.setField(controller, "appVersion", "1.0.0");
  }

  @Test
  void getVersion_shouldReturnVersion() {
    Map<String, String> result = controller.getVersion();

    assertEquals("1.0.0", result.get("version"));
  }
}
