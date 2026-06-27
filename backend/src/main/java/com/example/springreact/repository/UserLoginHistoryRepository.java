package com.example.springreact.repository;

import com.example.springreact.model.UserLoginHistory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserLoginHistoryRepository extends JpaRepository<UserLoginHistory, Long> {

  Optional<UserLoginHistory> findTopByUserIdOrderByLoginAtDesc(Long userId);

  List<UserLoginHistory> findByUserIdOrderByLoginAtDesc(Long userId, Pageable pageable);

  @Query("SELECT h.user.id, MAX(h.loginAt) FROM UserLoginHistory h GROUP BY h.user.id")
  List<Object[]> findLastLoginPerUser();

  void deleteByUserId(Long userId);
}
