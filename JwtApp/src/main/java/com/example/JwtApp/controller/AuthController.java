package com.example.JwtApp.controller;

import com.example.JwtApp.DTO.AuthRequest;
import com.example.JwtApp.DTO.AuthResponse;
import com.example.JwtApp.DTO.addUserDto;
import com.example.JwtApp.model.User;
import com.example.JwtApp.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin("*")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public String registerEmployer(@RequestBody AuthRequest request) {
        authService.registerEmployer(request);
        return "Employer registered successfully!";
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request) {
        return authService.login(request);
    }

    @PostMapping("/add-user")
    public String addUser(@RequestBody addUserDto req, Authentication authentication) {
        String employerUsername = authentication.getName();
        authService.addUser(req, employerUsername);
        return "User added successfully!";
    }

    @PostMapping("/user-login")
    public AuthResponse userLogin(@RequestBody AuthRequest request) {
        return authService.login(request);
    }
    @GetMapping("/user")
    public User getUserByUsername(@RequestParam String username){
        return authService.getUserByUsername(username);
    }
}
