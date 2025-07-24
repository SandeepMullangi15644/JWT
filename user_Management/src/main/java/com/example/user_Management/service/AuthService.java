package com.example.user_Management.service;

import com.example.user_Management.DTO.AuthRequest;
import com.example.user_Management.DTO.AuthResponse;
import com.example.user_Management.DTO.ChangePasswordRequest;
import com.example.user_Management.DTO.addUserDto;
import com.example.user_Management.exception.UserNotFoundException;
import com.example.user_Management.model.User;
import com.example.user_Management.repository.UserRepository;
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

        user.setMustChangePassword(true);

        repo.save(user);

        emailService.sendTemporaryPasswordEmail(request.getEmail(), request.getUsername(), tempPassword);

        tokenService.generateToken(user);
    }

    public AuthResponse login(AuthRequest request) {
        User user = repo.findByUsername(request.getUsername())
                .orElseThrow(() -> new UserNotFoundException(request.getUsername()));

        boolean isTempPasswordValid = user.getTemporaryPassword() != null &&
                user.getTempPasswordExpiry() != null &&
                LocalDateTime.now().isBefore(user.getTempPasswordExpiry()) &&
                encoder.matches(request.getPassword(), user.getTemporaryPassword());

        boolean isRegularPasswordValid = encoder.matches(request.getPassword(), user.getPassword());

        if (!isTempPasswordValid && !isRegularPasswordValid) {
            throw new BadCredentialsException("Invalid credentials or temporary password expired.");
        }

        // Authenticate using Spring Security
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
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

    public void changePassword(ChangePasswordRequest request) {
        if (request.getUsername() == null || request.getOldPassword() == null || request.getNewPassword() == null) {
            throw new IllegalArgumentException("username, oldPassword and newPassword must be provided");
        }
        User user = repo.findByUsername(request.getUsername())
                .orElseThrow(() -> new UserNotFoundException(request.getUsername()));

        boolean isTempValid = user.getTemporaryPassword() != null &&
                user.getTempPasswordExpiry() != null &&
                LocalDateTime.now().isBefore(user.getTempPasswordExpiry()) &&
                encoder.matches(request.getOldPassword(), user.getTemporaryPassword());

        boolean isRegularPasswordValid = encoder.matches(request.getOldPassword(), user.getPassword());

        if (!isTempValid && !isRegularPasswordValid) {
            throw new BadCredentialsException("Old password is incorrect.");
        }

        user.setPassword(encoder.encode(request.getNewPassword()));
        user.setTemporaryPassword(null);
        user.setTempPasswordExpiry(null);
        user.setMustChangePassword(false);

        repo.save(user);
    }

}
