package com.example.askfm.repository;

import com.example.askfm.model.Group;
import com.example.askfm.model.GroupMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.nio.channels.FileChannel;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    Optional<GroupMember> findByGroupAndUser_Username(Group group, String username);



    @Query("SELECT gm FROM GroupMember gm WHERE gm.group = :group AND LOWER(gm.user.username) LIKE LOWER(:search)")
    Page<GroupMember> findByGroupAndUsernameLike(Group group, String search, Pageable pageable);

    // Поиск всех участников группы с пагинацией
    @Query("SELECT gm FROM GroupMember gm WHERE gm.group.id = :groupId")
    Page<GroupMember> findByGroupId(@Param("groupId") Long groupId, Pageable pageable);

    // Поиск участников по имени
    @Query("SELECT gm FROM GroupMember gm WHERE gm.group.id = :groupId " +
            "AND LOWER(gm.user.username) LIKE LOWER(concat('%', :searchTerm, '%'))")
    Page<GroupMember> searchByUsername(
            @Param("groupId") Long groupId,
            @Param("searchTerm") String searchTerm,
            Pageable pageable
    );
}
