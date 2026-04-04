package com.ysalu.service.overview;

import com.ysalu.repository.overview.ModuleCatalogRepository;
import org.springframework.stereotype.Service;

@Service
// 项目概览服务。
public class ProjectOverviewService {

    private final ModuleCatalogRepository moduleCatalogRepository;

    public ProjectOverviewService(ModuleCatalogRepository moduleCatalogRepository) {
        this.moduleCatalogRepository = moduleCatalogRepository;
    }

    public ProjectOverview buildOverview() {
        return new ProjectOverview(
                "TestY",
                System.getProperty("java.version"),
                moduleCatalogRepository.getWelcomeMessage(),
                moduleCatalogRepository.findAllModuleNames()
        );
    }
}
