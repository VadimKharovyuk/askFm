package com.example.askfm.controller;

import com.example.askfm.model.Event;
import com.example.askfm.model.User;
import com.example.askfm.service.EmailService;
import com.example.askfm.service.EventReminderScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/reminders")
public class EventReminderTestController {
    private final EventReminderScheduler reminderScheduler;
    private final EmailService emailService;

    @PostMapping("/test-send")
    public ResponseEntity<String> testReminderSending() {
        try {
            reminderScheduler.scheduledReminderProcessing();
            return ResponseEntity.ok("Reminder processing started successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    @PostMapping("/test-email")
    public ResponseEntity<String> testEmail(@RequestParam String email) {
        try {
            Event testEvent = Event.builder()
                    .title("Test Event")
                    .eventDate(LocalDateTime.now().plusDays(1))
                    .city("Test City")
                    .address("Test Address")
                    .build();

            User testUser = User.builder()
                    .email(email)
                    .username("Test User")
                    .build();

            emailService.sendEventReminder(testUser, testEvent);
            return ResponseEntity.ok("Test email queued");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}