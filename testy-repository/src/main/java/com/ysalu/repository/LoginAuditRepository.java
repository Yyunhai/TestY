package com.ysalu.repository;

import com.ysalu.domain.LoginAudit;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 登录审计仓储接口。
 */
public interface LoginAuditRepository extends JpaRepository<LoginAudit, Long> {

    List<LoginAudit> findTop20ByOrderByLoggedInAtDesc();
}
