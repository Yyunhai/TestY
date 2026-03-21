package com.ysalu.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 提供认证模块所需的基础安全 Bean。
 */
@Configuration
public class SecuritySupportConfig {

    /**
     * 使用 BCrypt 对密码进行单向加密存储。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
