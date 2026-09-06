package com.example.springreact.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtServiceTest {

  private JwtService jwtService;

  @BeforeEach
  void setUp() {
    jwtService = new JwtService();
    ReflectionTestUtils.setField(
        jwtService, "secret", "dGVzdC1zZWNyZXQta2V5LWZvci1obWFjLXNoYTI1Ni10ZXN0aW5nLXB1cnBvc2Vz");
  }

  @Test
  void generateAccessToken_shouldIncludeAllClaims() {
    String token = jwtService.generateAccessToken("test@example.com", 1L, "Test User", "ADMIN", 0);

    assertNotNull(token);
    assertEquals("test@example.com", jwtService.extractEmail(token));
    assertEquals(1L, jwtService.extractUserId(token));
    assertEquals("ADMIN", jwtService.extractRole(token));
    assertEquals("access", jwtService.extractType(token));
    assertEquals(0, jwtService.extractTokenVersion(token));
  }

  @Test
  void generateRefreshToken_shouldHaveRefreshType() {
    String token = jwtService.generateRefreshToken("test@example.com", 0);

    assertNotNull(token);
    assertEquals("test@example.com", jwtService.extractEmail(token));
    assertEquals("refresh", jwtService.extractType(token));
  }

  @Test
  void extractTokenVersion_shouldReturnTokenVersionClaim() {
    assertEquals(
        7,
        jwtService.extractTokenVersion(
            jwtService.generateAccessToken("user@example.com", 1L, "User", "USER", 7)));
    assertEquals(
        7, jwtService.extractTokenVersion(jwtService.generateRefreshToken("user@example.com", 7)));
  }

  @Test
  void extractEmail_shouldReturnSubject() {
    String token = jwtService.generateAccessToken("user@example.com", 2L, "User", "USER", 0);

    assertEquals("user@example.com", jwtService.extractEmail(token));
  }

  @Test
  void extractUserId_shouldReturnUserIdClaim() {
    String token = jwtService.generateAccessToken("user@example.com", 42L, "User", "USER", 0);

    assertEquals(42L, jwtService.extractUserId(token));
  }

  @Test
  void extractRole_shouldReturnRoleClaim() {
    String token = jwtService.generateAccessToken("user@example.com", 1L, "User", "ADMIN", 0);

    assertEquals("ADMIN", jwtService.extractRole(token));
  }

  @Test
  void extractType_shouldReturnTypeClaim() {
    String accessToken = jwtService.generateAccessToken("user@example.com", 1L, "User", "USER", 0);
    String refreshToken = jwtService.generateRefreshToken("user@example.com", 0);

    assertEquals("access", jwtService.extractType(accessToken));
    assertEquals("refresh", jwtService.extractType(refreshToken));
  }

  @Test
  void extractExpiration_shouldReturnFutureDate() {
    String token = jwtService.generateAccessToken("user@example.com", 1L, "User", "USER", 0);

    assertTrue(jwtService.extractExpiration(token).getTime() > System.currentTimeMillis());
  }

  @Test
  void isTokenExpired_shouldReturnFalseForValidToken() {
    String token = jwtService.generateAccessToken("user@example.com", 1L, "User", "USER", 0);

    assertFalse(jwtService.isTokenExpired(token));
  }

  @Test
  void isTokenExpired_shouldReturnTrueForInvalidToken() {
    assertTrue(jwtService.isTokenExpired("invalid-token"));
  }

  @Test
  void validateToken_shouldReturnTrueForValidToken() {
    String token = jwtService.generateAccessToken("user@example.com", 1L, "User", "USER", 0);

    assertTrue(jwtService.validateToken(token, "user@example.com"));
  }

  @Test
  void validateToken_shouldReturnFalseForWrongEmail() {
    String token = jwtService.generateAccessToken("user@example.com", 1L, "User", "USER", 0);

    assertFalse(jwtService.validateToken(token, "other@example.com"));
  }

  @Test
  void validateToken_shouldReturnFalseForInvalidToken() {
    assertFalse(jwtService.validateToken("invalid-token", "user@example.com"));
  }

  @Test
  void isAccessToken_shouldReturnTrueForAccessToken() {
    String token = jwtService.generateAccessToken("user@example.com", 1L, "User", "USER", 0);

    assertTrue(jwtService.isAccessToken(token));
    assertFalse(jwtService.isRefreshToken(token));
  }

  @Test
  void isRefreshToken_shouldReturnTrueForRefreshToken() {
    String token = jwtService.generateRefreshToken("user@example.com", 0);

    assertTrue(jwtService.isRefreshToken(token));
    assertFalse(jwtService.isAccessToken(token));
  }

  @Test
  void isAccessToken_shouldReturnFalseForInvalidToken() {
    assertFalse(jwtService.isAccessToken("invalid-token"));
  }

  @Test
  void isRefreshToken_shouldReturnFalseForInvalidToken() {
    assertFalse(jwtService.isRefreshToken("invalid-token"));
  }
}
