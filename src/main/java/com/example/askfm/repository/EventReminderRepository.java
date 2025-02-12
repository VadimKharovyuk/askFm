package com.example.askfm.repository;

import com.example.askfm.model.Event;
import com.example.askfm.model.EventReminder;
import com.example.askfm.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventReminderRepository extends JpaRepository<EventReminder, Long> {
    Optional<EventReminder> findByEventAndUser(Event event, User user);
    List<EventReminder> findByEventAndSentAtBetween(Event event, LocalDateTime start, LocalDateTime end);
    boolean existsByEventAndUserAndSent(Event event, User user, boolean sent);
}