package org.run.runx3.psh.controller;

import lombok.RequiredArgsConstructor;
import org.run.runx3.common.config.CustomUserDetails;
import org.run.runx3.common.domain.Users;
import org.run.runx3.psh.dto.LikeDTO;
import org.run.runx3.psh.service.like.LikeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;


    // 1. 좋아요 추가
    @PostMapping
    public ResponseEntity<Long> addLike(@RequestBody LikeDTO likeDTO) {
        Long likeId = likeService.addLike(likeDTO);
        return ResponseEntity.ok(likeId);
    }

    // 2. 좋아요 삭제
    @DeleteMapping("/{likeId}")
    public ResponseEntity<Void> removeLike(@PathVariable Long likeId) {
        likeService.removeLike(likeId);
        return ResponseEntity.ok().build();
    }

    // 3. 특정 게시글/댓글 좋아요 여부
    @GetMapping("/check")
    public ResponseEntity<Boolean> isLiked(
            @RequestParam Long userId,
            @RequestParam Long targetId,
            @RequestParam String type) {

        boolean liked = likeService.isLiked(userId, targetId, type);
        return ResponseEntity.ok(liked);
    }

    // 로그인한 사용자의 좋아요 목록 조회  --> 마이페이지 조회용
    @GetMapping("/my")
    public ResponseEntity<List<LikeDTO>> getMyLikes(@AuthenticationPrincipal Users loginUser) {
        List<LikeDTO> likes = likeService.getLikesByUser(loginUser.getUserId());
        return ResponseEntity.ok(likes);
    }

    // 5. 좋아요 토글
    @PostMapping("/toggle")
    public ResponseEntity<Map<String, Object>> toggleLike(
            @RequestBody LikeDTO likeDTO,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "로그인이 필요합니다."));
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Users loginUser = userDetails.getUser();

        boolean liked = likeService.isLiked(loginUser.getUserId(), likeDTO.getTargetId(), likeDTO.getType());

        if (!liked) {
            LikeDTO dto = LikeDTO.builder()
                    .userId(loginUser.getUserId())
                    .targetId(likeDTO.getTargetId())
                    .type(likeDTO.getType())
                    .build();

            likeService.addLike(dto);
        } else {
            likeService.removeLikeByUserAndTarget(
                    loginUser.getUserId(),
                    likeDTO.getTargetId(),
                    likeDTO.getType()
            );
        }

        int likeCount = likeService.countLikes(likeDTO.getTargetId(), likeDTO.getType());

        return ResponseEntity.ok(Map.of(
                "liked", !liked,
                "likeCount", likeCount
        ));
    }

    // 좋아요 삭제 (userId, targetId, type 기반)
    @DeleteMapping
    public ResponseEntity<Void> removeLikeByUserAndTarget(@RequestBody LikeDTO likeDTO) {
        likeService.removeLikeByUserAndTarget(
                likeDTO.getUserId(),
                likeDTO.getTargetId(),
                likeDTO.getType()
        );
        return ResponseEntity.ok().build();
    }

}

