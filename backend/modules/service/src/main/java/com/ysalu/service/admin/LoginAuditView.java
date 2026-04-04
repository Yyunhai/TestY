package com.ysalu.service.admin;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// 登录审计视图对象。

public class LoginAuditView {

    private final Long id;
    private final Long userId;
    private final String principal;
    private final String remoteIp;
    private final String userAgent;
    private final boolean success;
    private final String message;
    private final List<String> roles;
    private final List<String> permissions;
    private final LocalDateTime loggedInAt;

    public LoginAuditView(
            Long id,
            Long userId,
            String principal,
            String remoteIp,
            String userAgent,
            boolean success,
            String message,
            List<String> roles,
            List<String> permissions,
            LocalDateTime loggedInAt
    ) {
        this.id = id;
        this.userId = userId;
        this.principal = principal;
        this.remoteIp = remoteIp;
        this.userAgent = userAgent;
        this.success = success;
        this.message = message;
        this.roles = Collections.unmodifiableList(new ArrayList<String>(roles));
        this.permissions = Collections.unmodifiableList(new ArrayList<String>(permissions));
        this.loggedInAt = loggedInAt;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getPrincipal() {
        return principal;
    }

    public String getRemoteIp() {
        return remoteIp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public List<String> getRoles() {
        return roles;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public LocalDateTime getLoggedInAt() {
        return loggedInAt;
    }
}
