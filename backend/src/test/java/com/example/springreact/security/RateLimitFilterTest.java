package com.example.springreact.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.springreact.config.RateLimitConfig;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RateLimitFilterTest {

  @Mock private RateLimitConfig rateLimitConfig;
  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;
  @Mock private FilterChain filterChain;

  private RateLimitFilter filter;

  @BeforeEach
  void setUp() {
    filter = new RateLimitFilter(rateLimitConfig);
  }

  @Test
  void shouldAllowLoginRequestsWithinLimit() throws Exception {
    when(rateLimitConfig.getMaxAttempts()).thenReturn(5);
    when(rateLimitConfig.getRefillMinutes()).thenReturn(1);
    when(request.getRequestURI()).thenReturn("/api/auth/login");
    when(request.getRemoteAddr()).thenReturn("192.168.1.1");

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
  }

  @Test
  void shouldBlockLoginRequestsWhenLimitExceeded() throws Exception {
    when(rateLimitConfig.getMaxAttempts()).thenReturn(5);
    when(rateLimitConfig.getRefillMinutes()).thenReturn(1);
    when(request.getRequestURI()).thenReturn("/api/auth/login");
    when(request.getRemoteAddr()).thenReturn("192.168.1.1");

    for (int i = 0; i < 5; i++) {
      filter.doFilterInternal(request, response, filterChain);
    }
    verify(filterChain, times(5)).doFilter(request, response);

    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(printWriter);

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain, times(5)).doFilter(request, response);
    verify(response).setStatus(429);
    assertTrue(stringWriter.toString().contains("Demasiados intentos"));
  }

  @Test
  void shouldApplyRateLimitToChangePasswordEndpoint() throws Exception {
    when(rateLimitConfig.getMaxAttempts()).thenReturn(5);
    when(rateLimitConfig.getRefillMinutes()).thenReturn(1);
    when(request.getRequestURI()).thenReturn("/api/auth/change-password");
    when(request.getRemoteAddr()).thenReturn("10.0.0.1");

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
  }

  @Test
  void shouldNotApplyRateLimitToOtherEndpoints() throws Exception {
    when(request.getRequestURI()).thenReturn("/api/users");
    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(response);
  }

  @Test
  void shouldTrackDifferentIpsIndependently() throws Exception {
    when(rateLimitConfig.getMaxAttempts()).thenReturn(5);
    when(rateLimitConfig.getRefillMinutes()).thenReturn(1);
    when(request.getRequestURI()).thenReturn("/api/auth/login");

    when(request.getRemoteAddr()).thenReturn("10.0.0.1");
    for (int i = 0; i < 5; i++) {
      filter.doFilterInternal(request, response, filterChain);
    }

    when(request.getRemoteAddr()).thenReturn("10.0.0.2");
    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain, times(6)).doFilter(request, response);
  }
}
