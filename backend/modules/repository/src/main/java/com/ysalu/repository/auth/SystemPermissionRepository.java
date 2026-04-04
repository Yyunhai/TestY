package com.ysalu.repository.auth;

import com.ysalu.domain.auth.SystemPermission;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

// System权限仓储接口。

public interface SystemPermissionRepository extends JpaRepository<SystemPermission, Long> {

    List<SystemPermission> findAllByOrderByCodeAsc();

    Optional<SystemPermission> findByCode(String code);

    List<SystemPermission> findAllByIdIn(Collection<Long> ids);
}
