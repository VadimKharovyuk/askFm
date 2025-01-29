package com.example.askfm.dto;

import com.example.askfm.enums.ReportCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostReportDTO {
    private Long id;
    private Long postId;
    private String postContent; // краткое содержание поста
    private String reporterUsername;
    private ReportCategory category;
    private String categoryDisplayName;
    private String reason;
    private LocalDateTime createdAt;
    private Integer similarReportsCount;
}
