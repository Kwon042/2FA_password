package com.example.authentication_security.repository;

import com.example.authentication_security.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    // 사용자명 중복 검사 메서드
    boolean existsByUsername(String username);

    // 이메일 중복 검사 메서드
    boolean existsByEmail(String email);

    void deleteByUsername(String username);

}
