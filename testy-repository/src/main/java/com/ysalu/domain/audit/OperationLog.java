package com.ysalu.domain.audit;

import com.ysalu.domain.auth.UserAccount;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;

/**
 * 操作日志实体。
 * 用于记录业务操作的操作者、模块、动作、目标对象和执行结果。
 */
@Entity
@Table(name = "operation_log")
public class OperationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_user_id")
    private UserAccount operatorUser;

    @Column(name = "operator_username", nullable = false, length = 128)
    private String operatorUsername;

    @Column(name = "module_code", nullable = false, length = 64)
    private String moduleCode;

    @Column(name = "action_code", nullable = false, length = 64)
    private String actionCode;

    @Column(name = "target_type", length = 64)
    private String targetType;

    @Column(name = "target_id", length = 128)
    private String targetId;

    @Column(nullable = false)
    private boolean success;

    @Column(nullable = false, length = 255)
    private String message;

    @Column(length = 1000)
    private String detail;

    @Column(name = "remote_ip", length = 64)
    private String remoteIp;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    public Long getId() {
        return id;
    }

    public UserAccount getOperatorUser() {
        return operatorUser;
    }

    public void setOperatorUser(UserAccount operatorUser) {
        this.operatorUser = operatorUser;
    }

    public String getOperatorUsername() {
        return operatorUsername;
    }

    public void setOperatorUsername(String operatorUsername) {
        this.operatorUsername = operatorUsername;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    public String getActionCode() {
        return actionCode;
    }

    public void setActionCode(String actionCode) {
        this.actionCode = actionCode;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getRemoteIp() {
        return remoteIp;
    }

    public void setRemoteIp(String remoteIp) {
        this.remoteIp = remoteIp;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    @PrePersist
    public void onCreate() {
        occurredAt = LocalDateTime.now();
    }
}
