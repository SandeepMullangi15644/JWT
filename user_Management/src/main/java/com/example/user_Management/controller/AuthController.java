package com.example.user_Management.controller;

import com.example.user_Management.DTO.AuthRequest;
import com.example.user_Management.DTO.AuthResponse;
import com.example.user_Management.DTO.ChangePasswordRequest;
import com.example.user_Management.DTO.addUserDto;
import com.example.user_Management.model.User;
import com.example.user_Management.service.AuthService;
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

    @PostMapping("/change-password")
    public String changePassword(@RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return "Password changed successfully!";
    }

    @GetMapping("/user")
    public User getUserByUsername(@RequestParam String username){
        return authService.getUserByUsername(username);
    }
}
