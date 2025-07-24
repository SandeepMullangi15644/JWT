package com.example.JwtApp.DTO;


import lombok.Data;

@Data
public class AuthRequest {
    private String username;
    private String password;
}
