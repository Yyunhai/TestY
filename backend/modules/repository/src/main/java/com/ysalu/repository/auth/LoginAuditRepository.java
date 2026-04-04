package com.ysalu.repository.auth;

import com.ysalu.domain.auth.LoginAudit;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// 登录审计仓储接口。

public interface LoginAuditRepository extends JpaRepository<LoginAudit, Long>, JpaSpecificationExecutor<LoginAudit> {

    List<LoginAudit> findTop20ByOrderByLoggedInAtDesc();

    long countBySuccessFalseAndLoggedInAtAfter(LocalDateTime since);

    @Query("select audit.principal as principal, count(audit) as failureCount, max(audit.loggedInAt) as latestFailureAt, max(audit.remoteIp) as latestRemoteIp "
            + "from LoginAudit audit "
            + "where audit.success = false and audit.loggedInAt >= :since "
            + "group by audit.principal "
            + "having count(audit) >= :threshold "
            + "order by count(audit) desc")
    List<FailedLoginAlertProjection> findFrequentFailedPrincipals(
            @Param("since") LocalDateTime since,
            @Param("threshold") long threshold
    );
}
