package com.ysalu.service;

import java.util.Arrays;
import java.util.List;

/**
 * 系统内置权限编码常量。
 * 所有角色授权和接口鉴权都围绕这些编码展开。
 */
public final class PermissionCodes {

    public static final String OVERVIEW_READ = "overview:read";
    public static final String PROFILE_READ = "profile:read";
    public static final String DOCS_READ = "docs:read";
    public static final String DOCS_WRITE = "docs:write";
    public static final String ADMIN_USERS_READ = "admin:users:read";
    public static final String ADMIN_LOGINS_READ = "admin:logins:read";
    public static final String SYSTEM_MANAGE = "system:manage";

    private PermissionCodes() {
    }

    /**
     * 返回全部内置权限编码。
     */
    public static List<String> all() {
        return Arrays.asList(
                OVERVIEW_READ,
                PROFILE_READ,
                DOCS_READ,
                DOCS_WRITE,
                ADMIN_USERS_READ,
                ADMIN_LOGINS_READ,
                SYSTEM_MANAGE
        );
    }
}
