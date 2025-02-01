package com.example.askfm.maper;

import com.example.askfm.dto.AdLeadResponseDTO;
import com.example.askfm.model.AdLead;
import org.springframework.stereotype.Component;

@Component
public class AdLeadMapper {
    public AdLeadResponseDTO toDto(AdLead lead) {
        return new AdLeadResponseDTO(
                lead.getId(),
                lead.getUsername(),
                lead.getEmail(),
                lead.getSubmittedAt()

        );
    }
}