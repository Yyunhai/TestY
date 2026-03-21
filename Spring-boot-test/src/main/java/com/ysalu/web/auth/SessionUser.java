package com.ysalu.web.auth;

import com.ysalu.service.AuthenticatedUser;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 会话中保存的用户快照。
 * 该对象只保留鉴权和界面展示所需字段，避免每次请求都重新拼装完整登录态。
 */
public class SessionUser implements Serializable {

    public static final String SESSION_USER_ATTRIBUTE = "sessionUser";

    private static final long serialVersionUID = 1L;

    private final Long id;
    private final String username;
    private final String email;
    private final String displayName;
    private final String accountStatus;
    private final List<String> roles;
    private final List<String> permissions;
    private final boolean rootAdmin;
    private final LocalDateTime lastLoginAt;
    private final String lastLoginIp;

    public SessionUser(
            Long id,
            String username,
            String email,
            String displayName,
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
        this.accountStatus = accountStatus;
        this.roles = Collections.unmodifiableList(new ArrayList<String>(roles));
        this.permissions = Collections.unmodifiableList(new ArrayList<String>(permissions));
        this.rootAdmin = rootAdmin;
        this.lastLoginAt = lastLoginAt;
        this.lastLoginIp = lastLoginIp;
    }

    /**
     * 根据服务层认证结果构造会话对象。
     */
    public static SessionUser from(AuthenticatedUser authenticatedUser) {
        return new SessionUser(
                authenticatedUser.getId(),
                authenticatedUser.getUsername(),
                authenticatedUser.getEmail(),
                authenticatedUser.getDisplayName(),
                authenticatedUser.getAccountStatus(),
                authenticatedUser.getRoles(),
                authenticatedUser.getPermissions(),
                authenticatedUser.isRootAdmin(),
                authenticatedUser.getLastLoginAt(),
                authenticatedUser.getLastLoginIp()
        );
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
