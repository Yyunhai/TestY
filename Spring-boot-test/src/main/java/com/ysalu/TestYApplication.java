package com.ysalu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
/**
 * Spring Boot 启动入口，负责装配整个多模块应用。
 */
public class TestYApplication {

    /**
     * 启动后端服务。
     */
    public static void main(String[] args) {
        SpringApplication.run(TestYApplication.class, args);
    }
}
