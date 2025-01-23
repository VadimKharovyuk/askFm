package com.example.askfm.service;

import com.example.askfm.dto.AnswerRequestDto;
import com.example.askfm.dto.AnswerResponseDto;

import com.example.askfm.exception.NotFoundException;
import com.example.askfm.model.Answer;
import com.example.askfm.model.Question;
import com.example.askfm.model.User;

import com.example.askfm.repository.AnswerRepository;
import com.example.askfm.repository.QuestionRepository;
import com.example.askfm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;

    @Transactional
    public AnswerResponseDto createAnswer(AnswerRequestDto requestDto, Long authorId) {
        // Находим пользователя, который отвечает
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Находим вопрос
        Question question = questionRepository.findById(requestDto.getQuestionId())
                .orElseThrow(() -> new NotFoundException("Question not found"));

        // Проверяем, что отвечает тот, кому был задан вопрос
        if (!question.getRecipient().getId().equals(authorId)) {
            throw new IllegalStateException("Only the recipient can answer this question");
        }

        // Проверяем, что на вопрос ещё не ответили
        if (question.isAnswered()) {
            throw new IllegalStateException("Question is already answered");
        }

        // Проверяем, что ответ не пустой
        if (requestDto.getContent() == null || requestDto.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Answer content cannot be empty");
        }

        // Создаем новый ответ
        Answer answer = Answer.builder()
                .content(requestDto.getContent().trim())
                .question(question)
                .author(author)
                .createdAt(LocalDateTime.now())
                .build();

        // Обновляем статус вопроса
        question.setAnswered(true);
        question.setAnswer(answer);

        // Сохраняем ответ и обновленный вопрос
        Answer savedAnswer = answerRepository.save(answer);
        questionRepository.save(question);

        return convertToDto(savedAnswer);
    }

    @Transactional
    public AnswerResponseDto updateAnswer(Long answerId, String newContent, Long userId) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new NotFoundException("Answer not found"));

        // Проверяем, что редактирует автор ответа
        if (!answer.getAuthor().getId().equals(userId)) {
            throw new IllegalStateException("Only the author can edit this answer");
        }

        // Проверяем новый контент
        if (newContent == null || newContent.trim().isEmpty()) {
            throw new IllegalArgumentException("Answer content cannot be empty");
        }

        // Обновляем содержимое ответа
        answer.setContent(newContent.trim());
        Answer updatedAnswer = answerRepository.save(answer);

        return convertToDto(updatedAnswer);
    }

    @Transactional
    public void deleteAnswer(Long answerId, Long userId) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new NotFoundException("Answer not found"));

        // Проверяем, что удаляет автор ответа
        if (!answer.getAuthor().getId().equals(userId)) {
            throw new IllegalStateException("Only the author can delete this answer");
        }

        // Обновляем статус вопроса
        Question question = answer.getQuestion();
        question.setAnswered(false);
        question.setAnswer(null);

        // Сохраняем изменения в вопросе и удаляем ответ
        questionRepository.save(question);
        answerRepository.delete(answer);
    }

    public AnswerResponseDto getAnswerById(Long answerId) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new NotFoundException("Answer not found"));
        return convertToDto(answer);
    }

    public List<AnswerResponseDto> getAnswersByAuthor(Long authorId) {
        List<Answer> answers = answerRepository.findByAuthorId(authorId);
        return answers.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }



    private AnswerResponseDto convertToDto(Answer answer) {
        return AnswerResponseDto.builder()
                .id(answer.getId())
                .content(answer.getContent())
                .authorUsername(answer.getAuthor().getUsername())
                .createdAt(answer.getCreatedAt())
                .build();
    }
}