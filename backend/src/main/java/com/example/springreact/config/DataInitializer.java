package com.example.springreact.config;

import com.example.springreact.model.Role;
import com.example.springreact.model.User;
import com.example.springreact.repository.RoleRepository;
import com.example.springreact.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Bean
    CommandLineRunner initDatabase() {
        return args -> {
            // Crear rol ADMIN si no existe
            Role adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> {
                    Role role = new Role("ADMIN", "Administrador del sistema");
                    roleRepository.save(role);
                    System.out.println("✅ Rol ADMIN creado");
                    return role;
                });

            // Crear rol USER si no existe
            Role userRole = roleRepository.findByName("USER")
                .orElseGet(() -> {
                    Role role = new Role("USER", "Usuario estándar");
                    roleRepository.save(role);
                    System.out.println("✅ Rol USER creado");
                    return role;
                });

            // Crear usuario admin por defecto solo si no existe
            if (userRepository.findByEmail("admin@example.com").isEmpty()) {
                User defaultAdmin = new User(
                    "Administrador",
                    "admin@example.com",
                    passwordEncoder.encode("admin123"),
                    false,  // requiresPasswordChange = false para admin
                    adminRole
                );
                userRepository.save(defaultAdmin);
                System.out.println("✅ Usuario administrador creado:");
                System.out.println("   Email: admin@example.com");
                System.out.println("   Password: admin123");
                System.out.println("   Role: ADMIN");
            }

            // Crear usuario estándar por defecto solo si no existe
            if (userRepository.findByEmail("user@example.com").isEmpty()) {
                User defaultUser = new User(
                    "Usuario Estándar",
                    "user@example.com",
                    passwordEncoder.encode("user123"),
                    false,  // requiresPasswordChange = false
                    userRole
                );
                userRepository.save(defaultUser);
                System.out.println("✅ Usuario estándar creado:");
                System.out.println("   Email: user@example.com");
                System.out.println("   Password: user123");
                System.out.println("   Role: USER");
            }
        };
    }
}
