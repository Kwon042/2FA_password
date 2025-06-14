package com.example.authentication_security.service;

import com.example.authentication_security.domain.TwoFactorStatus;
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
import java.util.Optional;
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

    // 2fa 시도: 코드 인증하는 과정
    public TwoFactorStatus verifyTwoFactorCode(String username, String code) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        LocalDateTime now = LocalDateTime.now();

        // 1. 잠금 상태 확인
        if (user.getTwoFactorLockTime() != null) {
            LocalDateTime lockExpires = user.getTwoFactorLockTime().plusMinutes(1);
            if (now.isBefore(lockExpires)) {
                return TwoFactorStatus.LOCKED;
            } else {
                user.setTwoFactorLockTime(null);
                user.setTwoFactorAttempts(0);
                userRepository.save(user);
            }
        }

        // 2. 코드 만료 또는 없음
        if (user.getTwoFactorCode() == null || user.getTwoFactorExpiry() == null ||
                user.getTwoFactorExpiry().isBefore(now)) {
            return TwoFactorStatus.FAILURE;
        }

        // 3. 코드 불일치
        if (!user.getTwoFactorCode().equals(code)) {
            int attempts = user.getTwoFactorAttempts() == null ? 1 : user.getTwoFactorAttempts() + 1;
            user.setTwoFactorAttempts(attempts);

            if (attempts >= 3) {
                user.setTwoFactorLockTime(now);  // 1분 잠금
                userRepository.save(user);

                // 비밀번호 재설정 코드 자동 전송
                generateResetPasswordCode(user);

                return TwoFactorStatus.LOCKED;
            }

            userRepository.save(user);
            return TwoFactorStatus.FAILURE;
        }

        // 4. 코드 일치 (성공)
        user.setTwoFactorAttempts(0);
        user.setTwoFactorLockTime(null);
        user.setTwoFactorCode(null);
        user.setTwoFactorExpiry(null);
        userRepository.save(user);

        return TwoFactorStatus.SUCCESS;
    }


    public void generateAndSendTwoFactorCode(User user) {
        String code = String.format("%06d", new Random().nextInt(999999));
        user.setTwoFactorCode(code);
        user.setTwoFactorExpiry(LocalDateTime.now().plusMinutes(5)); // 5분 유효
        userRepository.save(user);

        emailService.sendEmail(user.getEmail(), "Your 2FA Code", "Code: " + code);
    }

    public void lockAccount(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setAccountLocked(true);
        userRepository.save(user);
    }

    public void sendResetPasswordCode(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (user.getResetPasswordLockTime() != null) {
            LocalDateTime unlockTime = user.getResetPasswordLockTime().plusMinutes(1);
            if (LocalDateTime.now().isBefore(unlockTime)) {
                throw new IllegalStateException("1분 잠금 상태입니다. 잠시 후 다시 시도하세요.");
            } else {
                user.setResetPasswordLockTime(null);
                userRepository.save(user);
            }
        }

        generateResetPasswordCode(user); // 개선된 코드
    }

    private void generateResetPasswordCode(User user) {
        String code = String.format("%06d", new Random().nextInt(999999));
        user.setResetPasswordCode(code);
        user.setResetPasswordCodeExpiry(LocalDateTime.now().plusMinutes(10));
        user.setResetPasswordLockTime(LocalDateTime.now()); // 1분 제한 적용
        userRepository.save(user);

        String subject = "비밀번호 재설정 코드 안내";
        String text = "안녕하세요.\n비밀번호 재설정 코드는 " + code + " 입니다.\n10분 이내에 사용해주세요.";
        emailService.sendEmail(user.getEmail(), subject, text);
    }

    public boolean verifyResetCodeAndChangePassword(String username, String code, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (user.getResetPasswordCode() == null || user.getResetPasswordCodeExpiry() == null) {
            return false; // 코드 없음
        }

        if (user.getResetPasswordCodeExpiry().isBefore(LocalDateTime.now())) {
            return false; // 코드 만료
        }

        if (!user.getResetPasswordCode().equals(code)) {
            return false; // 코드 불일치
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setAccountLocked(false);  // 잠금 해제
        user.setResetPasswordCode(null);
        user.setResetPasswordCodeExpiry(null);
        userRepository.save(user);

        return true;
    }

    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

}
