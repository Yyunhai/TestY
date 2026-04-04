package com.ysalu;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// TestY 应用启动入口。
@SpringBootApplication
public class TestYApplication {

    // 启动 Spring Boot 前，先补齐默认日志目录。
    public static void main(String[] args) {
        configureDefaultLogPath();
        SpringApplication.run(TestYApplication.class, args);
    }

    // 仅在外部未显式配置时，把日志目录固定到项目根目录。
    private static void configureDefaultLogPath() {
        if (hasText(System.getenv("TESTY_LOG_PATH")) || hasText(System.getProperty("testy.logging.base-path"))) {
            return;
        }
        Path projectRoot = resolveProjectRoot();
        System.setProperty("testy.logging.base-path", projectRoot.resolve("logs").normalize().toString());
    }

    // 从当前工作目录向上查找真正的项目根目录。
    private static Path resolveProjectRoot() {
        Path userDir = Paths.get(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
        for (Path current = userDir; current != null; current = current.getParent()) {
            if (looksLikeProjectRoot(current)) {
                return current;
            }
        }
        return userDir;
    }

    // 用前后端目录和根 POM 判断一个目录是否是项目根。
    private static boolean looksLikeProjectRoot(Path path) {
        return Files.exists(path.resolve("pom.xml"))
                && Files.isDirectory(path.resolve("backend").resolve("app"))
                && Files.isDirectory(path.resolve("frontend").resolve("web"));
    }

    // 判断字符串是否包含有效内容。
    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
