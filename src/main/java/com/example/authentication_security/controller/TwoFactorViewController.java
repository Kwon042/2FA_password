package com.example.authentication_security.controller;

import com.example.authentication_security.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class TwoFactorViewController {

    private final UserService userService;

    // 2FA 입력 폼 화면
    @GetMapping("/2fa")
    public String show2faForm(@RequestParam String username, Model model) {
        System.out.println("✅ 2FA 페이지 요청: " + username);
        model.addAttribute("username", username);
        return "2fa";  // templates/2fa.html 을 렌더링
    }

    @PostMapping("/2fa/verify")
    public String verifyCode(@RequestParam String username,
                             @RequestParam String code,
                             Model model) {
        boolean success = userService.verifyTwoFactorCode(username, code);
        if (success) {
            return "login_success_form";  // 인증 성공 후 이동할 페이지
        } else {
            model.addAttribute("username", username);
            model.addAttribute("error", "인증 코드가 올바르지 않거나 만료되었습니다.");
            return "2fa"; // 다시 2fa 폼 보여주기
        }
    }
}
