package com.example.askfm.repository;

import com.example.askfm.model.Question;
import com.example.askfm.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {


    List<Question> findByRecipientIdAndAnsweredTrue(Long recipientId);
    List<Question> findByRecipientIdAndAnsweredFalse(Long recipientId);
    List<Question> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);
    List<Question> findByRecipientId(Long recipientId);

}
