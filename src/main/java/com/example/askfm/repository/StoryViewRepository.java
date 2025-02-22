package com.example.askfm.repository;

import com.example.askfm.model.Story;
import com.example.askfm.model.StoryView;
import com.example.askfm.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoryViewRepository extends JpaRepository<StoryView, Long> {
    List<StoryView> findByStory(Story story);

    boolean existsByStoryAndUser(Story story, User user);

}
