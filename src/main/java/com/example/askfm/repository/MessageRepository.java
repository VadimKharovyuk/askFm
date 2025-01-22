package com.example.askfm.repository;//package com.example.askfm.repository;
//
//import com.example.askfm.model.Message;
//import org.springframework.data.domain.Pageable;
//
//import org.springframework.data.domain.Page;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//
//import java.util.List;
//
//
//public interface MessageRepository extends JpaRepository<Message, Long> {
//    @Query("SELECT m FROM Message m " +
//            "WHERE m.sender.id = :userId OR m.recipient.id = :userId " +
//            "ORDER BY m.timestamp DESC")
//    Page<Message> findRecentMessages(@Param("userId") Long userId, Pageable pageable);
//
//    @Query("SELECT DISTINCT m FROM Message m " +
//            "WHERE m.sender.id = :userId OR m.recipient.id = :userId " +
//            "ORDER BY m.timestamp DESC")
//    List<Message> findConversations(@Param("userId") Long userId);
//
//    @Query("SELECT m FROM Message m WHERE " +
//            "(m.sender.id = :userId AND m.recipient.id = :recipientId) OR " +
//            "(m.sender.id = :recipientId AND m.recipient.id = :userId) " +
//            "ORDER BY m.timestamp DESC")
//    List<Message> findConversationHistory(@Param("userId") Long userId,
//                                          @Param("recipientId") Long recipientId);
//}


import com.example.askfm.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("""
        SELECT m FROM Message m 
        WHERE m.sender.id = :userId OR m.recipient.id = :userId 
        ORDER BY m.timestamp DESC
    """)
    Page<Message> findRecentMessages(@Param("userId") Long userId, Pageable pageable);

    @Query("""
        SELECT m1 FROM Message m1 
        WHERE (m1.sender.id = :userId OR m1.recipient.id = :userId) 
        AND m1.timestamp = (
            SELECT MAX(m2.timestamp) FROM Message m2 
            WHERE (m2.sender.id = :userId AND m2.recipient.id = m1.recipient.id) 
            OR (m2.sender.id = m1.recipient.id AND m2.recipient.id = :userId)
            OR (m2.sender.id = m1.sender.id AND m2.recipient.id = :userId)
            OR (m2.sender.id = :userId AND m2.recipient.id = m1.sender.id)
        )
        ORDER BY m1.timestamp DESC
    """)
    List<Message> findConversations(@Param("userId") Long userId);

    @Query("""
        SELECT m FROM Message m 
        WHERE (m.sender.id = :userId AND m.recipient.id = :recipientId) 
        OR (m.sender.id = :recipientId AND m.recipient.id = :userId) 
        ORDER BY m.timestamp ASC
    """)
    List<Message> findConversationHistory(@Param("userId") Long userId, @Param("recipientId") Long recipientId);
}