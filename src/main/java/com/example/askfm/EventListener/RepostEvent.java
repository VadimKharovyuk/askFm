package com.example.askfm.EventListener;

import com.example.askfm.model.Repost;
import com.example.askfm.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RepostEvent {
    private final User repostUser;
    private final Repost repost;
}
