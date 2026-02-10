package org.run.runx3.psh.service.bookmark;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.run.runx3.common.domain.Boards;
import org.run.runx3.common.domain.Bookmarks;
import org.run.runx3.common.domain.Users;
import org.run.runx3.common.repository.UserRepository;
import org.run.runx3.psh.dto.BookmarkDTO;
import org.run.runx3.psh.repository.BoardRepository;
import org.run.runx3.psh.repository.BookmarkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@Log4j2
@Transactional
@RequiredArgsConstructor
public class BookmarkServiceImpl implements BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final UserRepository usersRepository;
    private final BoardRepository boardRepository;

    @Override
    @Transactional
    public Long addBookmark(BookmarkDTO bookmarksDTO) {
        // 게스트 user ID 하드코딩
        Users user = usersRepository.findById(bookmarksDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Boards board = boardRepository.findById(bookmarksDTO.getBoardId())
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        // 1. 이미 북마크가 있는지 체크
        Optional<Bookmarks> exist = bookmarkRepository.findByUsersAndBoards(user, board);
        if (exist.isPresent()) {
            // 이미 존재하면 그냥 ID 반환하거나 토글용 처리
            return exist.get().getBookmarkId();
        }

        // 2. 새 북마크 생성
        Bookmarks bookmark = Bookmarks.builder()
                .users(user)
                .boards(board)
                .createdAt(LocalDateTime.now())
                .build();

        bookmarkRepository.save(bookmark);
        return bookmark.getBookmarkId();
    }




    @Override
    @Transactional
    public boolean toggleBookmark(BookmarkDTO bookmarksDTO) {
        Users user = usersRepository.findById(bookmarksDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Boards board = boardRepository.findById(bookmarksDTO.getBoardId())
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        Optional<Bookmarks> exist = bookmarkRepository.findByUsersAndBoards(user, board);
        if (exist.isPresent()) {
            bookmarkRepository.delete(exist.get());
            return false; // 북마크 해제됨
        }

        Bookmarks bookmark = Bookmarks.builder()
                .users(user)
                .boards(board)
                .createdAt(LocalDateTime.now())
                .build();
        bookmarkRepository.save(bookmark);
        return true; // 북마크 생성됨
    }


    @Override
    public void removeBookmark(Long bookmarkId) {
        bookmarkRepository.deleteById(bookmarkId);
    }

    @Override
    public List<BookmarkDTO> getBookmarkByUser(Long userId) {
        return bookmarkRepository.findAllByUsers_UserId(userId).stream()
                .map(bookmark -> BookmarkDTO.builder()
                        .bookmarkId(bookmark.getBookmarkId())
                        .userId(bookmark.getUsers().getUserId())
                        .boardId(bookmark.getBoards().getBoardId())
                        .username(bookmark.getUsers().getUsername())
                        .boardTitle(bookmark.getBoards().getTitle())
                        .createdAt(bookmark.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}
