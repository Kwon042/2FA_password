package com.example.authentication_security.config;

import com.example.authentication_security.global.CustomAuthenticationFailureHandler;
import com.example.authentication_security.global.CustomAuthenticationSuccessHandler;
import com.example.authentication_security.security.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
// CustomUserDetailsService를 사용하여 인증을 처리하고, 로그인 및 권한 관련 설정을 추가
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final CustomAuthenticationSuccessHandler customAuthSuccessHandler;
    private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;
//    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authorize -> authorize
                    .requestMatchers("/", "/login", "/signup", "/api/**", "/js/**", "/css/**", "/2fa", "/2fa/verify").permitAll()
                    .anyRequest().authenticated()
            )
            .userDetailsService(customUserDetailsService)
            .formLogin(form -> form
                    .loginPage("/login")
                    .permitAll()
                    .successHandler(customAuthSuccessHandler)  // 여기에 2단계 인증 핸들러 등록
                    .failureHandler(customAuthenticationFailureHandler)
            )
            .logout(logout -> logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/")
                    .permitAll()
            )
            .exceptionHandling(exception -> exception
                    .authenticationEntryPoint((request, response, authException) -> {
                        if (request.getRequestURI().startsWith("/api")) {
                            response.setContentType("application/json");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write("{\"error\": \"Unauthorized - Please login\"}");
                        } else {
                            response.sendRedirect("/login");
                        }
                    })
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // AuthenticationManager는 따로 정의 필요
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
