package com.example.user_Management.DTO;


import lombok.Data;

@Data
public class AuthRequest {
    private String username;
    private String password;
}
