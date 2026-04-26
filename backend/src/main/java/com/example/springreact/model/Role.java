package com.example.springreact.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 10)
  private String name;

  @Column(length = 100)
  private String descripcion;

  public Role(String name) {
    this.name = name;
  }

  public Role(String name, String descripcion) {
    this.name = name;
    this.descripcion = descripcion;
  }
}
