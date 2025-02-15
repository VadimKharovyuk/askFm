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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
    SELECT m FROM Message m
    WHERE m.id IN (
        SELECT MAX(m2.id)
        FROM Message m2
        WHERE m2.sender.id = :userId OR m2.recipient.id = :userId
        GROUP BY CASE
            WHEN m2.sender.id = :userId THEN m2.recipient.id
            ELSE m2.sender.id
        END
    )
    ORDER BY m.timestamp DESC
""")
    List<Message> findConversations(Long userId);


    @Query("""
    SELECT m FROM Message m 
    WHERE (m.sender.id = :userId AND m.recipient.id = :recipientId)
    OR (m.sender.id = :recipientId AND m.recipient.id = :userId)
    ORDER BY m.timestamp DESC
""")
    Page<Message> findConversationHistory(
            @Param("userId") Long userId,
            @Param("recipientId") Long recipientId,
            Pageable pageable
    );




    Long countByRecipientUsernameAndReadFalse(String username);

    // Если нужно для конкретного отправителя:
    Long countByRecipientUsernameAndSenderIdAndReadFalse(String username, Long senderId);

    @Modifying
    @Query("""
    UPDATE Message m 
    SET m.read = true 
    WHERE m.recipient.id = :userId 
    AND m.sender.id = :senderId 
    AND m.read = false
""")
    void markMessagesAsRead(@Param("userId") Long userId, @Param("senderId") Long senderId);


    //проверка чатов админ
    @Query("SELECT m FROM Message m WHERE m.recipient.id = :userId OR m.sender.id = :userId")
    Page<Message> findByRecipientOrSender(Long userId, Pageable pageable);

    @Query("SELECT m FROM Message m WHERE " +
            "(m.sender.id = :user1Id AND m.recipient.id = :user2Id) OR " +
            "(m.sender.id = :user2Id AND m.recipient.id = :user1Id) " +
            "ORDER BY m.timestamp DESC")
    List<Message> findConversationBetweenUsers(Long user1Id, Long user2Id);

    Page<Message> findByContentContainingIgnoreCase(String keyword, Pageable pageable);
}