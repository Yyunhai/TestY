package com.ysalu.web.auth;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// 认证响应对象。

public class AuthResponse {

    private final boolean success;
    private final String message;
    private final String username;
    private final String email;
    private final String displayName;
    private final String phoneNumber;
    private final String accountStatus;
    private final List<String> roles;
    private final List<String> permissions;
    private final boolean rootAdmin;
    private final LocalDateTime lastLoginAt;
    private final String lastLoginIp;

    public AuthResponse(
            boolean success,
            String message,
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
        this.success = success;
        this.message = message;
        this.username = username;
        this.email = email;
        this.displayName = displayName;
        this.phoneNumber = phoneNumber;
        this.accountStatus = accountStatus;
        this.roles = roles == null ? Collections.<String>emptyList() : Collections.unmodifiableList(new ArrayList<String>(roles));
        this.permissions = permissions == null ? Collections.<String>emptyList() : Collections.unmodifiableList(new ArrayList<String>(permissions));
        this.rootAdmin = rootAdmin;
        this.lastLoginAt = lastLoginAt;
        this.lastLoginIp = lastLoginIp;
    }

    public AuthResponse(boolean success, String message, String username, String email) {
        this(success, message, username, email, null, null, null, Collections.<String>emptyList(),
                Collections.<String>emptyList(), false, null, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
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

    public String getLastLoginIp() {
        return lastLoginIp;
    }
}
