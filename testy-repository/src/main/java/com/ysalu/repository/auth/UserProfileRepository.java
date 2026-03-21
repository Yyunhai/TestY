package com.ysalu.repository.auth;

import com.ysalu.domain.auth.UserProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 鐢ㄦ埛璧勬枡浠撳偍鎺ュ彛銆? */
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    Optional<UserProfile> findByUserAccount_Id(Long userAccountId);
}


