package com.ysalu.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
/**
 * 提供认证模块需要的安全相关基础 Bean。
 */
public class SecuritySupportConfig {

    @Bean
    /**
     * 使用 BCrypt 对密码进行单向加密存储。
     */
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
