package org.run.runx3.psh.service.board;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.run.runx3.common.domain.Boards;
import org.run.runx3.common.domain.Bookmarks;
import org.run.runx3.common.domain.Likes;
import org.run.runx3.common.domain.Users;
import org.run.runx3.common.repository.UserRepository;
import org.run.runx3.psh.domain.Reports;
import org.run.runx3.psh.dto.BoardDTO;
import org.run.runx3.psh.dto.BoardImageDTO;
import org.run.runx3.psh.dto.BoardListCommentCountDTO;
import org.run.runx3.psh.dto.page.PageRequestDTO;
import org.run.runx3.psh.dto.page.PageResponseDTO;
import org.run.runx3.psh.repository.*;
import org.run.runx3.psh.repository.BoardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Log4j2
@Transactional
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LikeRepository likeRepository;
    private final BookmarkRepository bookmarkRepository;
    private final ReportRepository reportRepository;


    @Override
    public Long insertBoard(BoardDTO boardDTO) {

        // 1) userId로 실제 유저 조회
        Users writer = userRepository.findById(boardDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("작성자 정보를 찾을 수 없습니다."));

        Boards board = dtoToEntity(boardDTO);
        board.setUsers(writer);

        // 이미지 대표 설정
        if (!boardDTO.getBoardImageDTOS().isEmpty()) {
            BoardImageDTO firstImg = boardDTO.getBoardImageDTOS().get(0);
            board.setImage("s_" + firstImg.getUuid() + "_" + firstImg.getFilename());
        }

        boardRepository.save(board);
        return board.getBoardId();
    }

    // 전체 게시글 조회
    @Override
    public List<BoardDTO> findAllBoard() {
        List<Boards> boardsList = boardRepository.findAll();

        return boardsList.stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
    }

    @Override
    public BoardDTO findBoardById(Long boardId) {
        Boards board = boardRepository.findByIdWithUser(boardId)
                .orElseThrow(() -> new RuntimeException("게시글이 존재하지 않습니다."));

        return entityToDto(board);
    }

    // 게시글 상세 조회
    @Override
    public BoardDTO findBoardById(Long boardId, Integer mode) {
        Boards boards = boardRepository.findByIdWithImages(boardId).orElse(null);
        if (boards == null) return null;
        // 조회수 증가
        if (mode == 1) {
            boards.updateReadCount();
            boardRepository.save(boards);
        }

        BoardDTO boardDTO = entityToDto(boards);
        boardDTO.setUsername(boards.getUsers().getUsername());
        return boardDTO;
    }

    // 게시글 수정
    @Override
    public void updateBoard(BoardDTO boardDTO) {
        Boards boards = boardRepository.findById(boardDTO.getBoardId()).orElse(null);
        if (boards == null) return;

        boards.change(boardDTO.getTitle(), boardDTO.getContent());

        // 이미지 수정 전처리
        if (boardDTO.getBoardImageDTOS() != null) {
            boards.removeImage();
            for (BoardImageDTO imgDTO : boardDTO.getBoardImageDTOS()) {
                boards.addImage(imgDTO.getUuid(), imgDTO.getFilename(), imgDTO.isImage());
            }
        }

        boardRepository.save(boards);
    }

    //게시글 삭제
    @Override
    public void deleteBoard(Long boardId) {
        Boards boards = boardRepository.findByIdWithImages(boardId).orElse(null);
        if (boards != null) {
            boards.removeImage();
            boardRepository.deleteById(boardId);
        }
    }






    // 기본 게시글 리스트 (검색 + 페이징)
    @Override
    public PageResponseDTO<BoardDTO> getList(PageRequestDTO pageRequestDTO) {
        Pageable pageable = pageRequestDTO.getPageable("boardId");
        Page<Boards> result = boardRepository.searchAll(
                pageRequestDTO.getTypes(),
                pageRequestDTO.getKeyword(),
                pageable
        );

        List<BoardDTO> dtoList = result.getContent().stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());

        return PageResponseDTO.<BoardDTO>withAll()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .total((int) result.getTotalElements())
                .build();
    }

    @Override
    public PageResponseDTO<BoardListCommentCountDTO> getListCommentCount(PageRequestDTO pageRequestDTO) {

        String[] types = pageRequestDTO.getTypes();
        String keyword = pageRequestDTO.getKeyword();
        Pageable pageable = pageRequestDTO.getPageable("boardId");

        //  Repository 호출 (null 방지)
        Page<BoardListCommentCountDTO> result = boardRepository.searchWithCommentCount(types, keyword, pageable);
        log.info(result.getContent().size());

        // null 이거나 비어있으면 기본값 처리
        List<BoardListCommentCountDTO> dtoList = (result != null) ? result.getContent() : new ArrayList<>();
        for (BoardListCommentCountDTO dto : dtoList) {
            log.info("문자열============="+dto);
        }

        // 로그 찍어서 데이터 확인
        log.info("==== Board List Size: {} ====", dtoList.size());
        dtoList.forEach(dto -> log.info("Title: {}, Writer: {}, CommentCount: {}",
                dto.getTitle(), dto.getUsername(), dto.getCommentCount()));

        // PageResponseDTO 빌드
        return PageResponseDTO.<BoardListCommentCountDTO>withAll()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .total(result != null ? (int) result.getTotalElements() : 0)
                .build();
    }

    // 좋아요 토글
    @Override
    public boolean toggleLike(Long boardId, Long userId) {
        Optional<Likes> existing = likeRepository.findByUsers_UserIdAndBoards_BoardId(userId, boardId);
        if (existing.isPresent()) {
            likeRepository.delete(existing.get());
            return false; // 좋아요 취소
        } else {
            Likes newLike = Likes.builder()
                    .users(Users.builder().userId(userId).build())
                    .boards(Boards.builder().boardId(boardId).build())
                    .build();
            likeRepository.save(newLike);
            return true;  // 좋아요 등록
        }
    }


    // 좋아요 개수
    @Override
    public int getLikeCount(Long boardId) {
        return likeRepository.findAllByBoards_BoardId(boardId).size();
    }

    // 북마크 토글
    @Override
    public boolean toggleBookmark(Long boardId, Long userId) {
        Optional<Bookmarks> existing = bookmarkRepository.findByUsers_UserIdAndBoards_BoardId(userId, boardId);
        if (existing.isPresent()) {
            bookmarkRepository.delete(existing.get());
            return false; // 북마크 취소
        } else {
            Bookmarks newBookmark = Bookmarks.builder()
                    .users(Users.builder().userId(userId).build())
                    .boards(Boards.builder().boardId(boardId).build())
                    .build();
            bookmarkRepository.save(newBookmark);
            return true; // 북마크 등록
        }
    }

    // 북마크 개수
    @Override
    public int getBookmarkCount(Long boardId) {
        return bookmarkRepository.findAllByBoards_BoardId(boardId).size();
    }


    // 신고 등록
    @Override
    public boolean reportBoard(Long boardId, Long userId, String reason) {
        boolean alreadyReported = reportRepository.existsByReporter_UserIdAndBoards_BoardId(userId, boardId);
        if (alreadyReported) return false; // 이미 신고함

        Reports newReport = Reports.builder()
                .reporter(Users.builder().userId(userId).build())
                .boards(Boards.builder().boardId(boardId).build())
                .reason(reason)
                .build();
        reportRepository.save(newReport);
        return true;
    }

    // 신고 개수
    @Override
    public int getReportCount(Long boardId) {
        return reportRepository.findAllByBoards_BoardId(boardId).size();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<BoardDTO> getListOrderByLikes(PageRequestDTO pageRequestDTO) {
        // Sort는 JPQL에서 처리하므로 null
        Pageable pageable = pageRequestDTO.getPageable(null);

        Page<Boards> result = boardRepository.findAllOrderByLikes(pageable);

        List<BoardDTO> dtoList = result.getContent().stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());

        int total = (int) result.getTotalElements();

        return new PageResponseDTO<>(pageRequestDTO, dtoList, total);
    }

    @Override
    public Long findUserIdByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(user -> user.getUserId())   // userId 맞게 수정
                .orElse(5L); // 비회원 혹은 미저장 OAuth 사용자 기본값
    }

    @Override
    public boolean isLikedByUser(Long boardId, Long userId) {
        return likeRepository.existsByBoards_BoardIdAndUsers_UserId(boardId, userId);

    }

    @Override
    public boolean isBookmarkedByUser(Long boardId, Long userId) {
        // BookmarkRepository에서 존재 여부 확인
        return bookmarkRepository.existsByUsers_UserIdAndBoards_BoardId(userId, boardId);
    }

    @Override
    public boolean isReportedByUser(Long boardId, Long userId) {
        // ReportRepository에서 존재 여부 확인
        return reportRepository.existsByReporter_UserIdAndBoards_BoardId(userId, boardId);
    }

}


















