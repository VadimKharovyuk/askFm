package com.example.askfm.repository;

import com.example.askfm.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    boolean existsByUsername(String username);
    Optional<Admin> findByUsername(String username);

}
