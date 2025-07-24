package com.example.JwtApp.DTO;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class AuthResponse {
    private String token;
    private List<String> role;

}
