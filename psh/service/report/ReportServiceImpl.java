package org.run.runx3.psh.service.report;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.run.runx3.common.domain.Boards;
import org.run.runx3.common.domain.Comments;
import org.run.runx3.common.domain.Users;
import org.run.runx3.common.repository.UserRepository;
import org.run.runx3.psh.domain.Reports;
import org.run.runx3.psh.dto.ReportDTO;
import org.run.runx3.psh.repository.BoardRepository;
import org.run.runx3.psh.repository.CommentRepository;
import org.run.runx3.psh.repository.ReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Log4j2
@Transactional
@RequiredArgsConstructor

public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public Long addReport(ReportDTO reportDTO) {
        Users reporter = userRepository.findById(reportDTO.getReporterId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Reports report = Reports.builder()
                .reason(reportDTO.getReason())
                .reporter(reporter)
                .status(Reports.Status.PENDING)
                .build();

        if (reportDTO.getBoardId() != null) {
            Boards board = boardRepository.findById(reportDTO.getBoardId())
                    .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
            report.setBoards(board);
        } else if (reportDTO.getCommentId() != null) {
            Comments comment = commentRepository.findById(reportDTO.getCommentId())
                    .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));
            report.setComment(comment);
        } else {
            throw new RuntimeException("신고 대상이 없습니다.");
        }

        reportRepository.save(report);
        return report.getReportId();
    }

    @Override
    @Transactional
    public void updateReportStatus(Long reportId, String status) {
        Reports report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("신고를 찾을 수 없습니다."));
        report.setStatus(Reports.Status.valueOf(status));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportDTO> getReportsByUser(Long userId) {
        return reportRepository.findAllByReporter_UserId(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportDTO> getReportsByBoard(Long boardId) {
        return reportRepository.findAllByBoards_BoardId(boardId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportDTO> getReportsByComment(Long commentId) {
        return reportRepository.findAllByComment_CommentId(commentId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private ReportDTO toDTO(Reports report) {
        return ReportDTO.builder()
                .reportId(report.getReportId())
                .reason(report.getReason())
                .boardId(report.getBoards() != null ? report.getBoards().getBoardId() : null)
                .commentId(report.getComment() != null ? report.getComment().getCommentId() : null)
                .reporterId(report.getReporter().getUserId())
                .status(report.getStatus().name())
                .build();
    }
}