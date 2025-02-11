package com.example.askfm.repository;

import com.example.askfm.enums.ParticipationType;
import com.example.askfm.model.EventAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventAttendanceRepository extends JpaRepository<EventAttendance, Long> {
    // Изменяем метод для поиска по username вместо userId
    Optional<EventAttendance> findByEventIdAndUserUsername(Long eventId, String username);

    @Query("SELECT COUNT(ea) FROM EventAttendance ea WHERE ea.event.id = :eventId AND ea.status = :status")
    long countByEventIdAndStatus(@Param("eventId") Long eventId, @Param("status") ParticipationType status);

    // Поиск всех записей об участии для конкретного события
    List<EventAttendance> findByEventId(Long eventId);


    // Удаление всех записей об участии для конкретного события
    void deleteByEventId(Long eventId);

    // Поиск всех записей об участии для конкретного пользователя
    List<EventAttendance> findByUserId(Long userId);
}