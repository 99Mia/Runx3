package org.run.runx3.psh.repository;

import org.run.runx3.common.domain.Profiles;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profiles,Long> {
    Optional<Profiles> findByUsers_UserId(Long userId);
}
