package com.example.springreact.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(nullable = false, unique = true, length = 100)
    private String email;
    
    @Column(nullable = false, length = 100)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    
    @Column(nullable = false)
    private Boolean requiresPasswordChange = true;
    
    @Column(nullable = false)
    private Boolean vigencia = true;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;
    
    // Constructor sin ID para creación de nuevos usuarios
    public User(String name, String email, String password, Role role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.requiresPasswordChange = true;
        this.vigencia = true;
        this.role = role;
    }

    public User(String name, String email, String password, Boolean requiresPasswordChange, Role role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.requiresPasswordChange = requiresPasswordChange;
        this.vigencia = Boolean.TRUE;
        this.role = role;
    }
}
