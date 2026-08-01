package com.example.springreact.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.springreact.model.Role;
import com.example.springreact.model.User;
import com.example.springreact.repository.UserRepository;
import com.example.springreact.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class JwtAuthenticationFilterTest {

  @Mock private JwtService jwtService;
  @Mock private UserRepository userRepository;
  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;
  @Mock private FilterChain filterChain;

  private JwtAuthenticationFilter filter;

  @BeforeEach
  void setUp() {
    filter = new JwtAuthenticationFilter(jwtService, userRepository);
    SecurityContextHolder.clearContext();
  }

  @Test
  void shouldSkipPublicPaths() throws Exception {
    when(request.getRequestURI()).thenReturn("/api/auth/login");

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(jwtService);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void shouldSkipStaticResources() throws Exception {
    when(request.getRequestURI()).thenReturn("/assets/app.js");

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(jwtService);
  }

  @Test
  void shouldSkipRootPath() throws Exception {
    when(request.getRequestURI()).thenReturn("/");

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(jwtService);
  }

  @Test
  void shouldProceedWithoutAuthWhenNoAuthorizationHeader() throws Exception {
    when(request.getRequestURI()).thenReturn("/api/users");
    when(request.getHeader("Authorization")).thenReturn(null);

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void shouldProceedWithoutAuthWhenHeaderDoesNotStartWithBearer() throws Exception {
    when(request.getRequestURI()).thenReturn("/api/users");
    when(request.getHeader("Authorization")).thenReturn("Basic token");

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void shouldAuthenticateWithValidAccessToken() throws Exception {
    String token = "valid-access-token";
    String email = "user@example.com";
    User user = new User("User", email, "password", new Role("USER"));

    when(request.getRequestURI()).thenReturn("/api/users");
    when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
    when(jwtService.extractEmail(token)).thenReturn(email);
    when(jwtService.isAccessToken(token)).thenReturn(true);
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    when(jwtService.validateToken(token, email)).thenReturn(true);
    when(jwtService.extractRole(token)).thenReturn("USER");

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    assertNotNull(auth);
    assertEquals(email, auth.getPrincipal());
    assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
  }

  @Test
  void shouldAuthenticateWithAdminRole() throws Exception {
    String token = "admin-access-token";
    String email = "admin@example.com";
    User user = new User("Admin", email, "password", new Role("ADMIN"));

    when(request.getRequestURI()).thenReturn("/api/users");
    when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
    when(jwtService.extractEmail(token)).thenReturn(email);
    when(jwtService.isAccessToken(token)).thenReturn(true);
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    when(jwtService.validateToken(token, email)).thenReturn(true);
    when(jwtService.extractRole(token)).thenReturn("ADMIN");

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    assertNotNull(auth);
    assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
  }

  @Test
  void shouldNotAuthenticateWithRefreshToken() throws Exception {
    String token = "refresh-token";
    String email = "user@example.com";

    when(request.getRequestURI()).thenReturn("/api/users");
    when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
    when(jwtService.extractEmail(token)).thenReturn(email);
    when(jwtService.isAccessToken(token)).thenReturn(false);

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void shouldNotAuthenticateWhenUserNotFound() throws Exception {
    String token = "valid-access-token";
    String email = "unknown@example.com";

    when(request.getRequestURI()).thenReturn("/api/users");
    when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
    when(jwtService.extractEmail(token)).thenReturn(email);
    when(jwtService.isAccessToken(token)).thenReturn(true);
    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void shouldNotAuthenticateWhenTokenValidationFails() throws Exception {
    String token = "invalid-token";
    String email = "user@example.com";
    User user = new User("User", email, "password", new Role("USER"));

    when(request.getRequestURI()).thenReturn("/api/users");
    when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
    when(jwtService.extractEmail(token)).thenReturn(email);
    when(jwtService.isAccessToken(token)).thenReturn(true);
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    when(jwtService.validateToken(token, email)).thenReturn(false);

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void shouldHandleExceptionGracefully() throws Exception {
    String token = "bad-token";

    when(request.getRequestURI()).thenReturn("/api/users");
    when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
    when(jwtService.extractEmail(token)).thenThrow(new RuntimeException("JWT parse error"));

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  // --- shouldNotFilter — cubre cada rama "tomada" del OR ---

  @Test
  void shouldSkipIndexHtml() throws Exception {
    when(request.getRequestURI()).thenReturn("/index.html");
    filter.doFilterInternal(request, response, filterChain);
    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(jwtService);
  }

  @Test
  void shouldSkipLoginPath() throws Exception {
    when(request.getRequestURI()).thenReturn("/login");
    filter.doFilterInternal(request, response, filterChain);
    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(jwtService);
  }

  @Test
  void shouldSkipFaviconIco() throws Exception {
    when(request.getRequestURI()).thenReturn("/favicon.ico");
    filter.doFilterInternal(request, response, filterChain);
    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(jwtService);
  }

  @Test
  void shouldSkipStaticPath() throws Exception {
    when(request.getRequestURI()).thenReturn("/static/logo.png");
    filter.doFilterInternal(request, response, filterChain);
    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(jwtService);
  }

  @Test
  void shouldSkipAuthRefreshPath() throws Exception {
    when(request.getRequestURI()).thenReturn("/api/auth/refresh");
    filter.doFilterInternal(request, response, filterChain);
    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(jwtService);
  }

  @Test
  void shouldSkipCssFiles() throws Exception {
    when(request.getRequestURI()).thenReturn("/assets/styles.css");
    filter.doFilterInternal(request, response, filterChain);
    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(jwtService);
  }

  @Test
  void shouldSkipJsFiles() throws Exception {
    when(request.getRequestURI()).thenReturn("/assets/bundle.js");
    filter.doFilterInternal(request, response, filterChain);
    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(jwtService);
  }

  @Test
  void shouldSkipIcoFiles() throws Exception {
    when(request.getRequestURI()).thenReturn("/favicon2.ico");
    filter.doFilterInternal(request, response, filterChain);
    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(jwtService);
  }

  @Test
  void shouldSkipPngFiles() throws Exception {
    when(request.getRequestURI()).thenReturn("/logo.png");
    filter.doFilterInternal(request, response, filterChain);
    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(jwtService);
  }

  @Test
  void shouldSkipJpgFiles() throws Exception {
    when(request.getRequestURI()).thenReturn("/image.jpg");
    filter.doFilterInternal(request, response, filterChain);
    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(jwtService);
  }

  @Test
  void shouldSkipJpegFiles() throws Exception {
    when(request.getRequestURI()).thenReturn("/image.jpeg");
    filter.doFilterInternal(request, response, filterChain);
    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(jwtService);
  }

  @Test
  void shouldSkipSvgFiles() throws Exception {
    when(request.getRequestURI()).thenReturn("/icon.svg");
    filter.doFilterInternal(request, response, filterChain);
    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(jwtService);
  }

  // --- Ramas adicionales del flujo principal ---

  @Test
  void shouldSkipAuthenticationWhenContextAlreadyHasAuth() throws Exception {
    // Cubre la rama: userEmail != null PERO Authentication ya existe en el contexto
    String token = "valid-token";
    String email = "user@example.com";
    Authentication existingAuth = mock(Authentication.class);
    SecurityContextHolder.getContext().setAuthentication(existingAuth);

    when(request.getRequestURI()).thenReturn("/api/users");
    when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
    when(jwtService.extractEmail(token)).thenReturn(email);

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    // La autenticación no fue reemplazada
    assertEquals(existingAuth, SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void shouldAuthenticateWithNullRoleAsEmptyAuthorities() throws Exception {
    // Cubre la rama: extractRole devuelve null → authorities vacías
    String token = "valid-access-token";
    String email = "user@example.com";
    User user = new User("User", email, "password", new Role("USER"));

    when(request.getRequestURI()).thenReturn("/api/data");
    when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
    when(jwtService.extractEmail(token)).thenReturn(email);
    when(jwtService.isAccessToken(token)).thenReturn(true);
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    when(jwtService.validateToken(token, email)).thenReturn(true);
    when(jwtService.extractRole(token)).thenReturn(null);

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    assertNotNull(auth);
    assertTrue(auth.getAuthorities().isEmpty());
  }
}
