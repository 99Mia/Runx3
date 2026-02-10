package org.run.runx3.psh.repository;

import org.run.runx3.psh.domain.Reports;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Reports,Long> {
    // 특정 유저가 등록한 신고 목록
    List<Reports> findAllByReporter_UserId(Long userId);

    // 특정 게시글에 대한 신고 목록
    List<Reports> findAllByBoards_BoardId(Long boardId);

    // 특정 댓글에 대한 신고 목록
    List<Reports> findAllByComment_CommentId(Long commentId);


    boolean existsByReporter_UserIdAndBoards_BoardId(Long userId, Long boardId);


    // 게시글 신고 개수
    int countByBoards_BoardId(Long boardId);


}