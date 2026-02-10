package org.run.runx3.psh.controller;

import lombok.RequiredArgsConstructor;
import org.run.runx3.psh.dto.ReportDTO;
import org.run.runx3.psh.service.report.ReportService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    // 신고 등록
    @PostMapping
    public Long addReport(@RequestBody ReportDTO reportDTO) {
        return reportService.addReport(reportDTO);
    }

    // 신고 상태 업데이트
    @PutMapping("/{reportId}")
    public void updateStatus(@PathVariable Long reportId,
                             @RequestParam String status) {
        reportService.updateReportStatus(reportId, status);
    }

    // 사용자별 신고 목록
    @GetMapping("/user/{userId}")
    public List<ReportDTO> getReportsByUser(@PathVariable Long userId) {
        return reportService.getReportsByUser(userId);
    }

    // 게시글별 신고 목록
    @GetMapping("/board/{boardId}")
    public List<ReportDTO> getReportsByBoard(@PathVariable Long boardId) {
        return reportService.getReportsByBoard(boardId);
    }

    // 댓글별 신고 목록
    @GetMapping("/comment/{commentId}")
    public List<ReportDTO> getReportsByComment(@PathVariable Long commentId) {
        return reportService.getReportsByComment(commentId);
    }
}

