package com.ysalu.web.admin;

import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

/**
 * 创建或更新角色的请求体。
 */
public class SaveRoleRequest {

    @NotBlank(message = "Role code is required.")
    @Size(max = 48, message = "Role code must be at most 48 characters.")
    private String code;

    @NotBlank(message = "Role name is required.")
    @Size(max = 100, message = "Role name must be at most 100 characters.")
    private String name;

    @Size(max = 255, message = "Role description must be at most 255 characters.")
    private String description;

    @NotEmpty(message = "At least one permission is required.")
    private List<Long> permissionIds;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Long> getPermissionIds() {
        return permissionIds;
    }

    public void setPermissionIds(List<Long> permissionIds) {
        this.permissionIds = permissionIds;
    }
}
