package com.example.springreact.security;

import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthFilter;
  private final RateLimitFilter rateLimitFilter;

  @Bean
  public WebSecurityCustomizer webSecurityCustomizer() {
    return (web) ->
        web.ignoring()
            .requestMatchers(
                "/",
                "/index.html",
                "/login",
                "/favicon.ico",
                "/assets/**",
                "/static/**",
                "/*.css",
                "/*.js",
                "/*.ico",
                "/*.png",
                "/*.jpg",
                "/*.jpeg",
                "/*.svg");
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .authorizeHttpRequests(
            auth ->
                auth
                    // Rutas públicas de API (sin autenticación)
                    .requestMatchers("/api/auth/login", "/api/auth/refresh", "/api/auth/logout")
                    .permitAll()
                    // Gestión de usuarios: solo ADMIN
                    .requestMatchers("/api/users/**")
                    .hasRole("ADMIN")
                    // Resto de rutas de API requieren autenticación
                    .requestMatchers("/api/**")
                    .authenticated()
                    // Rutas SPA (no API): dejar pasar para que SpaController sirva index.html
                    .anyRequest()
                    .permitAll())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        // Agregar RateLimitFilter antes del filtro JWT
        .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

    http.headers(
        headers ->
            headers
                .contentSecurityPolicy(
                    csp ->
                        csp.policyDirectives(
                            "default-src 'self'; "
                                + "script-src 'self'; "
                                + "style-src 'self' 'unsafe-inline'; "
                                + "img-src 'self' data:; "
                                + "font-src 'self'; "
                                + "connect-src 'self'; "
                                + "frame-ancestors 'none'"))
                .xssProtection(
                    xss ->
                        xss.headerValue(
                            org.springframework.security.web.header.writers
                                .XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                .referrerPolicy(
                    referrer ->
                        referrer.policy(
                            org.springframework.security.web.header.writers
                                .ReferrerPolicyHeaderWriter.ReferrerPolicy
                                .STRICT_ORIGIN_WHEN_CROSS_ORIGIN)));

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(
        Arrays.asList(
            "https://springreact-production.up.railway.app",
            "http://localhost:3000",
            "http://localhost:5173",
            "http://localhost:8080"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(
        Arrays.asList("Authorization", "Content-Type", "Accept", "X-Requested-With"));
    configuration.setExposedHeaders(Arrays.asList("Authorization"));
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", configuration);
    return source;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
