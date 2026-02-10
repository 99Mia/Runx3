package org.run.runx3.psh.service.friend;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.run.runx3.common.domain.Users;
import org.run.runx3.common.repository.UserRepository;
import org.run.runx3.psh.domain.Friends;
import org.run.runx3.psh.dto.FriendDTO;
import org.run.runx3.psh.repository.FriendRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Log4j2
@Transactional
@RequiredArgsConstructor
public class FriendServiceImpl implements FriendService {

    private final FriendRepository friendRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public FriendDTO addFriend(FriendDTO friendDTO) {
        Users user = userRepository.findById(friendDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        Users friend = userRepository.findById(friendDTO.getFriendId())
                .orElseThrow(() -> new RuntimeException("친구를 찾을 수 없습니다."));

        // 이미 친구 요청이 있는지 체크
        friendRepository.findByUsers_UserIdAndFriend_UserId(user.getUserId(), friend.getUserId())
                .ifPresent(f -> { throw new RuntimeException("이미 요청한 친구입니다."); });

        Friends friends = Friends.builder()
                .users(user)
                .friend(friend)
                .status(Friends.Status.PENDING)
                .build();

        friendRepository.save(friends);

        return FriendDTO.builder()
                .userId(user.getUserId())
                .friendId(friend.getUserId())
                .status(friends.getStatus().name())
                .build();
    }

    @Override
    @Transactional
    public void updateFriendStatus(Long userId, Long friendId, String status) {
        Friends friends = friendRepository.findByUsers_UserIdAndFriend_UserId(userId, friendId)
                .orElseThrow(() -> new RuntimeException("친구 요청이 존재하지 않습니다."));

        friends.setStatus(Friends.Status.valueOf(status.toUpperCase()));
        friendRepository.save(friends);
    }

    @Override
    @Transactional
    public void removeFriend(Long userId, Long friendId) {
        Friends friends = friendRepository.findByUsers_UserIdAndFriend_UserId(userId, friendId)
                .orElseThrow(() -> new RuntimeException("친구 관계가 존재하지 않습니다."));
        friendRepository.delete(friends);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendDTO> getFriends(Long userId) {
        return friendRepository.findAllByUsers_UserId(userId).stream()
                .map(f -> FriendDTO.builder()
                        .userId(f.getUsers().getUserId())
                        .friendId(f.getFriend().getUserId())
                        .status(f.getStatus().name())
                        .build())
                .collect(Collectors.toList());
    }
}