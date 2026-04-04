package com.ysalu.domain.auth;

// 账户状态枚举。

public enum AccountStatus {
    ACTIVE,
    LOCKED,
    DISABLED,
    UNKNOWN;

    public static AccountStatus fromDatabaseValue(String value) {
        if (value == null) {
            return UNKNOWN;
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return UNKNOWN;
        }
        try {
            return AccountStatus.valueOf(normalized.toUpperCase());
        } catch (IllegalArgumentException exception) {
            return UNKNOWN;
        }
    }
}
