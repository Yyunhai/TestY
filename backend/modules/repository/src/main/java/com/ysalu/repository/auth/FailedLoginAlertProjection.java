package com.ysalu.repository.auth;

import java.time.LocalDateTime;

// 失败登录告警投影。

public interface FailedLoginAlertProjection {

    String getPrincipal();

    long getFailureCount();

    LocalDateTime getLatestFailureAt();

    String getLatestRemoteIp();
}
