package com.example.authentication_security.controller;

import com.example.authentication_security.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class PasswordResetController {

    private final UserService userService;

    // 비밀번호 재설정 요청 폼 보여주기
    @GetMapping("/password-reset")
    public String showPasswordResetRequest(@RequestParam(required = false) String from2fa,
                                           @RequestParam(required = false) String username,
                                           Model model) {
        if ("true".equals(from2fa)) {
            model.addAttribute("message", "3회 인증 실패로 인해 비밀번호 재설정을 진행해주세요.");
            model.addAttribute("username", username);
        }
        return "password-reset-request";
    }

    // 이메일로 6자리 코드 전송 요청 처리
    @PostMapping("/password-reset/request")
    public String sendResetCode(@RequestParam String username, RedirectAttributes redirectAttributes) {
        try {
            userService.sendResetPasswordCode(username);
            redirectAttributes.addAttribute("username", username);
            return "redirect:/password-reset/verify";
        } catch (UsernameNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", "해당 사용자명이 존재하지 않습니다.");
            return "redirect:/password-reset/request";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/password-reset/request";
        }
    }

    @GetMapping("/password-reset/verify")
    public String showVerifyForm(@RequestParam String username, Model model) {
        model.addAttribute("username", username);
        return "password-reset-verify";
    }

    // 6자리 코드와 새 비밀번호 입력 폼 제출 처리
    @PostMapping("/password-reset/confirm")
    public String confirmReset(@RequestParam String username,
                               @RequestParam String code,
                               @RequestParam String newPassword,
                               Model model) {
        boolean success = userService.verifyResetCodeAndChangePassword(username, code, newPassword);

        if (success) {
            model.addAttribute("message", "비밀번호가 성공적으로 변경되었습니다. 로그인하세요.");
            return "login-form"; // 로그인 페이지로 이동
        } else {
            model.addAttribute("username", username);
            model.addAttribute("error", "코드가 올바르지 않거나 만료되었습니다.");
            return "password-reset-verify";
        }
    }
}

