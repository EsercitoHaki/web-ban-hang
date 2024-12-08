package com.example.webbanhang.repositories;

import com.example.webbanhang.models.Role;
import com.example.webbanhang.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByGoogleAccountId(Integer googleAccountId);
    Optional<User> findByGoogleAccountId(Integer googleAccountId);

    boolean existsByPhoneNumber(String phoneNumber);
    Optional<User> findByPhoneNumber(String phoneNumber) throws UsernameNotFoundException;
}

