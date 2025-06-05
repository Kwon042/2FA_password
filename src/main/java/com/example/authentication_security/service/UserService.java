package com.example.authentication_security.service;

import com.example.authentication_security.domain.User;
import com.example.authentication_security.domain.UserRole;
import com.example.authentication_security.dto.UserResponse;
import com.example.authentication_security.dto.UserSignupRequest;
import com.example.authentication_security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse signup(UserSignupRequest dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("이미 존재하는 사용자명입니다.");
        }

        // 이메일 중복 확인
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("이미 등록된 이메일입니다.");
        }

        // username에 "admin"이 포함되면 ROLE_ADMIN 부여
        UserRole role = dto.getUsername().toLowerCase().contains("admin")
                ? UserRole.ROLE_ADMIN
                : UserRole.ROLE_USER;

        User user = User.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .email(dto.getEmail())
                .role(role)
                .build();

        User saved = userRepository.save(user);
        return new UserResponse(saved.getId(), saved.getUsername(), saved.getEmail(), saved.getRole());
    }

    @Transactional
    public void deleteUserByUsername(String username) {
        if (!userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("해당 사용자가 존재하지 않습니다.");
        }
        userRepository.deleteByUsername(username);
    }

}
