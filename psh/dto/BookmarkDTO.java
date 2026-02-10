package org.run.runx3.psh.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkDTO {
    private Long bookmarkId;    // PK
    private Long boardId;       // 게시글 ID
    private Long userId;        // 사용자 ID
    private String boardTitle;  // 게시글 제목 (optional)
    private String username;    // 사용자 이름 (optional)
    private LocalDateTime createdAt;  // 북마크 등록일
}
