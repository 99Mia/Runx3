package org.run.runx3.psh.controller;

import lombok.RequiredArgsConstructor;
import org.run.runx3.common.domain.Users;
import org.run.runx3.psh.dto.BookmarkDTO;
import org.run.runx3.psh.service.bookmark.BookmarkService;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bookmark")
@RequiredArgsConstructor
public class BookmarksController {
    private final BookmarkService bookmarkService;

    // 북마크 토글
    @PostMapping(value = "/bookmark", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> toggleBookmark(@RequestBody BookmarkDTO bookmarkDTO,
                                              @AuthenticationPrincipal Users loginUser) {
        // 클라이언트에서 보낸 userId는 무시하고 로그인 사용자 ID 사용
        bookmarkDTO.setUserId(loginUser.getUserId());
        boolean bookmarked = bookmarkService.toggleBookmark(bookmarkDTO);

        Map<String, Object> map = new HashMap<>();
        map.put("bookmarked", bookmarked);
        return map;
    }

    // 사용자별 북마크 조회
    @GetMapping("/my")
    public List<BookmarkDTO> getMyBookmarks(@AuthenticationPrincipal Users loginUser) {
        return bookmarkService.getBookmarkByUser(loginUser.getUserId());
    }

    // 북마크 삭제
    @DeleteMapping("/{bookmarkId}")
    public Map<String, Long> removeBookmark(@PathVariable Long bookmarkId) {
        bookmarkService.removeBookmark(bookmarkId);
        Map<String, Long> map = new HashMap<>();
        map.put("bookmarkId", bookmarkId);
        return map;
    }
}


