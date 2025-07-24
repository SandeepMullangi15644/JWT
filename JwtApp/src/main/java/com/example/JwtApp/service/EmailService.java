package com.example.JwtApp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    public void sendTemporaryPasswordEmail(String to, String username, String tempPassword) {
        String subject = "Your Temporary Password";
        String text = "Hello " + username + ",\n\nYour temporary password is: " + tempPassword +
                "\nThis password will expire in 1 hour. Please change it after logging in.\nThanks;\nTeam Cigna.";
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
}

