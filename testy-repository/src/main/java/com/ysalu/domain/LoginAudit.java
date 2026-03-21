package com.ysalu.domain;

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
 * 登录审计实体。
 * 用于记录每次登录尝试的主体、IP、结果与权限快照。
 */
@Entity
@Table(name = "login_audit")
public class LoginAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_account_id")
    private UserAccount userAccount;

    @Column(name = "principal", nullable = false, length = 128)
    private String principal;

    @Column(name = "remote_ip", nullable = false, length = 64)
    private String remoteIp;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(nullable = false)
    private boolean success;

    @Column(nullable = false, length = 255)
    private String message;

    @Column(name = "roles_snapshot", length = 500)
    private String rolesSnapshot;

    @Column(name = "permissions_snapshot", length = 2000)
    private String permissionsSnapshot;

    @Column(name = "logged_in_at", nullable = false)
    private LocalDateTime loggedInAt;

    public Long getId() {
        return id;
    }

    public UserAccount getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(UserAccount userAccount) {
        this.userAccount = userAccount;
    }

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public String getRemoteIp() {
        return remoteIp;
    }

    public void setRemoteIp(String remoteIp) {
        this.remoteIp = remoteIp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
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

    public String getRolesSnapshot() {
        return rolesSnapshot;
    }

    public void setRolesSnapshot(String rolesSnapshot) {
        this.rolesSnapshot = rolesSnapshot;
    }

    public String getPermissionsSnapshot() {
        return permissionsSnapshot;
    }

    public void setPermissionsSnapshot(String permissionsSnapshot) {
        this.permissionsSnapshot = permissionsSnapshot;
    }

    public LocalDateTime getLoggedInAt() {
        return loggedInAt;
    }

    @PrePersist
    public void onCreate() {
        loggedInAt = LocalDateTime.now();
    }
}
