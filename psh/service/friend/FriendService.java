package org.run.runx3.psh.service.friend;

import org.run.runx3.psh.dto.FriendDTO;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public interface FriendService {

    FriendDTO addFriend(FriendDTO friendDTO);  // 친구 요청

    void updateFriendStatus(Long userId, Long friendId, String status); // 상태 변경 (ACCEPTED, BLOCKED)

    void removeFriend(Long userId, Long friendId); // 친구 삭제

    List<FriendDTO> getFriends(Long userId); // 친구 목록 조회
}
