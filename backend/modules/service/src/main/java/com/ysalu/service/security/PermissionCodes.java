package com.ysalu.service.security;

import java.util.Arrays;
import java.util.List;

// 权限编码常量。

public final class PermissionCodes {

    public static final String OVERVIEW_READ = "overview:read";
    public static final String PROFILE_READ = "profile:read";
    public static final String DOCS_READ = "docs:read";
    public static final String DOCS_WRITE = "docs:write";
    public static final String ADMIN_USERS_READ = "admin:users:read";
    public static final String ADMIN_USERS_WRITE = "admin:users:write";
    public static final String ADMIN_ROLES_READ = "admin:roles:read";
    public static final String ADMIN_ROLES_WRITE = "admin:roles:write";
    public static final String ADMIN_PERMISSIONS_READ = "admin:permissions:read";
    public static final String ADMIN_LOGINS_READ = "admin:logins:read";
    public static final String ADMIN_OPERATION_LOGS_READ = "admin:operation-logs:read";
    public static final String SYSTEM_MANAGE = "system:manage";

    private PermissionCodes() {
    }

    public static List<String> all() {
        return Arrays.asList(
                OVERVIEW_READ,
                PROFILE_READ,
                DOCS_READ,
                DOCS_WRITE,
                ADMIN_USERS_READ,
                ADMIN_USERS_WRITE,
                ADMIN_ROLES_READ,
                ADMIN_ROLES_WRITE,
                ADMIN_PERMISSIONS_READ,
                ADMIN_LOGINS_READ,
                ADMIN_OPERATION_LOGS_READ,
                SYSTEM_MANAGE
        );
    }
}
