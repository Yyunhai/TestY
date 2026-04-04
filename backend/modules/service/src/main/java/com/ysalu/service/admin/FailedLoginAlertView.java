package com.ysalu.service.admin;

import java.time.LocalDateTime;

// 失败登录告警视图对象。

public class FailedLoginAlertView {

    private final String principal;
    private final long failureCount;
    private final LocalDateTime latestFailureAt;
    private final String latestRemoteIp;

    public FailedLoginAlertView(String principal, long failureCount, LocalDateTime latestFailureAt, String latestRemoteIp) {
        this.principal = principal;
        this.failureCount = failureCount;
        this.latestFailureAt = latestFailureAt;
        this.latestRemoteIp = latestRemoteIp;
    }

    public String getPrincipal() {
        return principal;
    }

    public long getFailureCount() {
        return failureCount;
    }

    public LocalDateTime getLatestFailureAt() {
        return latestFailureAt;
    }

    public String getLatestRemoteIp() {
        return latestRemoteIp;
    }
}
