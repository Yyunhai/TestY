package com.ysalu.repository;

import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
/**
 * 提供项目模块概览所需的静态信息。
 */
public class ModuleCatalogRepository {

    /**
     * 返回当前项目暴露给前端展示的模块名称。
     */
    public List<String> findAllModuleNames() {
        return Arrays.asList("testy-repository", "testy-service", "Spring-boot-test");
    }

    /**
     * 返回项目启动成功时的欢迎文案。
     */
    public String getWelcomeMessage() {
        return "Spring Boot multi-module application is running.";
    }
}
