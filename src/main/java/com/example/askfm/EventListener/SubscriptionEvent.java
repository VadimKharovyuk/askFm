package com.example.askfm.EventListener;

import com.example.askfm.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SubscriptionEvent {
    private final User subscriber;
    private final User targetUser;
}
