package com.ysalu.service.log;

import com.ysalu.domain.audit.OperationLog;
import com.ysalu.domain.auth.UserAccount;
import com.ysalu.repository.audit.OperationLogRepository;
import com.ysalu.repository.auth.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 操作日志服务。
 */
@Service
public class OperationLogService {

    private final OperationLogRepository operationLogRepository;
    private final UserAccountRepository userAccountRepository;

    public OperationLogService(
            OperationLogRepository operationLogRepository,
            UserAccountRepository userAccountRepository
    ) {
        this.operationLogRepository = operationLogRepository;
        this.userAccountRepository = userAccountRepository;
    }

    @Transactional
    public void record(
            Long operatorUserId,
            String operatorUsername,
            String module,
            String action,
            String targetType,
            Object targetId,
            boolean success,
            String message,
            String detail,
            String remoteIp
    ) {
        OperationLog operationLog = new OperationLog();
        UserAccount operatorUser = null;
        if (operatorUserId != null) {
            operatorUser = userAccountRepository.findById(operatorUserId).orElse(null);
            operationLog.setOperatorUser(operatorUser);
        }
        operationLog.setOperatorUsername(resolveOperatorUsername(operatorUsername, operatorUser));
        operationLog.setModuleCode(normalizeRequired(module, 64, "UNKNOWN"));
        operationLog.setActionCode(normalizeRequired(action, 64, "UNKNOWN"));
        operationLog.setTargetType(normalizeOptional(targetType, 64));
        operationLog.setTargetId(targetId == null ? null : normalizeOptional(String.valueOf(targetId), 128));
        operationLog.setSuccess(success);
        operationLog.setMessage(normalizeRequired(message, 255, success ? "Operation completed." : "Operation failed."));
        operationLog.setDetail(normalizeOptional(detail, 1000));
        operationLog.setRemoteIp(normalizeOptional(remoteIp, 64));
        operationLogRepository.save(operationLog);
    }

    private String resolveOperatorUsername(String operatorUsername, UserAccount operatorUser) {
        if (operatorUsername != null && !operatorUsername.trim().isEmpty()) {
            return normalizeRequired(operatorUsername, 128, "anonymous");
        }
        if (operatorUser != null && operatorUser.getUsername() != null && !operatorUser.getUsername().trim().isEmpty()) {
            return normalizeRequired(operatorUser.getUsername(), 128, "anonymous");
        }
        return "anonymous";
    }

    private String normalizeRequired(String value, int maxLength, String fallback) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty()) {
            normalized = fallback;
        }
        return truncate(normalized, maxLength);
    }

    private String normalizeOptional(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        return truncate(normalized, maxLength);
    }

    private String truncate(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
