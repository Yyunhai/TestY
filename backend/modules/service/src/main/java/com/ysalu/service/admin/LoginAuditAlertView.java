package com.ysalu.service.admin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// 登录审计告警视图对象。

public class LoginAuditAlertView {

    private final long failedAttemptsLast24Hours;
    private final List<FailedLoginAlertView> suspiciousPrincipals;

    public LoginAuditAlertView(long failedAttemptsLast24Hours, List<FailedLoginAlertView> suspiciousPrincipals) {
        this.failedAttemptsLast24Hours = failedAttemptsLast24Hours;
        this.suspiciousPrincipals = Collections.unmodifiableList(new ArrayList<FailedLoginAlertView>(suspiciousPrincipals));
    }

    public long getFailedAttemptsLast24Hours() {
        return failedAttemptsLast24Hours;
    }

    public List<FailedLoginAlertView> getSuspiciousPrincipals() {
        return suspiciousPrincipals;
    }
}
