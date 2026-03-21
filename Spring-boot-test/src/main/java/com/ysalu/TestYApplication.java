package com.ysalu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot 应用启动入口。
 * 该模块负责装配 Web 层、服务层、数据层与文档模块，并启动整个系统。
 */
@SpringBootApplication
public class TestYApplication {

    /**
     * 启动后端服务。
     */
    public static void main(String[] args) {
        SpringApplication.run(TestYApplication.class, args);
    }
}
