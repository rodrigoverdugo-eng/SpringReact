package com.example.springreact.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_login_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false)
  private LocalDateTime loginAt;

  @Column(length = 45)
  private String ipAddress;

  public UserLoginHistory(User user, LocalDateTime loginAt, String ipAddress) {
    this.user = user;
    this.loginAt = loginAt;
    this.ipAddress = ipAddress;
  }
}
