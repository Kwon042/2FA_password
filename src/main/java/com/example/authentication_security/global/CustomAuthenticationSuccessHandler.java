package com.example.authentication_security.global;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JavaMailSender mailSender;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        // 1. 인증코드 생성 및 메일 전송
        String code = generateVerificationCode();
        String userEmail = ((UserDetails) authentication.getPrincipal()).getUsername();

        sendVerificationEmail(userEmail, code);

        // 2. 세션에 코드와 인증 정보 저장
        HttpSession session = request.getSession();
        session.setAttribute("2fa_code", code);
        session.setAttribute("2fa_authentication", authentication);

        System.out.println("2FA 인증 코드: " + code);

        // 3. 2단계 인증 페이지로 리다이렉트
        response.sendRedirect("/2fa");
    }

    private String generateVerificationCode() {
        Random random = new Random();
        return String.valueOf(100000 + random.nextInt(900000)); // 6자리
    }

    private void sendVerificationEmail(String toEmail, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Your 2FA Verification Code");
        message.setText("Your verification code is: " + code);
        mailSender.send(message);
    }
}
