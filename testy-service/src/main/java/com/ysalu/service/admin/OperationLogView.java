package com.ysalu.service.admin;

import java.time.LocalDateTime;

/**
 * 管理端操作日志视图。
 */
public class OperationLogView {

    private final Long id;
    private final Long operatorUserId;
    private final String operatorUsername;
    private final String module;
    private final String action;
    private final String targetType;
    private final String targetId;
    private final boolean success;
    private final String message;
    private final String detail;
    private final String remoteIp;
    private final LocalDateTime occurredAt;

    public OperationLogView(
            Long id,
            Long operatorUserId,
            String operatorUsername,
            String module,
            String action,
            String targetType,
            String targetId,
            boolean success,
            String message,
            String detail,
            String remoteIp,
            LocalDateTime occurredAt
    ) {
        this.id = id;
        this.operatorUserId = operatorUserId;
        this.operatorUsername = operatorUsername;
        this.module = module;
        this.action = action;
        this.targetType = targetType;
        this.targetId = targetId;
        this.success = success;
        this.message = message;
        this.detail = detail;
        this.remoteIp = remoteIp;
        this.occurredAt = occurredAt;
    }

    public Long getId() {
        return id;
    }

    public Long getOperatorUserId() {
        return operatorUserId;
    }

    public String getOperatorUsername() {
        return operatorUsername;
    }

    public String getModule() {
        return module;
    }

    public String getAction() {
        return action;
    }

    public String getTargetType() {
        return targetType;
    }

    public String getTargetId() {
        return targetId;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getDetail() {
        return detail;
    }

    public String getRemoteIp() {
        return remoteIp;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}
