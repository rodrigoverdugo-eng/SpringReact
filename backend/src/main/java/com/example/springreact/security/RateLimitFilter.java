package com.example.springreact.security;

import com.example.springreact.config.RateLimitConfig;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

  private final RateLimitConfig rateLimitConfig;

  // Caché para almacenar buckets por IP
  private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    String path = request.getRequestURI();

    // Solo aplicar rate limiting a endpoints de autenticación
    if (path.startsWith("/api/auth/login") || path.startsWith("/api/auth/change-password")) {
      String clientIp = getClientIP(request);
      Bucket bucket = resolveBucket(clientIp);

      if (bucket.tryConsume(1)) {
        // Agregar headers informativos
        response.addHeader("X-Rate-Limit-Remaining", String.valueOf(bucket.getAvailableTokens()));
        filterChain.doFilter(request, response);
      } else {
        // Rate limit excedido
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");
        response
            .getWriter()
            .write(
                "{\"message\": \"Demasiados intentos. Por favor, espera un momento antes de intentar nuevamente.\"}");
      }
    } else {
      // No aplicar rate limiting a otras rutas
      filterChain.doFilter(request, response);
    }
  }

  private Bucket resolveBucket(String clientIp) {
    return cache.computeIfAbsent(clientIp, k -> createNewBucket());
  }

  private Bucket createNewBucket() {
    // Obtener configuración desde application.properties
    int maxRequests = rateLimitConfig.getMaxAttempts();
    Duration refillDuration = Duration.ofMinutes(rateLimitConfig.getRefillMinutes());

    // Usar el nuevo builder para Bandwidth
    Bandwidth limit =
        Bandwidth.builder()
            .capacity(maxRequests)
            .refillIntervally(maxRequests, refillDuration)
            .build();
    return Bucket.builder().addLimit(limit).build();
  }

  private String getClientIP(HttpServletRequest request) {
    // Intentar obtener la IP real si está detrás de un proxy
    String xfHeader = request.getHeader("X-Forwarded-For");
    if (xfHeader == null || xfHeader.isEmpty()) {
      return request.getRemoteAddr();
    }
    // X-Forwarded-For puede contener múltiples IPs, tomar la primera
    return xfHeader.split(",")[0].trim();
  }
}
