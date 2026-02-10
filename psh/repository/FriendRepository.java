package org.run.runx3.psh.repository;

import org.run.runx3.psh.domain.Friends;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friends,Long> {
    // 특정 유저의 친구 관계 조회
    List<Friends> findAllByUsers_UserId(Long userId);

    // 특정 유저와 친구의 관계 조회
    Optional<Friends> findByUsers_UserIdAndFriend_UserId(Long userId, Long friendId);
}

