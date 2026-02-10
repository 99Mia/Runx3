package org.run.runx3.psh.repository;

import org.run.runx3.common.domain.Boards;
import org.run.runx3.common.domain.Bookmarks;
import org.run.runx3.common.domain.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmarks,Long> {
    List<Bookmarks> findAllByUsers_UserId(Long userId);
    Optional<Bookmarks> findByUsersAndBoards(Users user, Boards board);
    // 특정 게시글에 대한 북마크 리스트
    List<Bookmarks> findAllByBoards_BoardId(Long boardId);
    boolean existsByUsers_UserIdAndBoards_BoardId(Long userId, Long boardId);


    // 특정 사용자 & 게시글 북마크 찾기
    Optional<Bookmarks> findByUsers_UserIdAndBoards_BoardId(Long userId, Long boardId);
}
