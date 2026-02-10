package org.run.runx3.psh.service.like;

import lombok.RequiredArgsConstructor;
import org.run.runx3.common.domain.Boards;
import org.run.runx3.common.domain.Comments;
import org.run.runx3.common.domain.Likes;
import org.run.runx3.common.domain.Users;
import org.run.runx3.common.repository.UserRepository;
import org.run.runx3.psh.dto.LikeDTO;
import org.run.runx3.psh.repository.BoardRepository;
import org.run.runx3.psh.repository.CommentRepository;
import org.run.runx3.psh.repository.LikeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;

    @Override
    public Long addLike(LikeDTO likeDTO) {
        Users user = userRepository.findById(likeDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        Likes like = Likes.builder()
                .users(user)
                .createdAt(LocalDateTime.now())
                .build();

        if ("BOARD".equalsIgnoreCase(likeDTO.getType())) {
            Boards board = boardRepository.findById(likeDTO.getTargetId())
                    .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
            like.setBoards(board);
        } else if ("COMMENT".equalsIgnoreCase(likeDTO.getType())) {
            Comments comment = commentRepository.findById(likeDTO.getTargetId())
                    .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));
            like.setComment(comment);
        } else {
            throw new RuntimeException("유효하지 않은 타입입니다.");
        }

        likeRepository.save(like);
        return like.getLikeId();
    }


    @Override
    @Transactional
    public void removeLike(Long likeId) {
        likeRepository.deleteById(likeId);
    }


    @Override
    @Transactional(readOnly = true)
    public List<LikeDTO> getLikesByUser(Long userId) {
        return likeRepository.findAllByUsers_UserId(userId).stream()
                .map(like -> LikeDTO.builder()
                        .likeId(like.getLikeId())
                        .userId(like.getUsers().getUserId())
                        .targetId(like.getBoards() != null ? like.getBoards().getBoardId() : like.getComment().getCommentId())
                        .type(like.getBoards() != null ? "BOARD" : "COMMENT")
                        .createdAt(like.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isLiked(Long userId, Long targetId, String type) {
        if ("BOARD".equalsIgnoreCase(type)) {
            return likeRepository.existsByUsers_UserIdAndBoards_BoardId(userId, targetId); // boards로 수정
        } else if ("COMMENT".equalsIgnoreCase(type)) {
            return likeRepository.existsByUsers_UserIdAndComment_CommentId(userId, targetId);
        }
        return false;
    }

    @Transactional
    public void removeLikeByUserAndTarget(Long userId, Long targetId, String type) {
        if ("BOARD".equalsIgnoreCase(type)) {
            likeRepository.findByUsers_UserIdAndBoards_BoardId(userId, targetId)
                    .ifPresent(likeRepository::delete); // 있으면 삭제, 없으면 아무 것도 안 함
        } else if ("COMMENT".equalsIgnoreCase(type)) {
            likeRepository.findByUsers_UserIdAndComment_CommentId(userId, targetId)
                    .ifPresent(likeRepository::delete);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public int countLikes(Long targetId, String type) {
        if ("BOARD".equalsIgnoreCase(type)) {
            return likeRepository.findAllByBoards_BoardId(targetId).size();
        } else if ("COMMENT".equalsIgnoreCase(type)) {
            return likeRepository.findAllByComment_CommentId(targetId).size();
        }
        return 0;
    }


}