package com.ysalu.service.log;

import com.ysalu.domain.audit.OperationLog;
import com.ysalu.domain.auth.UserAccount;
import com.ysalu.logging.ApplicationLogService;
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
    private final ApplicationLogService applicationLogService;

    public OperationLogService(
            OperationLogRepository operationLogRepository,
            UserAccountRepository userAccountRepository,
            ApplicationLogService applicationLogService
    ) {
        this.operationLogRepository = operationLogRepository;
        this.userAccountRepository = userAccountRepository;
        this.applicationLogService = applicationLogService;
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
        String resolvedModule = normalizeRequired(module, 64, "UNKNOWN");
        String resolvedAction = normalizeRequired(action, 64, "UNKNOWN");
        String resolvedTargetType = normalizeOptional(targetType, 64);
        String resolvedTargetId = targetId == null ? null : normalizeOptional(String.valueOf(targetId), 128);
        String resolvedMessage = normalizeRequired(message, 255, success ? "Operation completed." : "Operation failed.");
        String resolvedDetail = normalizeOptional(detail, 1000);
        String resolvedRemoteIp = normalizeOptional(remoteIp, 64);

        OperationLog operationLog = new OperationLog();
        UserAccount operatorUser = null;
        if (operatorUserId != null) {
            operatorUser = userAccountRepository.findById(operatorUserId).orElse(null);
            operationLog.setOperatorUser(operatorUser);
        }
        String resolvedOperatorUsername = resolveOperatorUsername(operatorUsername, operatorUser);
        operationLog.setOperatorUsername(resolvedOperatorUsername);
        operationLog.setModuleCode(resolvedModule);
        operationLog.setActionCode(resolvedAction);
        operationLog.setTargetType(resolvedTargetType);
        operationLog.setTargetId(resolvedTargetId);
        operationLog.setSuccess(success);
        operationLog.setMessage(resolvedMessage);
        operationLog.setDetail(resolvedDetail);
        operationLog.setRemoteIp(resolvedRemoteIp);
        OperationLog savedLog = operationLogRepository.save(operationLog);
        applicationLogService.operation(
                success,
                resolvedAction,
                resolvedMessage,
                "logId", savedLog.getId(),
                "module", resolvedModule,
                "operatorUserId", operatorUser == null ? operatorUserId : operatorUser.getId(),
                "operatorUsername", resolvedOperatorUsername,
                "targetType", resolvedTargetType,
                "targetId", resolvedTargetId,
                "remoteIp", resolvedRemoteIp,
                "detail", resolvedDetail
        );
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
