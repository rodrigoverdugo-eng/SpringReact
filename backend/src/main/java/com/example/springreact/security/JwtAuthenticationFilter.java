package com.example.springreact.security;

import com.example.springreact.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtService jwtService;
    private final UserRepository userRepository;
    
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        String path = request.getRequestURI();
        
        // Ignorar rutas estáticas y públicas
        if (shouldNotFilter(path)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;
        
        // Si no hay header o no empieza con Bearer, continuar
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Extraer token
        jwt = authHeader.substring(7);
        
        try {
            // Extraer email del token
            userEmail = jwtService.extractEmail(jwt);

            // Si el email existe y no hay autenticación previa
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Validar que sea un access token
                if (!jwtService.isAccessToken(jwt)) {
                    filterChain.doFilter(request, response);
                    return;
                }

                // Validar token solo si el usuario existe
                if (userRepository.findByEmail(userEmail).isPresent() && jwtService.validateToken(jwt, userEmail)) {
                    // Crear objeto de autenticación
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userEmail,
                            null,
                            new ArrayList<>()
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Establecer autenticación en el contexto
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Si hay error, simplemente continuar sin autenticar
            logger.error("Error al procesar JWT: " + e.getMessage());
        }
        
        filterChain.doFilter(request, response);
    }
    
    private boolean shouldNotFilter(String path) {
        return path.equals("/") ||
               path.equals("/index.html") ||
               path.equals("/login") ||
               path.equals("/favicon.ico") ||
               path.startsWith("/assets/") ||
               path.startsWith("/static/") ||
               path.startsWith("/api/auth/login") ||
               path.startsWith("/api/auth/refresh") ||
               path.endsWith(".css") ||
               path.endsWith(".js") ||
               path.endsWith(".ico") ||
               path.endsWith(".png") ||
               path.endsWith(".jpg") ||
               path.endsWith(".jpeg") ||
               path.endsWith(".svg");
    }
}
