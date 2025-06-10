package com.example.authentication_security.global;

import com.example.authentication_security.repository.UserRepository;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        String code = generateVerificationCode();
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();

        // 1. DB에 코드 저장
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setTwoFactorCode(code);
            user.setTwoFactorExpiry(LocalDateTime.now().plusMinutes(5));
            userRepository.save(user);
        });

        // 2. 이메일 전송
        sendVerificationEmail(username, code);

        String encodedUsername = URLEncoder.encode(username, StandardCharsets.UTF_8);
        response.sendRedirect("/2fa?username=" + encodedUsername); // 또는 API 클라이언트에선 JSON 응답
    }

    private String generateVerificationCode() {
        return String.valueOf(100000 + new Random().nextInt(900000));
    }

    private void sendVerificationEmail(String toEmail, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Your 2FA Code");
        message.setText("Your verification code is: " + code);
        mailSender.send(message);
    }
}
