package com.ysalu.repository;

import com.ysalu.domain.SystemPermission;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 系统权限仓储接口。
 */
public interface SystemPermissionRepository extends JpaRepository<SystemPermission, Long> {

    Optional<SystemPermission> findByCode(String code);
}
