package com.example.askfm.repository;

import com.example.askfm.model.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnswerRepository extends JpaRepository<Answer, Long> {
    List<Answer> findByAuthorId(Long authorId);

}
