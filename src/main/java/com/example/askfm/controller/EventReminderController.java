package com.example.askfm.controller;

import com.example.askfm.dto.EventReminderDto;
import com.example.askfm.model.Event;
import com.example.askfm.service.EmailService;
import com.example.askfm.service.EventReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
@Slf4j
@Controller
@RequestMapping("/reminders")
@RequiredArgsConstructor
public class EventReminderController {
    private final EventReminderService reminderService;
    private final EmailService emailService;
    @GetMapping
    public String getAllReminders(Model model,
                                  @RequestParam(required = false) Long eventId,
                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
                                  @RequestParam(defaultValue = "all") String status) {

        List<EventReminderDto> reminders;

        if (eventId != null && startDate != null && endDate != null) {
            Event event = Event.builder().id(eventId).build();
            reminders = reminderService.getRemindersByEvent(event, startDate, endDate);
        } else {
            // В зависимости от параметра status показываем разные напоминания
            switch (status) {
                case "sent" -> reminders = reminderService.getSentReminders();
                case "unsent" -> reminders = reminderService.getUnsentReminders();
                default -> reminders = reminderService.getAllReminders();
            }
        }

        // Статистика
        int queueSize = emailService.getQueueSize();
        long sentCount = reminders.stream().filter(EventReminderDto::isSent).count();
        long unsentCount = reminders.stream().filter(r -> !r.isSent()).count();

        model.addAttribute("reminders", reminders);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("eventId", eventId);
        model.addAttribute("currentStatus", status);
        model.addAttribute("queueSize", queueSize);
        model.addAttribute("sentCount", sentCount);
        model.addAttribute("unsentCount", unsentCount);

        return "reminders/list";
    }

    @PostMapping("/{reminderId}/mark-sent")
    @ResponseBody
    public ResponseEntity<String> markReminderAsSent(@PathVariable Long reminderId) {
        try {
            reminderService.markAsSent(reminderId);
            return ResponseEntity.ok("Reminder marked as sent");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/delete-old")
    @ResponseBody
    public ResponseEntity<String> deleteOldReminders() {
        try {
            LocalDateTime oldDate = LocalDateTime.now().minusDays(7); // удаляем старше 7 дней
            reminderService.deleteOldReminders(oldDate);
            return ResponseEntity.ok("Old reminders deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @DeleteMapping("/delete-all")
    @ResponseBody
    public ResponseEntity<String> deleteAllReminders() {
        try {
            reminderService.deleteAll();
            log.info("All reminders have been deleted");
            return ResponseEntity.ok("All reminders deleted successfully");
        } catch (Exception e) {
            log.error("Failed to delete all reminders: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}