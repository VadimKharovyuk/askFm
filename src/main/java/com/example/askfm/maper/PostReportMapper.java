package com.example.askfm.maper;

import com.example.askfm.dto.PostReportCreateDTO;
import com.example.askfm.dto.PostReportDTO;
import com.example.askfm.model.PostReport;
import com.example.askfm.service.PostService;
import com.example.askfm.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PostReportMapper {
    private final UserService userService;
    private final PostService postService;

    public PostReport toEntity(PostReportCreateDTO dto, String reporterUsername) {
        return PostReport.builder()
                .post(postService.findById(dto.getPostId()))  // Правильно
                .reporter(userService.findByUsername(reporterUsername))  // Правильно
                .category(dto.getCategory())
                .reason(dto.getReason())
                .createdAt(LocalDateTime.now())
                .similarReportsCount(0)
                .build();
    }

    public PostReportDTO toDTO(PostReport report) {
        return PostReportDTO.builder()
                .id(report.getId())
                .postId(report.getPost().getId())
                .postContent(truncateContent(report.getPost().getContent(), 100))
                .reporterUsername(report.getReporter().getUsername())
                .category(report.getCategory())
                .categoryDisplayName(report.getCategory().getDisplayName())
                .reason(report.getReason())
                .createdAt(report.getCreatedAt())
                .similarReportsCount(report.getSimilarReportsCount())
                .authorUsername(report.getPost().getAuthor().getUsername())
                .build();
    }

    private String truncateContent(String content, int maxLength) {
        if (content == null) return null;
        if (content.length() <= maxLength) return content;
        return content.substring(0, maxLength - 3) + "...";
    }

    public List<PostReportDTO> toDTOList(List<PostReport> reports) {
        return reports.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}
