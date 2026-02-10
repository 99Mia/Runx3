package org.run.runx3.psh.service.like;

import org.run.runx3.psh.dto.LikeDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
@Transactional
public interface LikeService {
    Long addLike(LikeDTO likeDTO);             // 좋아요 등록
    void removeLike(Long likeId);              // 좋아요 삭제
    List<LikeDTO> getLikesByUser(Long userId); // 사용자별 좋아요 조회
    boolean isLiked(Long userId, Long targetId, String type); // 특정 게시글/댓글 좋아요 여부

    void removeLikeByUserAndTarget(Long userId, Long targetId, String type);
    int countLikes(Long targetId, String type); // 게시글/댓글 좋아요 개수 반환

}
