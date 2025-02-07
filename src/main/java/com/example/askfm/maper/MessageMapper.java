package com.example.askfm.maper;

import com.example.askfm.dto.MessageDTO;
import com.example.askfm.model.Message;
import com.example.askfm.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MessageMapper {
    private final ImageService imageService;

    public MessageDTO toDto(Message message) {
        String senderAvatar = message.getSender().getAvatar() != null ?
                "data:image/jpeg;base64," + imageService.getBase64Avatar(message.getSender().getAvatar()) : null;
        String recipientAvatar = message.getRecipient().getAvatar() != null ?
                "data:image/jpeg;base64," + imageService.getBase64Avatar(message.getRecipient().getAvatar()) : null;

        return MessageDTO.builder()
                .id(message.getId())
                .senderId(message.getSender().getId())
                .recipientId(message.getRecipient().getId())
                .content(message.getContent())
                .timestamp(message.getTimestamp())
                .read(message.isRead())
                .senderName(message.getSender().getUsername())
                .recipientName(message.getRecipient().getUsername())
                .senderAvatar(senderAvatar)
                .recipientAvatar(recipientAvatar)
                .build();
    }

    public Message toEntity(MessageDTO dto) {
        return Message.builder()
                .content(dto.getContent())
                .timestamp(dto.getTimestamp() != null ? dto.getTimestamp() : LocalDateTime.now())
                .read(dto.isRead())
                .build();
    }

    public List<MessageDTO> toDtoList(List<Message> messages) {
        return messages.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public Page<MessageDTO> toDtoPage(Page<Message> messagePage) {
        return messagePage.map(this::toDto);
    }
}
