package org.run.runx3.psh.service.comment;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.run.runx3.common.domain.Boards;
import org.run.runx3.common.domain.Comments;
import org.run.runx3.common.domain.Users;
import org.run.runx3.common.repository.UserRepository;
import org.run.runx3.psh.dto.CommentDTO;
import org.run.runx3.psh.dto.page.PageRequestDTO;
import org.run.runx3.psh.dto.page.PageResponseDTO;
import org.run.runx3.psh.repository.BoardRepository;
import org.run.runx3.psh.repository.CommentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@Log4j2
@Transactional
@RequiredArgsConstructor  // final 필드 자동 주입
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;



    // 댓글 등록
    @Override
    public Long insertComment(CommentDTO commentDTO) {
        Comments comments = dtoToEntity(commentDTO);
        Boards boards = boardRepository.findById(commentDTO.getBoardId()).orElse(null);
        Users users = userRepository.findById(commentDTO.getUserId()).orElse(null);
        comments.setBoards(boards);
        comments.setUsers(users);
        Long commentId = commentRepository.save(comments).getCommentId();
        return commentId;
    }

    // 댓글 조회
    @Override
    public CommentDTO findById(Long commentId) {
        Comments comments = commentRepository.findById(commentId).orElse(null);
        return entityToDto(comments);
    }

    // 댓글 수정
    @Override
    public void modifyComment(CommentDTO commentDTO) {
        Comments comments = commentRepository.findById(commentDTO.getCommentId()).orElse(null);
        comments.setContent(commentDTO.getContent());
        commentRepository.save(comments);
    }

    // 댓글 삭제
    @Override
    public void deleteComment(Long commentId) {
        commentRepository.deleteById(commentId);
    }


    // 즉, 게시글 별 댓글 조회 + 페이징 처리 + DTO 변환을 한번에 수행하는 메서드이다.
    // pageable 객체를 생성해서 DB 조회에 사용할 페이징과 정렬 정보 세팅

    @Override
    public PageResponseDTO<CommentDTO> getListOfBoard(Long boardId, PageRequestDTO pageRequestDTO) {
        Pageable pageable = pageRequestDTO.getPageable("commentId");
        Page<Comments> result = commentRepository.findByBoards_BoardId(boardId, pageable);

        List<CommentDTO> dtoList = result.getContent().stream()
                .map(comments -> entityToDto(comments))
                .collect(Collectors.toList());

        return PageResponseDTO.<CommentDTO>withAll()
                .pageRequestDTO(pageRequestDTO)
                .total((int) result.getTotalElements())
                .dtoList(dtoList)
                .build();
    }

}
