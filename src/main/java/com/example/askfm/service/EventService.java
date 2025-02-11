package com.example.askfm.service;

import com.example.askfm.dto.EventCreateDto;
import com.example.askfm.dto.EventListDto;
import com.example.askfm.dto.EventResponseDto;
import com.example.askfm.dto.EventUpdateDto;
import com.example.askfm.enums.EventCategory;
import com.example.askfm.enums.ParticipationType;
import com.example.askfm.exception.EventCreationException;
import com.example.askfm.exception.EventNotFoundException;
import com.example.askfm.exception.ImageProcessingException;
import com.example.askfm.exception.UserNotFoundException;
import com.example.askfm.maper.EventMapper;
import com.example.askfm.model.Event;
import com.example.askfm.model.EventAttendance;
import com.example.askfm.model.User;
import com.example.askfm.repository.EventAttendanceRepository;
import com.example.askfm.repository.EventRepository;
import com.example.askfm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EventService {
    private static final int MAX_PAGE_SIZE = 100;
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final UserRepository userRepository;
    private final EventAttendanceRepository attendanceRepository;



    public EventResponseDto createEvent(EventCreateDto createDto, Long creatorId) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + creatorId));

        try {
            Event event = eventMapper.toEntity(createDto, creator);
            Event savedEvent = eventRepository.save(event);
            return eventMapper.toDto(savedEvent, null);
        } catch (IOException e) {
            throw new EventCreationException("Error processing event photo: " + e.getMessage());
        }
    }



    public Page<EventListDto> getAllEvents(int page, int size) {
        size = Math.min(size, MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, size, Sort.by("eventDate").descending());
        Page<Event> eventsPage = eventRepository.findAll(pageable);
        return eventsPage.map(event -> eventMapper.toListDto(List.of(event)).get(0));
    }

    // События по городу с пагинацией
    public Page<EventListDto> getEventsByCity(String city, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("eventDate").descending());
        Page<Event> eventsPage = eventRepository.findByCity(city, pageable);
        return eventsPage.map(event -> eventMapper.toListDto(List.of(event)).get(0));
    }

    // Предстоящие события с пагинацией
    public Page<EventListDto> getUpcomingEvents(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("eventDate").ascending());
        Page<Event> eventsPage = eventRepository.findByEventDateAfter(LocalDateTime.now(), pageable);
        return eventsPage.map(event -> eventMapper.toListDto(List.of(event)).get(0));
    }

    // События, созданные пользователем
    public Page<EventListDto> getUserCreatedEvents(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Event> eventsPage = eventRepository.findByCreatorId(userId, pageable);
        return eventsPage.map(event -> eventMapper.toListDto(List.of(event)).get(0));
    }

    // События, на которые пользователь идёт
    public Page<EventListDto> getUserGoingEvents(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("eventDate").ascending());
        Page<Event> eventsPage = eventRepository.findByAttendancesUserIdAndAttendancesStatus(
                userId, ParticipationType.GOING, pageable);
        return eventsPage.map(event -> eventMapper.toListDto(List.of(event)).get(0));
    }

    // События, которые интересуют пользователя
    public Page<EventListDto> getUserInterestedEvents(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("eventDate").ascending());
        Page<Event> eventsPage = eventRepository.findByAttendancesUserIdAndAttendancesStatus(
                userId, ParticipationType.INTERESTED, pageable);
        return eventsPage.map(event -> eventMapper.toListDto(List.of(event)).get(0));
    }

    // Обновленный метод getAllUserEvents для получения всех типов событий с пагинацией
    public Map<String, Page<EventListDto>> getAllUserEvents(Long userId, int page, int size) {
        Map<String, Page<EventListDto>> result = new HashMap<>();

        result.put("created", getUserCreatedEvents(userId, page, size));
        result.put("going", getUserGoingEvents(userId, page, size));
        result.put("interested", getUserInterestedEvents(userId, page, size));

        return result;
    }

    public EventResponseDto getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + id));
        return eventMapper.toDto(event, null);
    }

    public void deleteEvent(Long eventId, String username) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + eventId));

        // Проверяем, является ли пользователь создателем события
        if (!event.getCreator().getUsername().equals(username)) {
            throw new IllegalStateException("Only creator can delete the event");
        }
        // Сначала удаляем все записи об участии
        attendanceRepository.deleteByEventId(eventId);
        // Затем удаляем само событие
        eventRepository.delete(event);
    }

    public EventResponseDto updateEvent(Long eventId, EventUpdateDto updateDto, String username) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + eventId));

        if (!event.getCreator().getUsername().equals(username)) {
            throw new IllegalStateException("Only creator can update the event");
        }

        event.setTitle(updateDto.getTitle());
        event.setDescription(updateDto.getDescription());
        event.setEventDate(updateDto.getEventDate());
        event.setCity(updateDto.getCity());
        event.setAddress(updateDto.getAddress());

        Event savedEvent = eventRepository.save(event);
        return eventMapper.toDto(savedEvent, null);
    }


    public Page<EventListDto> getEventsByCategory(EventCategory category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("eventDate").descending());
        Page<Event> events = eventRepository.findByCategory(category, pageable);
        return events.map(event -> eventMapper.toListDto(List.of(event)).get(0));
    }

    public Map<EventCategory, Long> getEventCountByCategory() {
        Map<EventCategory, Long> counts = new HashMap<>();
        for (EventCategory category : EventCategory.values()) {
            long count = eventRepository.countByCategory(category);
            counts.put(category, count);
        }
        return counts;
    }
//список по категориям
    public List<EventListDto> getRelatedEvents(Long eventId, int limit) {
        Event currentEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found"));

        // Получаем события той же категории, исключая текущее событие
        Page<Event> relatedEvents = eventRepository
                .findByCategoryAndIdNot(currentEvent.getCategory(), eventId,
                        PageRequest.of(0, limit, Sort.by("eventDate").ascending()));

        return eventMapper.toListDto(relatedEvents.getContent());
    }

    public Page<EventListDto> getEventsByLocation(String location, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("eventDate").descending());
        Page<Event> eventsPage = eventRepository.findByLocation(location, pageable);
        return eventsPage.map(event -> eventMapper.toListDto(List.of(event)).get(0));
    }
}