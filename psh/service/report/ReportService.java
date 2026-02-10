package org.run.runx3.psh.service.report;

import org.run.runx3.psh.dto.ReportDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ReportService {
    Long addReport(ReportDTO reportDTO);
    void updateReportStatus(Long reportId, String status);
    List<ReportDTO> getReportsByUser(Long userId);
    List<ReportDTO> getReportsByBoard(Long boardId);
    List<ReportDTO> getReportsByComment(Long commentId);
}
