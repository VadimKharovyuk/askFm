package com.example.askfm.service;

import com.example.askfm.dto.PostReportCreateDTO;
import com.example.askfm.dto.PostReportDTO;
import com.example.askfm.maper.PostReportMapper;
import com.example.askfm.model.PostReport;
import com.example.askfm.repository.PostReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostReportService {
    private final PostReportRepository reportRepository;
    private final PostReportMapper reportMapper;

    public PostReportDTO createReport(PostReportCreateDTO dto, String username) {
        PostReport report = reportMapper.toEntity(dto, username);
        return reportMapper.toDTO(reportRepository.save(report));
    }

    // Метод для админа с пагинацией
    public Page<PostReportDTO> getAllReports(Pageable pageable) {
        Page<PostReport> reportPage = reportRepository.findAllByOrderByCreatedAtDesc(pageable);
        return reportPage.map(reportMapper::toDTO);
    }

    public List<PostReportDTO> getReportsForPost(Long postId) {
        return reportMapper.toDTOList(
                reportRepository.findByPostIdOrderByCreatedAtDesc(postId)
        );
    }


    public void deleteReport(Long id) {
        reportRepository.deleteById(id);
    }
}
