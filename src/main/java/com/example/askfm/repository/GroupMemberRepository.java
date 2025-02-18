package com.example.askfm.repository;

import com.example.askfm.model.Group;
import com.example.askfm.model.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    Optional<GroupMember> findByGroupAndUser_Username(Group group, String username);
}
