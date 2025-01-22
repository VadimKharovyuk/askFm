package com.example.askfm.repository;

import com.example.askfm.enums.UserRole;
import com.example.askfm.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<User> searchByUsername(String query);

    @Query("SELECT COUNT(u) FROM User u")
    long countAllUsers();

    // Метод для подсчета пользователей с определенной ролью
    long countByRole(UserRole role);

    // Метод для поиска пользователей по имени (игнорируя регистр)
    List<User> findByUsernameContainingIgnoreCase(String username);

    long countByCreatedAtBefore(LocalDateTime date);

}
