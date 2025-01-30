package com.example.askfm.maper;

import com.example.askfm.dto.VisitDTO;
import com.example.askfm.model.Visit;
import com.example.askfm.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class VisitMapper {
    private final ImageService imageService;

    public VisitDTO toDTO(Visit visit) {
        return VisitDTO.builder()
                .visitorId(visit.getVisitor().getId())
                .visitorUsername(visit.getVisitor().getUsername())
                .visitorAvatar(imageService.getBase64Avatar(visit.getVisitor().getAvatar()))
                .visitedAt(visit.getVisitedAt())
                .build();
    }

    public List<VisitDTO> toDTOList(List<Visit> visits) {
        return visits.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}
