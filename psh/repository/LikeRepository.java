package org.run.runx3.psh.repository;

import org.run.runx3.common.domain.Likes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Likes,Long> {
    // Board 좋아요 존재 여부
    boolean existsByUsers_UserIdAndBoards_BoardId(Long userId, Long boardId);

    // Comment 좋아요 존재 여부
    boolean existsByUsers_UserIdAndComment_CommentId(Long userId, Long commentId);

    boolean existsByBoards_BoardIdAndUsers_UserId(Long boardId, Long userId);
    List<Likes> findAllByUsers_UserId(Long userId);

    Optional<Likes> findByUsers_UserIdAndBoards_BoardId(Long userId, Long boardId);
    Optional<Likes> findByUsers_UserIdAndComment_CommentId(Long userId, Long commentId);

    List<Likes> findAllByBoards_BoardId(Long boardId);
    List<Likes> findAllByComment_CommentId(Long commentId);

}
