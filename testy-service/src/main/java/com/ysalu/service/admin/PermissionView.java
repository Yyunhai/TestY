package com.ysalu.service.admin;

/**
 * 权限管理只读视图。
 */
public class PermissionView {

    private final Long id;
    private final String code;
    private final String name;
    private final String description;

    public PermissionView(Long id, String code, String name, String description) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
