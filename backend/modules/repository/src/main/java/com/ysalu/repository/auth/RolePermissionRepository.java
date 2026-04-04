package com.ysalu.repository.auth;

import com.ysalu.domain.auth.RolePermission;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

// 角色权限仓储接口。

public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {

    boolean existsByRole_IdAndPermission_Id(Long roleId, Long permissionId);

    List<RolePermission> findAllByRole_Id(Long roleId);

    List<RolePermission> findAllByRole_IdIn(Collection<Long> roleIds);

    void deleteAllByRole_Id(Long roleId);
}
