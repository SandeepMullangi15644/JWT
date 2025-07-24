package com.example.user_Management.service;

import com.example.user_Management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository repo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = repo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        String passwordToUse = user.getPassword();

        if(user.getTemporaryPassword() != null &&
        user.getTempPasswordExpiry() != null &&
            LocalDateTime.now().isBefore(user.getTempPasswordExpiry())){
            passwordToUse =user.getTemporaryPassword();
        }
        if (passwordToUse ==null){
            throw new IllegalArgumentException("password cannot be null");
        }
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(passwordToUse)
                .roles(user.getRole())
                .build();
    }
}


