package com.example.springreact.repository;

import com.example.springreact.model.Role;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
  Optional<Role> findByName(String name);

  @Query("SELECT r FROM Role r ORDER BY CASE WHEN r.name = 'ADMIN' THEN 1 ELSE 0 END, r.name")
  List<Role> findAllOrderedAdminLast();
}
