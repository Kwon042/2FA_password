package com.example.authentication_security.controller;

import com.example.authentication_security.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class TwoFactorAuthController {

    private final UserService userService;

    // 2FA 입력 폼 페이지 보여주기
    @GetMapping("/2fa")
    public String showTwoFactorForm() {
        return "2fa";  // templates/2fa.html 렌더링
    }

    // 2FA 코드 검증 처리
    @PostMapping("/2fa/verify")
    public String verifyTwoFactorCode(@RequestParam String username,
                                      @RequestParam String code,
                                      HttpSession session,
                                      Model model) {

        String sessionCode = (String) session.getAttribute("2fa_code");
        Authentication savedAuth = (Authentication) session.getAttribute("2fa_authentication");

        if (sessionCode == null || savedAuth == null || !savedAuth.getName().equals(username)) {
            model.addAttribute("error", "인증 세션이 만료되었거나 올바르지 않습니다.");
            model.addAttribute("username", username);
            return "2fa";
        }

        if (!sessionCode.equals(code)) {
            model.addAttribute("error", "인증 코드가 올바르지 않습니다.");
            model.addAttribute("username", username);
            return "2fa";
        }

        // 2FA 인증 성공: 세션에 저장된 인증 정보를 SecurityContext에 등록
        SecurityContextHolder.getContext().setAuthentication(savedAuth);

        // 2FA 관련 세션 데이터 삭제
        session.removeAttribute("2fa_code");
        session.removeAttribute("2fa_authentication");

        return "redirect:/";  // 인증 성공 후 홈으로 이동
    }
}
