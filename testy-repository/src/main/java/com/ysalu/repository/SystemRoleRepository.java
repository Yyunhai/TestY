package com.ysalu.repository;

import com.ysalu.domain.SystemRole;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 系统角色仓储接口。
 */
public interface SystemRoleRepository extends JpaRepository<SystemRole, Long> {

    Optional<SystemRole> findByCode(String code);
}
