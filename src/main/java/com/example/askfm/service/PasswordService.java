package com.example.askfm.service;

import com.example.askfm.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class PasswordService {

    private final UserService userService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    public void resetPassword(String email) {
        User user = userService.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found with email: " + email);
        }

        String newPassword = generateSecurePassword();
        user.setPassword(passwordEncoder.encode(newPassword));
        userService.save(user);

        sendPasswordResetEmail(user.getEmail(), newPassword);
    }

    private String generateSecurePassword() {
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            password.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return password.toString();
    }

    private void sendPasswordResetEmail(String email, String newPassword) {
        String subject = "Восстановление пароля";
        String text = String.format("""
            Здравствуйте!
            
            Ваш новый пароль: %s
            
            Пожалуйста, измените его при следующем входе в систему.
            
            С уважением,
            Команда поддержки
            """, newPassword);

        emailService.queueEmail(email, subject, text);
    }
}