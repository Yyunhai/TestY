package com.ysalu.repository.auth;

import com.ysalu.domain.auth.SystemRole;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 系统角色仓储接口。
 */
public interface SystemRoleRepository extends JpaRepository<SystemRole, Long> {

    List<SystemRole> findAllByOrderByBuiltInDescCodeAsc();

    Optional<SystemRole> findByCode(String code);

    boolean existsByCode(String code);
}
