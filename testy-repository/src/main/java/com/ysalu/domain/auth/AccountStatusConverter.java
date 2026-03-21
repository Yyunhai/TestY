package com.ysalu.domain.auth;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * 账户状态字段转换器。
 * 用于兼容数据库中的历史脏值，避免 Hibernate 在读取非法枚举值时直接抛异常。
 */
@Converter(autoApply = false)
public class AccountStatusConverter implements AttributeConverter<AccountStatus, String> {

    @Override
    public String convertToDatabaseColumn(AccountStatus attribute) {
        AccountStatus status = attribute == null ? AccountStatus.UNKNOWN : attribute;
        return status.name();
    }

    @Override
    public AccountStatus convertToEntityAttribute(String dbData) {
        return AccountStatus.fromDatabaseValue(dbData);
    }
}
