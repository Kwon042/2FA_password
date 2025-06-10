package com.example.authentication_security.controller;

import com.example.authentication_security.domain.User;
import com.example.authentication_security.repository.UserRepository;
import com.example.authentication_security.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class TwoFactorViewController {

    private final UserService userService;
    private final UserRepository userRepository;

    // 2FA 입력 폼 화면
    @GetMapping("/2fa")
    public String show2faForm(@RequestParam String username, Model model) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        model.addAttribute("username", username);
        model.addAttribute("twoFactorLockTime", user.getTwoFactorLockTime()); // 잠금 시작 시간 추가
        return "2fa";
    }

    @PostMapping("/2fa/verify")
    public String verifyCode(@RequestParam String username,
                             @RequestParam String code,
                             Model model) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        LocalDateTime now = LocalDateTime.now();

        if (user.getTwoFactorLockTime() != null) {
            LocalDateTime lockExpires = user.getTwoFactorLockTime().plusMinutes(1);
            if (now.isBefore(lockExpires)) {
                // 아직 잠금 시간 안 끝남
                model.addAttribute("username", username);
                model.addAttribute("twoFactorLockTime", user.getTwoFactorLockTime());
                return "2fa";
            } else {
                // 잠금 시간 지남 → 잠금 해제 및 시도 횟수 초기화 후 저장
                user.setTwoFactorLockTime(null);
                user.setTwoFactorAttempts(0);
                userRepository.save(user);
            }
        }

        boolean success = userService.verifyTwoFactorCode(username, code);
        if (success) {
            return "login_success_form";
        } else {
            model.addAttribute("username", username);
            model.addAttribute("error", "인증 코드가 올바르지 않거나 만료되었습니다.");
            return "2fa";
        }
    }

}
