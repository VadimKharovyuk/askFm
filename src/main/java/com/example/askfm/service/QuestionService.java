package com.example.askfm.service;

import com.example.askfm.dto.QuestionDTO;
import com.example.askfm.model.Answer;
import com.example.askfm.model.Question;
import com.example.askfm.model.User;
import com.example.askfm.repository.QuestionRepository;
import com.example.askfm.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;

    public Question askQuestion(@Valid QuestionDTO questionDTO, User author) {
        User recipient = userRepository.findByUsername(questionDTO.getRecipientUsername())
                .orElseThrow(() -> new IllegalArgumentException("Recipient not found"));

        Question question = Question.builder()
                .content(questionDTO.getContent())
                .anonymous(questionDTO.isAnonymous())
                .author(questionDTO.isAnonymous() ? null : author)
                .recipient(recipient)
                .createdAt(LocalDateTime.now())
                .answered(false)
                .build();

        return questionRepository.save(question);
    }

    public Answer answerQuestion(Long questionId, String content, User author) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));

        if (!question.getRecipient().equals(author)) {
            throw new IllegalArgumentException("Only the recipient can answer this question");
        }

        Answer answer = Answer.builder()
                .content(content)
                .question(question)
                .author(author)
                .createdAt(LocalDateTime.now())
                .build();

        question.setAnswer(answer);
        question.setAnswered(true);
        questionRepository.save(question);

        return answer;
    }
}
