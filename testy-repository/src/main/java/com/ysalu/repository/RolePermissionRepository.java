package com.ysalu.repository;

import com.ysalu.domain.RolePermission;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 角色权限关联仓储接口。
 */
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {

    boolean existsByRole_IdAndPermission_Id(Long roleId, Long permissionId);

    List<RolePermission> findAllByRole_IdIn(Collection<Long> roleIds);
}
