package com.ysalu;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 应用启动入口。
 *
 * <p>这里除了启动 Spring Boot，也会在启动前补齐默认日志目录，
 * 避免因为不同启动目录导致日志文件落到项目外部。</p>
 */
@SpringBootApplication
public class TestYApplication {

    /**
     * 启动应用。
     */
    public static void main(String[] args) {
        configureDefaultLogPath();
        SpringApplication.run(TestYApplication.class, args);
    }

    /**
     * 在未显式配置日志目录时，为应用设置默认日志路径。
     *
     * <p>优先尊重环境变量和 JVM 参数；只有在用户没有传入配置时，
     * 才自动将日志目录固定到项目根目录下的 logs。</p>
     */
    private static void configureDefaultLogPath() {
        if (hasText(System.getenv("TESTY_LOG_PATH")) || hasText(System.getProperty("testy.logging.base-path"))) {
            return;
        }
        Path projectRoot = resolveProjectRoot();
        System.setProperty("testy.logging.base-path", projectRoot.resolve("logs").normalize().toString());
    }

    /**
     * 推断项目根目录。
     *
     * <p>优先使用当前工作目录；如果当前目录是启动模块目录，
     * 则回退到其父目录作为整个项目根目录。</p>
     */
    private static Path resolveProjectRoot() {
        Path userDir = Paths.get(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
        if (looksLikeProjectRoot(userDir)) {
            return userDir;
        }
        Path parent = userDir.getParent();
        if (parent != null && looksLikeProjectRoot(parent)) {
            return parent;
        }
        return userDir;
    }

    /**
     * 判断指定目录是否像项目根目录。
     */
    private static boolean looksLikeProjectRoot(Path path) {
        return Files.exists(path.resolve("pom.xml"))
                && Files.isDirectory(path.resolve("Spring-boot-test"))
                && Files.isDirectory(path.resolve("web"));
    }

    /**
     * 判断字符串是否包含有效内容。
     */
    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
