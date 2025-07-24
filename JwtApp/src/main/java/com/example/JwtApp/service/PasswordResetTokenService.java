package com.example.JwtApp.service;

import com.example.JwtApp.model.PasswordResetToken;
import com.example.JwtApp.model.User;
import com.example.JwtApp.repository.PasswordResetTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetTokenService {
    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    public PasswordResetToken generateToken(User user) {
        String tokenValue = UUID.randomUUID().toString();
        PasswordResetToken token = new PasswordResetToken();
        token.setToken(tokenValue);
        token.setExpiryDate(LocalDateTime.now().plusHours(1));
        token.setUser(user);
        token.setUsed(false);
        return tokenRepository.save(token);
    }

    public PasswordResetToken validateToken(String tokenValue) {
        PasswordResetToken token = tokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new RuntimeException("Invalid token"));
        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expired");
        }
        if (token.isUsed()) {
            throw new RuntimeException("Token already used");
        }
        return token;
    }

    public void markTokenAsUsed(PasswordResetToken token) {
        token.setUsed(true);
        tokenRepository.save(token);
    }
}

