package com.ysalu.repository;

import com.ysalu.domain.UserRole;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 用户角色关联仓储接口。
 */
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    boolean existsByUserAccount_IdAndRole_Code(Long userAccountId, String roleCode);

    List<UserRole> findAllByUserAccount_Id(Long userAccountId);
}
