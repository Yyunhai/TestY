package com.ysalu.service.admin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 角色管理只读视图。
 */
public class RoleView {

    private final Long id;
    private final String code;
    private final String name;
    private final String description;
    private final boolean builtIn;
    private final List<String> permissions;

    public RoleView(Long id, String code, String name, String description, boolean builtIn, List<String> permissions) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.builtIn = builtIn;
        this.permissions = Collections.unmodifiableList(new ArrayList<String>(permissions));
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

    public boolean isBuiltIn() {
        return builtIn;
    }

    public List<String> getPermissions() {
        return permissions;
    }
}
