package com.example.askfm.service;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.askfm.model.User;
import com.example.askfm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
@RequiredArgsConstructor
@Service
public class MentionService {
    private final UserRepository userRepository;

    public Set<User> extractMentions(String content) {
        Set<User> mentions = new HashSet<>();
        Pattern pattern = Pattern.compile("@(\\w+)");
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            String username = matcher.group(1);
            userRepository.findByUsername(username)
                    .ifPresent(mentions::add);
        }
        return mentions;
    }
}