package com.example.askfm.dto;

import com.example.askfm.model.AdLead;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdLeadResponseDTO {
    private Long id;
    private String username;
    private String email;
    private LocalDateTime submittedAt;


}
