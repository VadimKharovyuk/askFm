package com.example.askfm.controller;

import com.example.askfm.dto.EventCreateDto;
import com.example.askfm.dto.EventListDto;
import com.example.askfm.dto.EventResponseDto;
import com.example.askfm.dto.EventUpdateDto;
import com.example.askfm.enums.EventCategory;
import com.example.askfm.enums.ParticipationType;
import com.example.askfm.exception.EventNotFoundException;
import com.example.askfm.exception.UserNotFoundException;
import com.example.askfm.model.User;
import com.example.askfm.service.EventAttendanceService;
import com.example.askfm.service.EventService;
import com.example.askfm.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
@Slf4j
@Controller
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventViewController {
    private final EventService eventService;
    private final UserService userService;
    private final EventAttendanceService attendanceService;

    @GetMapping("/create")
    public String showCreateEventForm(Model model) {
        model.addAttribute("eventForm", new EventCreateDto());
        model.addAttribute("categories", EventCategory.values());
        return "events/create";
    }


    @PostMapping("/create")
    public String createEvent(@Valid @ModelAttribute("eventForm") EventCreateDto eventForm,
                              BindingResult bindingResult,
                              @AuthenticationPrincipal UserDetails userDetails,
                              Model model,
                              RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            log.debug("Validation errors: {}", bindingResult.getAllErrors());
            model.addAttribute("categories", EventCategory.values()); // Добавляем категории обратно
            return "events/create";
        }

        try {
            User user = userService.findByUsername(userDetails.getUsername());
            log.debug("Found user: {}", user);

            EventResponseDto createdEvent = eventService.createEvent(eventForm, user.getId());


            redirectAttributes.addFlashAttribute("successMessage", "Подію успішно створено!");
            return "redirect:/events";
        } catch (Exception e) {
            log.error("Error creating event", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Помилка створення події: " + e.getMessage());
            return "redirect:/events/create";
        }
    }

    @GetMapping
    public String getAllEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) EventCategory category,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        User currentUser = userService.findByUsername(userDetails.getUsername());
        model.addAttribute("currentUser", currentUser);

        Page<EventListDto> events;

        // Handle search criteria
        if (city != null && !city.isEmpty()) {
            events = eventService.getEventsByCity(city, page, size);
        } else if (location != null && !location.isEmpty()) {
            events = eventService.getEventsByLocation(location, page, size);
        } else if (category != null) {
            events = eventService.getEventsByCategory(category, page, size);
        } else {
            events = eventService.getAllEvents(page, size);
        }

        model.addAttribute("searchCity", city);
        model.addAttribute("searchLocation", location);
        model.addAttribute("selectedCategory", category);

        // Add category counts
        Map<EventCategory, Long> categoryCount = eventService.getEventCountByCategory();
        model.addAttribute("categoryCount", categoryCount);

        model.addAttribute("events", events);
        return "events/list";
    }


    @GetMapping("/my")
    public String getMyEvents(Model model,
                              @AuthenticationPrincipal UserDetails userDetails,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "20") int size) {

        User currentUser = userService.findByUsername(userDetails.getUsername());

        // И передаем его в сервис
        Map<String, Page<EventListDto>> userEvents = eventService.getAllUserEvents(currentUser.getId(), page, size);

        model.addAttribute("createdEvents", userEvents.get("created"));
        model.addAttribute("goingEvents", userEvents.get("going"));
        model.addAttribute("interestedEvents", userEvents.get("interested"));
        model.addAttribute("currentPage", page);
        return "events/my-events";
    }

    @GetMapping("/city/{city}")
    public String getEventsByCity(@PathVariable String city,
                                  Model model,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "20") int size) {
        Page<EventListDto> events = eventService.getEventsByCity(city, page, size);
        model.addAttribute("events", events);
        model.addAttribute("city", city);
        model.addAttribute("currentPage", page);
        return "events/city-events";
    }

    @GetMapping("/upcoming")
    public String getUpcomingEvents(Model model,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "20") int size) {
        Page<EventListDto> events = eventService.getUpcomingEvents(page, size);
        model.addAttribute("events", events);
        model.addAttribute("currentPage", page);
        return "events/upcoming";
    }

    @GetMapping("/{id}")
    public String getEventById(@PathVariable Long id, Model model,
                               @AuthenticationPrincipal UserDetails userDetails) {
        try {
            EventResponseDto event = eventService.getEventById(id);
            model.addAttribute("event", event);

            // Проверяем, является ли текущий пользователь создателем события
            boolean isCreator = userDetails != null &&
                    userDetails.getUsername().equals(event.getCreator().getName());
            model.addAttribute("isCreator", isCreator);

            // Добавляем статус участия текущего пользователя
            if (userDetails != null) {
                ParticipationType status = attendanceService.getUserParticipationStatus(id, userDetails.getUsername());
                model.addAttribute("userParticipationStatus", status);
            }

            // Получаем другие события (например, той же категории или того же создателя)
            List<EventListDto> otherEvents = eventService.getRelatedEvents(id, 6); // получаем 6 похожих событий
            model.addAttribute("otherEvents", otherEvents);

            return "events/event-details";
        } catch (EventNotFoundException e) {
            return "error/404";
        }
    }


    @PostMapping("/{id}/attend")
    public String attendEvent(@PathVariable("id") Long eventId,
                              @RequestParam("status") ParticipationType status,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        try {
            String username = userDetails.getUsername();
            attendanceService.attendEvent(eventId, username, status);
            String message = status == ParticipationType.GOING ?
                    "You are going to this event" :
                    "You are interested in this event";
            redirectAttributes.addFlashAttribute("success", message);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/events/" + eventId;
    }

    @PostMapping("/{id}/cancel")
    public String cancelAttendance(@PathVariable("id") Long eventId,
                                   @AuthenticationPrincipal UserDetails userDetails,
                                   RedirectAttributes redirectAttributes) {
        try {
            String username = userDetails.getUsername();
            attendanceService.cancelAttendance(eventId, username);
            redirectAttributes.addFlashAttribute("success", "Your attendance has been cancelled");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/events/" + eventId;
    }

    @PostMapping("/{id}/delete")
    public String deleteEvent(@PathVariable Long id,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        try {
            eventService.deleteEvent(id, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("success", "Event successfully deleted");
            return "redirect:/events/my";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/events/" + id;
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model) {
        try {
            EventResponseDto event = eventService.getEventById(id);
            if (!event.getCreator().getName().equals(userDetails.getUsername())) {
                throw new IllegalStateException("Only creator can edit the event");
            }

            model.addAttribute("event", event);
            return "events/event-edit";
        } catch (Exception e) {
            return "redirect:/events/" + id;
        }
    }

    @PostMapping("/{id}/edit")
    public String updateEvent(@PathVariable Long id,
                              @Valid @ModelAttribute("event") EventUpdateDto updateDto,
                              BindingResult bindingResult,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "events/event-edit";
        }

        try {
            eventService.updateEvent(id, updateDto, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("success", "Event successfully updated");
            return "redirect:/events/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "events/event-edit";
        }
    }


}