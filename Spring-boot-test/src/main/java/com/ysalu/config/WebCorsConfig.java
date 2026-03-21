package com.ysalu.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 配置前后端分离开发时的跨域访问策略。
 * 这里只对 `/api/**` 开放本地开发地址，并允许携带 Cookie 以支持 Session 登录。
 */
@Configuration
public class WebCorsConfig implements WebMvcConfigurer {

    /**
     * 为 API 路径统一注册跨域规则。
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
