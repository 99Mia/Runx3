package org.run.runx3.psh.service.board;

import org.run.runx3.common.domain.Boards;
import org.run.runx3.psh.domain.BoardImage;
import org.run.runx3.psh.dto.BoardDTO;
import org.run.runx3.psh.dto.BoardImageDTO;
import org.run.runx3.psh.dto.BoardListCommentCountDTO;
import org.run.runx3.psh.dto.page.PageRequestDTO;
import org.run.runx3.psh.dto.page.PageResponseDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.User;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public interface BoardService {

    Long insertBoard(BoardDTO boardDTO);
    List<BoardDTO> findAllBoard();

    BoardDTO findBoardById(Long boardId);
    BoardDTO findBoardById(Long boardId, Integer mode);

    void updateBoard(BoardDTO boardDTO);
    void deleteBoard(Long boardId);

    PageResponseDTO<BoardDTO> getList(PageRequestDTO pageRequestDTO);
    PageResponseDTO<BoardListCommentCountDTO> getListCommentCount(PageRequestDTO pageRequestDTO);

    boolean isLikedByUser(Long boardId, Long userId);
    boolean isBookmarkedByUser(Long boardId, Long userId);
    boolean isReportedByUser(Long boardId, Long userId);

    boolean toggleLike(Long boardId, Long userId);
    int getLikeCount(Long boardId);

    boolean toggleBookmark(Long boardId, Long userId);
    int getBookmarkCount(Long boardId);

    boolean reportBoard(Long boardId, Long userId, String reason);
    int getReportCount(Long boardId);

    // DTO 변환
    default Boards dtoToEntity(BoardDTO boardDTO) {
        Boards boards = Boards.builder()
                .boardId(boardDTO.getBoardId())
                .title(boardDTO.getTitle())
                .content(boardDTO.getContent())
                .image(boardDTO.getImage())
                .build();
        if(boardDTO.getBoardImageDTOS() != null) {
            boardDTO.getBoardImageDTOS().forEach(file ->{
                boards.addImage(file.getUuid(), file.getFilename(), file.isImage());
            });
        }
        return boards;
    }

    default BoardDTO entityToDto(Boards boards) {
        BoardDTO boardDTO = BoardDTO.builder()
                .boardId(boards.getBoardId())
                .title(boards.getTitle())
                .content(boards.getContent())
                .views(boards.getViews())
                .regDate(boards.getCreatedAt())
                .modeDate(boards.getUpdatedAt())
                .userId(boards.getUsers() != null ? boards.getUsers().getUserId() : null)
                .username(boards.getUsers() != null ? boards.getUsers().getUsername() : null)
                .build();

        List<BoardImageDTO> imageBoardDTO = boards.getImageSet().stream()
                .sorted()
                .map(this::imgEntityToDTO)
                .collect(Collectors.toList());
        boardDTO.setBoardImageDTOS(imageBoardDTO);
        return boardDTO;
    }

    default BoardImageDTO imgEntityToDTO(BoardImage boardImage) {
        return BoardImageDTO.builder()
                .uuid(boardImage.getUuid())
                .filename(boardImage.getFilename())
                .image(boardImage.isImage())
                .ord(boardImage.getOrd())
                .build();
    }

    PageResponseDTO<BoardDTO> getListOrderByLikes(PageRequestDTO pageRequestDTO);

    Long findUserIdByEmail(String email);

}
































































