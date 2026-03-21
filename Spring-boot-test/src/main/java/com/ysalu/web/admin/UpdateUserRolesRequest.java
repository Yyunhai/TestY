package com.ysalu.web.admin;

import java.util.List;
import javax.validation.constraints.NotEmpty;

/**
 * 更新用户角色的请求体。
 */
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
