package com.example.askfm.repository;

import com.example.askfm.enums.EventCategory;
import com.example.askfm.enums.ParticipationType;
import com.example.askfm.model.Event;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static org.hibernate.jpa.HibernateHints.HINT_FETCH_SIZE;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    @Query(value = "SELECT e FROM Event e WHERE e.city = :city",
            countQuery = "SELECT COUNT(e) FROM Event e WHERE e.city = :city")
    Page<Event> findByCity(@Param("city") String city, Pageable pageable);

    @Query(value = "SELECT e FROM Event e WHERE e.eventDate > :date",
            countQuery = "SELECT COUNT(e) FROM Event e WHERE e.eventDate > :date")
    Page<Event> findByEventDateAfter(@Param("date") LocalDateTime date, Pageable pageable);

    @Query(value = "SELECT e FROM Event e WHERE e.creator.id = :userId",
            countQuery = "SELECT COUNT(e) FROM Event e WHERE e.creator.id = :userId")
    Page<Event> findByCreatorId(@Param("userId") Long userId, Pageable pageable);

    @Query(value = "SELECT e FROM Event e JOIN e.attendances a " +
            "WHERE a.user.id = :userId AND a.status = :status",
            countQuery = "SELECT COUNT(DISTINCT e) FROM Event e JOIN e.attendances a " +
                    "WHERE a.user.id = :userId AND a.status = :status")
    Page<Event> findByAttendancesUserIdAndAttendancesStatus(
            @Param("userId") Long userId,
            @Param("status") ParticipationType status,
            Pageable pageable
    );

    Page<Event> findByCategory(EventCategory category, Pageable pageable);

    @Query("SELECT COUNT(e) FROM Event e WHERE e.category = :category")
    long countByCategory(@Param("category") EventCategory category);

    Page<Event> findByCategoryAndIdNot(EventCategory category, Long eventId, PageRequest eventDate);

    Page<Event> findByLocation(String location, Pageable pageable);

    List<Event> findByEventDateBetween(LocalDateTime start, LocalDateTime end);

}
