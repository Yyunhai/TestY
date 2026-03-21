package com.ysalu.service.auth;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 认证成功后的用户视图。
 * 聚合了账户、资料、角色、权限以及最近登录信息。
 */
public class AuthenticatedUser {

    private final Long id;
    private final String username;
    private final String email;
    private final String displayName;
    private final String phoneNumber;
    private final String accountStatus;
    private final List<String> roles;
    private final List<String> permissions;
    private final boolean rootAdmin;
    private LocalDateTime lastLoginAt;
    private String lastLoginIp;

    public AuthenticatedUser(
            Long id,
            String username,
            String email,
            String displayName,
            String phoneNumber,
            String accountStatus,
            List<String> roles,
            List<String> permissions,
            boolean rootAdmin,
            LocalDateTime lastLoginAt,
            String lastLoginIp
    ) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.displayName = displayName;
        this.phoneNumber = phoneNumber;
        this.accountStatus = accountStatus;
        this.roles = Collections.unmodifiableList(new ArrayList<String>(roles));
        this.permissions = Collections.unmodifiableList(new ArrayList<String>(permissions));
        this.rootAdmin = rootAdmin;
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

    public String getPhoneNumber() {
        return phoneNumber;
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

    public boolean isRootAdmin() {
        return rootAdmin;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public String getLastLoginIp() {
        return lastLoginIp;
    }

    public void setLastLoginIp(String lastLoginIp) {
        this.lastLoginIp = lastLoginIp;
    }
}
