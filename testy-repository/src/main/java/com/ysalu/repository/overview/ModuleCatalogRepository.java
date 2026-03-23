package com.ysalu.repository.overview;

import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * 模块目录仓储。
 * 提供项目首页展示所需的模块名称和欢迎信息。
 */
@Repository
public class ModuleCatalogRepository {

    /**
     * 返回项目当前公开给前端展示的模块名称。
     */
    public List<String> findAllModuleNames() {
        return Arrays.asList("testy-logging", "testy-repository", "testy-service", "testy-document", "Spring-boot-test");
    }

    /**
     * 返回项目首页显示的欢迎文案。
     */
    public String getWelcomeMessage() {
        return "Spring Boot multi-module application is running.";
    }
}
