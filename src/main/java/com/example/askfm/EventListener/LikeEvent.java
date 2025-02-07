package com.example.askfm.EventListener;

import com.example.askfm.model.Post;
import com.example.askfm.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LikeEvent {
    private final User liker;
    private final Post post;
}
