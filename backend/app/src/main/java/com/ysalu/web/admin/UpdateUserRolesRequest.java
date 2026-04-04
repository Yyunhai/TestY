package com.ysalu.web.admin;

import java.util.List;
import javax.validation.constraints.NotEmpty;

// Update用户Roles请求对象。

public class UpdateUserRolesRequest {

    @NotEmpty(message = "At least one role is required.")
    private List<Long> roleIds;

    public List<Long> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<Long> roleIds) {
        this.roleIds = roleIds;
    }
}
