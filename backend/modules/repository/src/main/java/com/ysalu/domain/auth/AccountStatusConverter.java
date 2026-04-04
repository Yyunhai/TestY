package com.ysalu.domain.auth;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = false)
// 账户状态转换器。
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
