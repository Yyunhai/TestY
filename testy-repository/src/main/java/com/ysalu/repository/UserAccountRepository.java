package com.ysalu.repository;

import com.ysalu.domain.UserAccount;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 用户账号数据访问接口，负责常用查询和唯一性校验。
 */
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<UserAccount> findByUsername(String username);

    Optional<UserAccount> findByEmail(String email);
}
