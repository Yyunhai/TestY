package com.ysalu.repository.auth;

import com.ysalu.domain.auth.UserRole;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 鐢ㄦ埛瑙掕壊鍏宠仈浠撳偍鎺ュ彛銆? */
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    boolean existsByUserAccount_IdAndRole_Code(Long userAccountId, String roleCode);

    List<UserRole> findAllByUserAccount_Id(Long userAccountId);

    void deleteAllByUserAccount_Id(Long userAccountId);
}


