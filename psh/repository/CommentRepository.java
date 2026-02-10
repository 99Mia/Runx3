package org.run.runx3.psh.repository;

import org.run.runx3.common.domain.Comments;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comments,Long> {
    Page<Comments> findByBoards_BoardId(Long boardId, Pageable pageable);
    List<Comments> findByUsers_UserId(Long userId);

}
