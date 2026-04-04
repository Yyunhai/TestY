package com.ysalu.repository.overview;

import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
// 项目模块目录仓储。
public class ModuleCatalogRepository {

    public List<String> findAllModuleNames() {
        return Arrays.asList("app", "service", "repository", "document", "logging");
    }

    public String getWelcomeMessage() {
        return "Spring Boot multi-module application is running.";
    }
}
