package org.run.runx3.psh.service.bookmark;

import org.run.runx3.psh.dto.BookmarkDTO;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public interface BookmarkService {
    Long addBookmark(BookmarkDTO bookmarksDTO);       // 북마크 등록
    void removeBookmark(Long bookmarkId);              // 북마크 삭제
    List<BookmarkDTO> getBookmarkByUser(Long userId); // 사용자별 북마크 목록 조회

    boolean toggleBookmark(BookmarkDTO bookmarkDTO);
}
