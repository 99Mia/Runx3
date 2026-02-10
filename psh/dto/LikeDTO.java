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
public class LikeDTO {
    private Long likeId;
    private Long targetId;  // 게시글 또는 댓글 ID
    private Long userId;
    private String type;    // "BOARD" 또는 "COMMENT"
    private LocalDateTime createdAt;


}
