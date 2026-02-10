package org.run.runx3.psh.service.comment;

import org.run.runx3.common.domain.Comments;
import org.run.runx3.psh.dto.CommentDTO;
import org.run.runx3.psh.dto.page.PageRequestDTO;
import org.run.runx3.psh.dto.page.PageResponseDTO;
import org.springframework.stereotype.Service;

@Service
public interface CommentService {
    Long insertComment(CommentDTO commentDTO);  // 댓글 등록
    CommentDTO findById(Long commentId);  // 댓글 조회
    void modifyComment(CommentDTO commentDTO); // 댓글 수정
    void deleteComment(Long commentId);  // 댓글 삭제

    // 특정 게시글(Board)에 속한 댓글 리스트를 가져오는 메서드
    PageResponseDTO<CommentDTO> getListOfBoard(Long boardId, PageRequestDTO pageRequestDTO);

    default Comments dtoToEntity(CommentDTO commentDTO){
        Comments comments = Comments.builder()
                .commentId(commentDTO.getCommentId())
                .content(commentDTO.getContent())
                .build();
        return comments;
    }
    default CommentDTO entityToDto(Comments comments){
        CommentDTO commentDTO=CommentDTO.builder()
                .commentId(comments.getCommentId())
                .content(comments.getContent())
                .boardId(comments.getBoards().getBoardId())
                .userId(comments.getUsers().getUserId())
                .nickname(comments.getUsers().getNickname())
                .createdAt(comments.getCreatedAt())
                .updatedAt(comments.getUpdatedAt())
                .build();
        return commentDTO;
    }
}
