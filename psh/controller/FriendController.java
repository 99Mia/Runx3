package org.run.runx3.psh.controller;

import lombok.RequiredArgsConstructor;
import org.run.runx3.psh.dto.FriendDTO;
import org.run.runx3.psh.service.friend.FriendService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/friends")
@RequiredArgsConstructor
public class FriendController {
    private final FriendService friendService;

    // 친구 요청
    @PostMapping("/")
    public FriendDTO addFriend(@RequestBody FriendDTO friendDTO) {
        return friendService.addFriend(friendDTO);
    }

    // 친구 요청 상태 변경
    @PutMapping("/{userId}/{friendId}")
    public void updateStatus(@PathVariable Long userId,
                             @PathVariable Long friendId,
                             @RequestParam String status) {
        friendService.updateFriendStatus(userId, friendId, status);
    }

    // 친구 삭제
    @DeleteMapping("/{userId}/{friendId}")
    public void removeFriend(@PathVariable Long userId,
                             @PathVariable Long friendId) {
        friendService.removeFriend(userId, friendId);
    }

    // 친구 목록 조회
    @GetMapping("/{userId}")
    public List<FriendDTO> getFriends(@PathVariable Long userId) {
        return friendService.getFriends(userId);
    }
}

