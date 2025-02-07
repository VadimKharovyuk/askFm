package com.example.askfm.EventListener;

import com.example.askfm.model.Post;
import com.example.askfm.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommentEvent {
    private final User commenter;
    private final Post post;
}