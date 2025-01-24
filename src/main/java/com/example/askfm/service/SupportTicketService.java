package com.example.askfm.service;

import com.example.askfm.dto.SupportTicketDto;
import com.example.askfm.enums.TicketStatus;
import com.example.askfm.exception.SpamDetectedException;
import com.example.askfm.exception.TicketNotFoundException;
import com.example.askfm.model.SupportTicket;
import com.example.askfm.repository.SupportTicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupportTicketService {
    private final SupportTicketRepository supportTicketRepository;

    public void createTicket(SupportTicketDto dto) {
        validateTicket(dto);
        SupportTicket ticket = new SupportTicket();
        ticket.setClientName(dto.getClientName());
        ticket.setSubject(dto.getSubject());
        ticket.setDescription(dto.getDescription());
        ticket.setEmail(dto.getEmail());
        ticket.setStatus(TicketStatus.NEW);
        supportTicketRepository.save(ticket);
    }

    public List<SupportTicketDto> getAllTickets() {
        return supportTicketRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public SupportTicketDto getTicketById(Long id) {
        SupportTicket ticket = supportTicketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found: " + id));
        return convertToDto(ticket);
    }

    public void updateStatus(Long id, TicketStatus status) {
        SupportTicket ticket = supportTicketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found: " + id));
        ticket.setStatus(status);
        supportTicketRepository.save(ticket);
    }

    private void validateTicket(SupportTicketDto dto) {
        if (dto.getWebsite() != null && !dto.getWebsite().isEmpty()) {
            log.warn("Spam detected for email: {}", dto.getEmail());
            throw new SpamDetectedException("Spam detected");
        }
    }


    private SupportTicketDto convertToDto(SupportTicket ticket) {
        return SupportTicketDto.builder()
                .id(ticket.getId())
                .clientName(ticket.getClientName())
                .subject(ticket.getSubject())
                .description(ticket.getDescription())
                .email(ticket.getEmail())
                .status(ticket.getStatus())
                .build();
    }

}
