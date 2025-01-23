package com.example.askfm.service;

import com.example.askfm.dto.AnswerResponseDto;
import com.example.askfm.dto.QuestionDTO;
import com.example.askfm.dto.QuestionRequestDto;
import com.example.askfm.dto.QuestionResponseDto;
import com.example.askfm.exception.NotFoundException;
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
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;



    @Transactional
    public QuestionResponseDto askQuestion(QuestionRequestDto requestDto, Long authorId) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        User recipient = userRepository.findById(requestDto.getRecipientId())
                .orElseThrow(() -> new RuntimeException("Recipient not found"));

        Question question = Question.builder()
                .content(requestDto.getContent())
                .anonymous(requestDto.isAnonymous())
                .author(author)
                .recipient(recipient)
                .createdAt(LocalDateTime.now())
                .answered(false)
                .build();

        Question savedQuestion = questionRepository.save(question);
        return convertToDto(savedQuestion);
    }

    public List<QuestionResponseDto> getQuestionsForUser(Long userId) {
        return questionRepository.findByRecipientId(userId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public QuestionResponseDto getQuestionById(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new NotFoundException("Question not found with id: " + questionId));

        return convertToDto(question);
    }
    @Transactional
    public void deleteQuestion(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        questionRepository.delete(question);
    }


    // Получить вопросы, на которые пользователь ответил
    public List<QuestionResponseDto> getAnsweredQuestions(Long userId) {
        return questionRepository.findByRecipientIdAndAnsweredTrue(userId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Получить вопросы, на которые пользователь еще не ответил
    public List<QuestionResponseDto> getUnansweredQuestions(Long userId) {
        return questionRepository.findByRecipientIdAndAnsweredFalse(userId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }


    private QuestionResponseDto convertToDto(Question question) {
        return QuestionResponseDto.builder()
                .id(question.getId())
                .content(question.getContent())
                .author(question.isAnonymous() ? null : question.getAuthor())
                .authorUsername(question.isAnonymous() ? "Аноним" : question.getAuthor().getUsername())
                .recipient(question.getRecipient())
                .recipientUsername(question.getRecipient().getUsername())
                .anonymous(question.isAnonymous())
                .createdAt(question.getCreatedAt())
                .answered(question.isAnswered())
                .answer(question.getAnswer() != null ?
                        AnswerResponseDto.builder()
                                .id(question.getAnswer().getId())
                                .content(question.getAnswer().getContent())
                                .authorUsername(question.getAnswer().getAuthor().getUsername())
                                .createdAt(question.getAnswer().getCreatedAt())
                                .build() : null)
                .likes(0) // Здесь можно добавить реальное количество лайков, если есть такая функциональность
                .build();
    }

//
//
//
//    public Question askQuestion(@Valid QuestionDTO questionDTO, User author) {
//        User recipient = userRepository.findByUsername(questionDTO.getRecipientUsername())
//                .orElseThrow(() -> new IllegalArgumentException("Recipient not found"));
//
//        Question question = Question.builder()
//                .content(questionDTO.getContent())
//                .anonymous(questionDTO.isAnonymous())
//                .author(questionDTO.isAnonymous() ? null : author)
//                .recipient(recipient)
//                .createdAt(LocalDateTime.now())
//                .answered(false)
//                .build();
//
//        return questionRepository.save(question);
//    }
//
//    public Answer answerQuestion(Long questionId, String content, User author) {
//        Question question = questionRepository.findById(questionId)
//                .orElseThrow(() -> new IllegalArgumentException("Question not found"));
//
//        if (!question.getRecipient().equals(author)) {
//            throw new IllegalArgumentException("Only the recipient can answer this question");
//        }
//
//        Answer answer = Answer.builder()
//                .content(content)
//                .question(question)
//                .author(author)
//                .createdAt(LocalDateTime.now())
//                .build();
//
//        question.setAnswer(answer);
//        question.setAnswered(true);
//        questionRepository.save(question);
//
//        return answer;
//    }
//
//    public List<Question> getAnsweredQuestions(User user) {
//        return questionRepository.findByRecipientAndAnsweredOrderByCreatedAtDesc(user, true);
//    }
//
//    public List<Question> getUnansweredQuestions(User user) {
//        return questionRepository.findByRecipientAndAnsweredOrderByCreatedAtDesc(user, false);
//    }
//
//    public List<Question> getAllQuestions(User user) {
//        return questionRepository.findByRecipientOrderByCreatedAtDesc(user);
//    }
}
