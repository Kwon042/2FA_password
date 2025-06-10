package com.example.authentication_security.controller;

import com.example.authentication_security.dto.TwoFactorDTO;
import com.example.authentication_security.dto.UserResponse;
import com.example.authentication_security.dto.UserSignupRequest;
import com.example.authentication_security.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signup(@RequestBody @Valid UserSignupRequest userSignupRequest) {
        UserResponse userResponse = userService.signup(userSignupRequest);
        return ResponseEntity.ok(userResponse);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteUser(Authentication authentication) {
        String username = authentication.getName();
        userService.deleteUserByUsername(username);
        return ResponseEntity.noContent().build();
    }

    // REST 방식으로 2FA 코드 검증 > db 기반
    @PostMapping("/2fa/verify")
    public ResponseEntity<?> verifyTwoFactorCode(@RequestBody TwoFactorDTO dto) {
        boolean success = userService.verifyTwoFactorCode(dto.getUsername(), dto.getCode());
        if (success) {
            return ResponseEntity.ok("2FA 인증 성공");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("2FA 인증 실패");
        }
    }


}
