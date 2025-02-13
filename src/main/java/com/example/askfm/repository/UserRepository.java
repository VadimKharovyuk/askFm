package com.example.askfm.repository;

import com.example.askfm.enums.UserRole;
import com.example.askfm.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

    @Query("""

            SELECT u FROM User u 
        WHERE u.username != :currentUsername 
        AND u.id NOT IN (
            SELECT s.subscribedTo.id 
            FROM Subscription s 
            WHERE s.subscriber.username = :currentUsername
        )
        ORDER BY 
            SIZE(u.subscribers) * 0.4 + 
            SIZE(u.askedQuestions) * 0.3 + 
            SIZE(u.receivedQuestions) * 0.3 DESC,
            u.createdAt DESC
        LIMIT :limit
        """)
    List<User> findSuggestedUsers(String currentUsername, int limit);



    // Новый метод с пагинацией
    @Query(value = """
        SELECT u FROM User u 
        WHERE u.username != :currentUsername 
        AND u.id NOT IN (
            SELECT s.subscribedTo.id 
            FROM Subscription s 
            WHERE s.subscriber.username = :currentUsername
        )
        ORDER BY 
            SIZE(u.subscribers) * 0.4 + 
            SIZE(u.askedQuestions) * 0.3 + 
            SIZE(u.receivedQuestions) * 0.3 DESC,
            u.createdAt DESC
        """)
    Page<User> findPaginatedSuggestedUsers(String currentUsername, Pageable pageable);

    User findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.id = :userId")
    Optional<User> findByIdEager(@Param("userId") Long userId);

}

