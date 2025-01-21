package com.example.askfm.service;

import com.example.askfm.dto.AdminRegistrationDTO;
import com.example.askfm.enums.AdminRole;
import com.example.askfm.model.Admin;
import com.example.askfm.repository.AdminRepository;
import com.example.askfm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public Admin registerAdmin(AdminRegistrationDTO registrationDTO) {
        if (adminRepository.existsByUsername(registrationDTO.getUsername())) {
            throw new IllegalArgumentException("Admin already exists");
        }

        Admin admin = Admin.builder()
                .username(registrationDTO.getUsername())
                .password(passwordEncoder.encode(registrationDTO.getPassword()))
                .role(AdminRole.SUPER_ADMIN)
                .createdAt(LocalDateTime.now())
                .build();

        return adminRepository.save(admin);
    }


    public Admin authenticateAdmin(String username, String password) {
        Admin admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordEncoder.matches(password, admin.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        return admin;
    }

    public Admin findByUsername(String username) {
        return adminRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found"));
    }

//    public long getTotalNewsCount() {
//        return newsRepository.count();
//    }

    public long getTotalUsersCount() {
        return userRepository.count();
    }
//
//    public List<News> getRecentNews() {
//        return newsRepository.findTop5ByOrderByCreatedAtDesc();
//    }
}
