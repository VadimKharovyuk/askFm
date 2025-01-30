package com.example.askfm.service;

import com.example.askfm.dto.VisitDTO;
import com.example.askfm.maper.VisitMapper;
import com.example.askfm.model.User;
import com.example.askfm.model.Visit;
import com.example.askfm.repository.VisitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Duration;
@Service
@RequiredArgsConstructor
public class VisitService {
    private final VisitRepository visitRepository;
    private final VisitMapper visitMapper;
    private static final Duration MIN_VISIT_INTERVAL = Duration.ofMinutes(5);

    public Page<VisitDTO> getRecentVisitors(User user, int page, int size) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusHours(24);

        Pageable pageable = PageRequest.of(page, size);

        Page<Visit> visitsPage = visitRepository.findVisitorsLastDay(
                user,
                startDate,
                endDate,
                pageable
        );

        return visitsPage.map(visitMapper::toDTO);
    }

    public void recordVisit(User visitor, User visitedUser) {
        if (visitor == null || visitor.getId().equals(visitedUser.getId())) {
            return;
        }

        LocalDateTime cutoffTime = LocalDateTime.now().minus(MIN_VISIT_INTERVAL);

        // Проверяем, был ли недавний визит от этого пользователя
        boolean recentVisit = visitRepository.existsByVisitorAndVisitedUserAndVisitedAtAfter(
                visitor,
                visitedUser,
                cutoffTime
        );

        // Сохраняем только если прошло достаточно времени с последнего визита
        if (!recentVisit) {
            Visit visit = Visit.builder()
                    .visitor(visitor)
                    .visitedUser(visitedUser)
                    .visitedAt(LocalDateTime.now())
                    .build();
            visitRepository.save(visit);
        }
    }

    public Long getUniqueVisitorsCount(User user) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusHours(24);

        return visitRepository.countUniqueVisitorsLastDay(user, startDate, endDate);
    }
}