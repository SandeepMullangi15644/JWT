package com.example.JwtApp.service;

import com.example.JwtApp.DTO.AuthRequest;
import com.example.JwtApp.DTO.AuthResponse;
import com.example.JwtApp.DTO.addUserDto;
import com.example.JwtApp.exception.UserNotFoundException;
import com.example.JwtApp.exception.UsernameAlreadyExistsException;
import com.example.JwtApp.model.User;
import com.example.JwtApp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final PasswordResetTokenService tokenService;

    public void registerEmployer(AuthRequest request) {
        var user = User.builder()
                .username(request.getUsername())
                .password(encoder.encode(request.getPassword()))
                .role("EMPLOYER")
                .build();
        repo.save(user);
    }

    public void addUser(addUserDto request, String employerUsername) {
        User employer = repo.findByUsername(employerUsername)
                .orElseThrow(() -> new RuntimeException("Employer not found"));

        User user = User.builder()
                .username(request.getUsername())
                .role("USER")
                .employerId(employer.getId())
                .first_name(request.getFirst_name())
                .last_name(request.getLast_name())
                .address(request.getAddress())
                .email(request.getEmail())
                .plan_id(request.getPlan_id())
                .date_of_birth(request.getDate_of_birth())
                .create_time(request.getCreate_time())
                .create_By(request.getCreate_By())
                .build();


        String tempPassword = UUID.randomUUID().toString();
        user.setTemporaryPassword(encoder.encode(tempPassword));
        user.setTempPasswordExpiry(LocalDateTime.now().plusHours(1));

        repo.save(user);

        emailService.sendTemporaryPasswordEmail(request.getEmail(), request.getUsername(), tempPassword);

        tokenService.generateToken(user);
    }
    public AuthResponse login(AuthRequest request) {
        Authentication auth = authManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        List<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        String token = jwtUtil.generateToken(request.getUsername(), roles);
        return new AuthResponse(token, roles);
    }

    public User getUserByUsername(String username){
            return repo.findByUsername(username)
                    .orElseThrow(() -> new UserNotFoundException(username));
        }
}
