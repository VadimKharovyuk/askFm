package com.example.askfm.repository;

import com.example.askfm.model.User;
import com.example.askfm.model.Visit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface VisitRepository extends JpaRepository<Visit, Long> {

    @Query("SELECT v FROM Visit v " +
            "WHERE v.visitedUser = :user " +
            "AND v.visitedAt >= :startDate " +
            "AND v.visitedAt <= :endDate " +
            "AND v.visitedAt = (SELECT MAX(v2.visitedAt) FROM Visit v2 " +
            "                   WHERE v2.visitor = v.visitor " +
            "                   AND v2.visitedUser = v.visitedUser " +
            "                   AND v2.visitedAt >= :startDate " +
            "                   AND v2.visitedAt <= :endDate) " +
            "ORDER BY v.visitedAt DESC")
    Page<Visit> findVisitorsLastDay(
            @Param("user") User user,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    boolean existsByVisitorAndVisitedUserAndVisitedAtAfter(
            User visitor,
            User visitedUser,
            LocalDateTime after
    );

    @Query("SELECT COUNT(DISTINCT v.visitor) FROM Visit v " +
            "WHERE v.visitedUser = :user " +
            "AND v.visitedAt >= :startDate " +
            "AND v.visitedAt <= :endDate")
    Long countUniqueVisitorsLastDay(
            @Param("user") User user,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );



}
