package com.ysalu.repository.auth;

import com.ysalu.domain.auth.UserAccount;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

// 用户账户仓储接口。

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<UserAccount> findByUsername(String username);

    Optional<UserAccount> findByEmail(String email);
}

