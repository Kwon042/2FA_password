package com.example.authentication_security.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor // 매개 변수가 없는 기본 생성자를 생성
@AllArgsConstructor // 모든 필드를 매개변수로 받는 생성자를 생성
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserRole role = UserRole.ROLE_USER;

    @Column(name = "two_factor_enabled")
    private boolean twoFactorEnabled;

    @Column(name = "two_factor_code")
    private String twoFactorCode;

    @Column(name = "two_factor_expiry")
    private LocalDateTime twoFactorExpiry;

    // 2fa 로그인 횟수 제한
    @Column(name = "two_factor_attempts")
    private Integer twoFactorAttempts;

    @Column(name = "two_factor_lock_time")
    private LocalDateTime twoFactorLockTime;

    @Column(name = "account_locked")
    private Boolean accountLocked;

    @Column(name = "reset_password_code")
    private String resetPasswordCode;

    @Column(name = "reset_password_lock_time")
    private LocalDateTime resetPasswordLockTime;

    @Column(name = "reset_password_code_expiry")
    private LocalDateTime resetPasswordCodeExpiry;

}
