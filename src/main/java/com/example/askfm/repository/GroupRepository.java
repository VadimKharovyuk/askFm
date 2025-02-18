package com.example.askfm.repository;

import com.example.askfm.enums.GroupCategory;
import com.example.askfm.enums.GroupRole;
import com.example.askfm.model.Group;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    Page<Group> findByCategory(GroupCategory category, Pageable pageable);

    // Находит группы, где пользователь является участником
    @Query("SELECT DISTINCT g FROM Group g JOIN g.members m WHERE m.user.username = :username")
    Page<Group> findByMembersUserUsername(String username, Pageable pageable);

    // Находит группы, где пользователь имеет определенные роли
    @Query("SELECT DISTINCT g FROM Group g JOIN g.members m WHERE m.user.username = :username AND m.role IN :roles")
    Page<Group> findByMembersUserUsernameAndMembersRoleIn(
            String username,
            List<GroupRole> roles,
            Pageable pageable
    );

}
