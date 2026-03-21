package com.ysalu.service.overview;

import com.ysalu.repository.overview.ModuleCatalogRepository;
import org.springframework.stereotype.Service;

/**
 * 组装首页概览数据。
 * 该服务连接控制层与模块目录仓储，统一生成项目运行信息。
 */
@Service
public class ProjectOverviewService {

    private final ModuleCatalogRepository moduleCatalogRepository;

    public ProjectOverviewService(ModuleCatalogRepository moduleCatalogRepository) {
        this.moduleCatalogRepository = moduleCatalogRepository;
    }

    /**
     * 收集应用名称、Java 版本、欢迎语和模块列表。
     */
    public ProjectOverview buildOverview() {
        return new ProjectOverview(
                "TestY",
                System.getProperty("java.version"),
                moduleCatalogRepository.getWelcomeMessage(),
                moduleCatalogRepository.findAllModuleNames()
        );
    }
}
