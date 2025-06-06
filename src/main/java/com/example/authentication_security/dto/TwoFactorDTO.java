package com.example.authentication_security.dto;

import lombok.Data;

@Data
public class TwoFactorDTO {

    private String username;
    private String code;
}
