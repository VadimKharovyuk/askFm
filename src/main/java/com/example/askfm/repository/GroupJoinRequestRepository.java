package com.example.askfm.repository;

import com.example.askfm.enums.JoinRequestStatus;
import com.example.askfm.model.Group;
import com.example.askfm.model.GroupJoinRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupJoinRequestRepository extends JpaRepository<GroupJoinRequest, Long> {
    // Поиск заявки конкретного пользователя в конкретную группу с определенным статусом
    Optional<GroupJoinRequest> findByGroupAndUser_UsernameAndStatus(
            Group group,
            String username,
            JoinRequestStatus status
    );

    // Поиск всех заявок в группу с определенным статусом
    List<GroupJoinRequest> findByGroupAndStatus(Group group, JoinRequestStatus status);

    // Поиск всех заявок пользователя
    List<GroupJoinRequest> findByUser_Username(String username);

    // Проверка существования заявки
    boolean existsByGroupAndUser_UsernameAndStatus(
            Group group,
            String username,
            JoinRequestStatus status
    );

}
