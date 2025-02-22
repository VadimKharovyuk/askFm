package com.example.askfm.repository;

import com.example.askfm.enums.ReactionType;
import com.example.askfm.model.Story;
import com.example.askfm.model.StoryReaction;
import com.example.askfm.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface StoryReactionRepository extends JpaRepository<StoryReaction, Long> {

    /**
     * Найти реакцию по истории и пользователю
     * @param story история
     * @param user пользователь
     * @return Optional с реакцией, если она существует
     */
    Optional<StoryReaction> findByStoryAndUser(Story story, User user);

    /**
     * Найти все реакции для истории
     * @param story история
     * @return список реакций
     */
    List<StoryReaction> findByStory(Story story);

    /**
     * Проверить существование реакции от пользователя к истории
     * @param story история
     * @param user пользователь
     * @return true если реакция существует
     */
    boolean existsByStoryAndUser(Story story, User user);

    /**
     * Подсчитать количество реакций определенного типа для истории
     * @param story история
     * @param reactionType тип реакции
     * @return количество реакций
     */
    long countByStoryAndReactionType(Story story, ReactionType reactionType);

    /**
     * Удалить все реакции для истории
     * @param story история
     */
    void deleteByStory(Story story);
}
