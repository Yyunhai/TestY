package com.ysalu.repository.audit;

import com.ysalu.domain.audit.OperationLog;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 操作日志仓储接口。
 */
public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {
}
