package com.ysalu.service;

import com.ysalu.repository.ModuleCatalogRepository;
import org.springframework.stereotype.Service;

@Service
/**
 * 组装首页概览数据，衔接控制层和仓储层。
 */
public class ProjectOverviewService {
    private final ModuleCatalogRepository moduleCatalogRepository;

    public ProjectOverviewService(ModuleCatalogRepository moduleCatalogRepository) {
        this.moduleCatalogRepository = moduleCatalogRepository;
    }

    /**
     * 收集当前应用的名称、运行环境和模块信息。
     */
    public ProjectOverview buildOverview() {
        // 在服务层集中组装首页信息，避免控制层直接拼接多个数据来源。
        return new ProjectOverview(
                "TestY",
                System.getProperty("java.version"),
                moduleCatalogRepository.getWelcomeMessage(),
                moduleCatalogRepository.findAllModuleNames()
        );
    }
}
