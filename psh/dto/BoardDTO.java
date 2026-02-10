package org.run.runx3.psh.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardDTO {
    private Long boardId;
    private String title;
    private String content;
    private String category;
    private String image;
    private int views;
    private boolean isImportant;
    private Long userId;
    private String username;
    private List<CommentDTO> comments;

    private int likeCount;
    private boolean liked; // 로그인한 사용자가 좋아요를 눌렀는지 여부
    private boolean bookmarked;
    private boolean reported;

    private LocalDateTime regDate;
    private LocalDateTime modeDate;

    private List<BoardImageDTO> boardImageDTOS;

}
