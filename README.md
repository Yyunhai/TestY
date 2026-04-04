# TestY 项目说明

TestY 是一个基于 Spring Boot 多模块后端和 Vue 3 前端的示例项目，当前已实现认证授权、管理员能力、Markdown 文档管理、登录审计和操作日志能力。

## 目录结构

```text
.
├─ backend
│  ├─ app                  # Spring Boot 启动模块、控制器、配置、集成测试
│  └─ modules
│     ├─ logging           # 日志配置和清理
│     ├─ repository        # JPA 实体与仓储
│     ├─ service           # 核心业务服务
│     └─ document          # Markdown 文档服务
├─ frontend
│  └─ web                  # Vue 3 前端
├─ logs                    # 默认运行日志目录
├─ Dockerfile
├─ docker-compose.yml
└─ pom.xml                 # 根聚合 POM
```

## 技术栈

- 后端：Spring Boot 2.7、Spring MVC、Spring Data JPA
- 前端：Vue 3、Vue CLI
- 数据库：MySQL 8.0
- 测试：JUnit 5、Spring Boot Test、H2

## 本地运行要求

- JDK 8
- Maven 3.9 或兼容版本
- Node.js 18 左右版本
- MySQL 8.0

## 后端运行

后端配置文件位于：

- `backend/app/src/main/resources/application.properties`

在项目根目录执行：

```bash
mvn -pl backend/app -am spring-boot:run
```

默认后端端口为 `8080`。

默认日志目录为项目根目录下的 `logs/`，也可以通过环境变量 `TESTY_LOG_PATH` 覆盖。

## 前端运行

前端目录位于：

- `frontend/web`

进入前端目录后执行：

```bash
npm install
npm run serve
```

默认前端开发端口为 `8081`，并会将 `/api` 请求代理到 `http://localhost:8080`。

## 常用构建命令

后端测试：

```bash
mvn test
```

前端构建：

```bash
cd frontend/web
npm run build
```

## Docker 运行

项目提供了 `Dockerfile` 和 `docker-compose.yml`：

```bash
docker compose up --build
```

启动后：

- MySQL 暴露在 `3306`
- 应用暴露在 `8080`
- 应用日志保存在项目根目录 `logs/`
