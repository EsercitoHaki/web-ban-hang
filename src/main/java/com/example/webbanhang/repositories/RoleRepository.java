package com.example.webbanhang.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.webbanhang.models.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
}
