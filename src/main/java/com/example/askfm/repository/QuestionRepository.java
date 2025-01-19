package com.example.askfm.repository;

import com.example.askfm.model.Question;
import com.example.askfm.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByRecipientOrderByCreatedAtDesc(User recipient);
    List<Question> findByRecipientAndAnsweredOrderByCreatedAtDesc(User recipient, boolean answered);

}
