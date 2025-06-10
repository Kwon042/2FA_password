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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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

        LocalDateTime now = LocalDateTime.now();

        if (user.getTwoFactorLockTime() != null) {
            LocalDateTime lockExpires = user.getTwoFactorLockTime().plusMinutes(1);

            if (now.isBefore(lockExpires)) {
                // 아직 잠금 시간 안 지남 → 그대로 2FA 페이지로
                log.info("2FA 잠금 상태 유지 중: {}", username);
                model.addAttribute("username", username);
                model.addAttribute("twoFactorLockTime", user.getTwoFactorLockTime());
                return "2fa";
            } else {
                // 잠금 해제 → 잠금 해제 후 비밀번호 재설정 요청 페이지로 리다이렉트
                log.info("2FA 잠금 해제됨, 비밀번호 재설정 페이지로 이동: {}", username);
                user.setTwoFactorLockTime(null);
                user.setTwoFactorAttempts(0);
                userRepository.save(user);

                return "redirect:/password-reset?from2fa=true&username=" + URLEncoder.encode(username, StandardCharsets.UTF_8);
            }
        }

        // 잠금 아님 → 평소처럼 2FA 페이지
        log.info("2FA 정상 진입: {}", username);
        model.addAttribute("username", username);
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
