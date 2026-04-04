package com.ysalu.repository.auth;

import com.ysalu.domain.auth.UserProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

// 用户资料仓储接口。

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    Optional<UserProfile> findByUserAccount_Id(Long userAccountId);
}

