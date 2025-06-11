package com.example.authentication_security.global;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        String errorMessage = "로그인 실패";

        if (exception instanceof BadCredentialsException) {
            errorMessage = "비밀번호가 틀렸습니다.";
        } else if (exception.getMessage() != null) {
            errorMessage = exception.getMessage();
        }

        // 에러 메시지를 세션에 저장해서 로그인 페이지에서 보여주기
        request.getSession().setAttribute("loginError", errorMessage);

        // 로그인 페이지로 다시 리다이렉트
        response.sendRedirect("/login?error");
    }
}
