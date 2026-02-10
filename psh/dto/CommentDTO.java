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
public class CommentDTO {
    private Long commentId;
    private String content;
    private Long boardId;
    private Long userId;
    private String nickname;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
