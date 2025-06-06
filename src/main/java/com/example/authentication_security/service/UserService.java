package com.example.authentication_security.service;

import com.example.authentication_security.domain.User;
import com.example.authentication_security.domain.UserRole;
import com.example.authentication_security.dto.UserResponse;
import com.example.authentication_security.dto.UserSignupRequest;
import com.example.authentication_security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;


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

    public boolean verifyTwoFactorCode(String username, String code) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (user.getTwoFactorCode() == null || user.getTwoFactorExpiry() == null) {
            return false;
        }

        if (user.getTwoFactorExpiry().isBefore(LocalDateTime.now())) {
            return false;  // 코드 만료
        }

        if (!user.getTwoFactorCode().equals(code)) {
            return false;  // 코드 불일치
        }

        // 검증 성공하면 코드 초기화 (재사용 방지)
        user.setTwoFactorCode(null);
        user.setTwoFactorExpiry(null);
        userRepository.save(user);

        return true;
    }


    public void generateAndSendTwoFactorCode(User user) {
        String code = String.format("%06d", new Random().nextInt(999999));
        user.setTwoFactorCode(code);
        user.setTwoFactorExpiry(LocalDateTime.now().plusMinutes(5)); // 5분 유효
        userRepository.save(user);

        emailService.sendEmail(user.getEmail(), "Your 2FA Code", "Code: " + code);
    }

}
