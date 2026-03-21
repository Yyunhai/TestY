package com.ysalu.service.admin;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 管理员查看用户列表时使用的只读视图。
 */
public class AdminUserView {

    private final Long id;
    private final String username;
    private final String email;
    private final String displayName;
    private final String accountStatus;
    private final List<String> roles;
    private final List<String> permissions;
    private final LocalDateTime lastLoginAt;
    private final String lastLoginIp;

    public AdminUserView(
            Long id,
            String username,
            String email,
            String displayName,
            String accountStatus,
            List<String> roles,
            List<String> permissions,
            LocalDateTime lastLoginAt,
            String lastLoginIp
    ) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.displayName = displayName;
        this.accountStatus = accountStatus;
        this.roles = Collections.unmodifiableList(new ArrayList<String>(roles));
        this.permissions = Collections.unmodifiableList(new ArrayList<String>(permissions));
        this.lastLoginAt = lastLoginAt;
        this.lastLoginIp = lastLoginIp;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public List<String> getRoles() {
        return roles;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public String getLastLoginIp() {
        return lastLoginIp;
    }
}
