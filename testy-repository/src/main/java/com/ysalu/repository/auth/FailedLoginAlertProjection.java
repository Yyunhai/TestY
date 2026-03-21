package com.ysalu.repository.auth;

import java.time.LocalDateTime;

/**
 * 登录失败聚合告警投影。
 */
public interface FailedLoginAlertProjection {

    String getPrincipal();

    long getFailureCount();

    LocalDateTime getLatestFailureAt();

    String getLatestRemoteIp();
}
