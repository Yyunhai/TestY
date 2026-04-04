package com.ysalu.web.admin;

import javax.validation.constraints.NotBlank;

// Update用户状态请求对象。

public class UpdateUserStatusRequest {

    @NotBlank(message = "Account status is required.")
    private String accountStatus;

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }
}
