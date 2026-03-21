package com.ysalu.web.admin;

import javax.validation.constraints.NotBlank;

/**
 * 更新用户账户状态的请求体。
 */
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
