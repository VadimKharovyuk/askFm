package com.example.askfm.repository;

import com.example.askfm.enums.NewsletterStatus;
import com.example.askfm.model.Newsletter;
import com.example.askfm.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NewsletterRepository extends JpaRepository<Newsletter, Long> {
    // Находит все рассылки со статусом SCHEDULED и временем отправки до указанного
    List<Newsletter> findByStatusAndScheduledTimeBefore(
            NewsletterStatus status,
            LocalDateTime scheduledTime
    );

    // Находит все рассылки определенного статуса
    List<Newsletter> findByStatus(NewsletterStatus status);

    // Находит все рассылки, созданные конкретным админом
    List<Newsletter> findByCreatedBy(User admin);

    // Находит все рассылки за определенный период
    List<Newsletter> findByScheduledTimeBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    // Подсчет количества рассылок по статусу
    @Query("SELECT n.status, COUNT(n) FROM Newsletter n GROUP BY n.status")
    List<Object[]> countByStatus();

    // Поиск по теме или содержанию (для админ-панели)
    @Query("SELECT n FROM Newsletter n WHERE " +
            "LOWER(n.subject) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(n.content) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Newsletter> search(@Param("search") String search);
}
