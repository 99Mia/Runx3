package org.run.runx3.psh.controller;

import org.run.runx3.common.config.CustomUserDetails;
import org.run.runx3.common.domain.Users;
import org.run.runx3.common.repository.UserRepository;
import org.run.runx3.psh.dto.CommentDTO;
import org.run.runx3.psh.dto.page.PageRequestDTO;
import org.run.runx3.psh.dto.page.PageResponseDTO;
import org.run.runx3.psh.service.comment.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
@RestController
@RequestMapping("/comments")
public class CommentController {
    @Autowired
    private CommentService commentService;
    @Autowired
    private UserRepository userRepository;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Long> register(@RequestBody CommentDTO commentDTO,
                                      Principal principal) {

        Long userId = 0L;  // 기본 Guest
        String nickname = "Guest";

        if (principal != null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Object principalObj = auth.getPrincipal();

            // 1️⃣ 일반 로그인(CustomUserDetails)
            if (principalObj instanceof CustomUserDetails user) {
                userId = user.getId();
                nickname = user.getUsername();
            }
            // 2️⃣ OAuth2 / 구글 로그인
            else if (principalObj instanceof org.springframework.security.oauth2.core.user.OAuth2User oauthUser) {
                // 이메일 또는 sub로 DB 조회
                String email = (String) oauthUser.getAttribute("email");
                Users users = userRepository.findByEmail(email).orElse(null);

                if (users != null) {
                    userId = users.getUserId();
                    nickname = users.getNickname() != null ? users.getNickname() : users.getUsername();
                } else {
                    // DB에 없는 경우 Guest 처리
                    userId = 0L;
                    nickname = "Guest";
                }
            }
        }

        // 댓글 DTO에 로그인 정보 세팅
        commentDTO.setUserId(userId);
        commentDTO.setNickname(nickname);

        // 댓글 저장
        Long commentId = commentService.insertComment(commentDTO);

        Map<String, Long> map = new HashMap<>();
        map.put("commentId", commentId);
        return map;
    }


    // 댓글 목록
    @GetMapping("/list/{boardId}")
    public PageResponseDTO<CommentDTO> getComments(@PathVariable("boardId") Long boardId,
                                                   PageRequestDTO pageRequestDTO){
        return commentService.getListOfBoard(boardId, pageRequestDTO);
    }

    // 댓글 상세
    @GetMapping("/{commentId}")
    public CommentDTO read(@PathVariable("commentId") Long commentId){
        return commentService.findById(commentId);
    }

    // 댓글 삭제
    @DeleteMapping("/{commentId}")
    public Map<String, Long> remove(@PathVariable("commentId") Long commentId){
        commentService.deleteComment(commentId);
        Map<String, Long> map = new HashMap<>();
        map.put("commentId", commentId);
        return map;
    }

    // 댓글 수정
    @PutMapping(value = "/{commentId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Long> modify(@PathVariable("commentId") Long commentId,
                                    @RequestBody CommentDTO commentDTO){
        commentDTO.setCommentId(commentId);
        commentService.modifyComment(commentDTO);
        Map<String, Long> map = new HashMap<>();
        map.put("commentId", commentId);
        return map;
    }
}

