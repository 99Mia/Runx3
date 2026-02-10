package org.run.runx3.psh.dto;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class BoardListCommentCountDTO {
    private Long boardId;
    private String title;
//    private String nickname;
    private Integer views;
    private LocalDateTime createdAt;
    private Long commentCount;
    private String username;





}
