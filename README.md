# TestY 项目说明

TestY 是一个基于 Spring Boot 多模块后端和 Vue 3 前端的示例系统，当前已经实现认证授权、管理员能力、Markdown 文档管理，以及登录审计和操作日志能力。

## 项目目标

这个项目的重点不是单页展示，而是提供一套可以继续扩展的业务底座，适合作为以下类型系统的起点：

- 后台管理系统
- 内部协作平台
- 知识库或文档系统
- 带权限控制的内容管理系统

## 功能概览

- 用户注册会同时写入账户表、资料表和默认角色关系。
- 用户登录支持用户名或邮箱，并记录登录 IP、时间、User-Agent、角色快照和权限快照。
- 系统内置 `USER` 和 `ROOT` 两个角色。
- 管理员可以查看用户、角色、权限、登录审计和操作日志。
- 普通用户可以查看项目概览，并创建、编辑、删除、恢复自己的 Markdown 文档。
- 系统会对关键写操作记录操作日志，例如注册、重置密码、退出登录、文档创建/更新/删除/恢复，以及管理员修改角色和用户状态。

## 模块结构

- `testy-repository`
  持久化层，包含 JPA 实体和仓储接口。
- `testy-logging`
  日志模块，负责统一日志输出格式、文件落盘和日志目录配置。
- `testy-service`
  核心业务层，包含认证、权限、管理员、日志等服务。
- `testy-document`
  Markdown 文档相关服务和只读视图对象。
- `Spring-boot-test`
  启动模块，包含控制器、配置、异常处理和集成测试。
- `web`
  Vue 3 前端项目。

## 认证与权限设计

系统使用基于 Session 的登录态，不使用 JWT。登录成功后，后端会把当前用户快照写入 `HttpSession`，并通过拦截器统一保护 `/api/**` 路径下的接口。

当前内置权限包括：

- `overview:read`
- `profile:read`
- `docs:read`
- `docs:write`
- `admin:users:read`
- `admin:users:write`
- `admin:roles:read`
- `admin:roles:write`
- `admin:permissions:read`
- `admin:logins:read`
- `admin:operation-logs:read`
- `system:manage`

## 数据表说明

核心表如下：

- `user_account`
  账户主表，保存用户名、邮箱、密码摘要、状态和最近登录信息。
- `user_profile`
  用户资料表，保存显示名和手机号。
- `system_role`
  系统角色表。
- `system_permission`
  系统权限表。
- `user_role`
  用户和角色关系表。
- `role_permission`
  角色和权限关系表。
- `login_audit`
  登录审计表。
- `operation_log`
  操作日志表。
- `markdown_document`
  Markdown 文档表。
- `markdown_document_version`
  Markdown 文档版本表。

## 日志功能

项目当前包含两类日志：

- 登录审计
  用于记录每次登录尝试，包含登录主体、IP、User-Agent、成功状态、角色快照和权限快照。
- 操作日志
  用于记录关键业务操作，包含操作者、模块、动作、目标对象、结果、说明和发生时间。

同时，运行日志会通过 `testy-logging` 模块写入项目根目录下的 `logs/` 文件夹，默认拆分为：

- `logs/application.log`
- `logs/security.log`
- `logs/operation.log`
- `logs/error.log`

日志模块还包含定期清理任务，默认每天执行一次，删除超过 14 天的历史日志文件。可通过以下环境变量调整：

- `TESTY_LOG_CLEANUP_ENABLED`
- `TESTY_LOG_RETENTION_DAYS`
- `TESTY_LOG_CLEANUP_INITIAL_DELAY_MS`
- `TESTY_LOG_CLEANUP_INTERVAL_MS`

当前会记录的典型操作包括：

- 用户注册
- 密码重置
- 退出登录
- 文档创建
- 文档更新
- 文档删除
- 文档版本恢复
- 创建角色
- 更新角色
- 更新用户角色
- 更新用户状态

## 默认管理员

系统启动时会自动初始化 root 管理员账户，默认配置如下：

- 用户名：`rootadmin`
- 邮箱：`root@testy.local`
- 密码：`Root@123456`

可以通过环境变量覆盖：

- `TESTY_ROOT_USERNAME`
- `TESTY_ROOT_EMAIL`
- `TESTY_ROOT_PASSWORD`

## 本地运行要求

- JDK 8
- Maven 3.9 或兼容版本
- Node.js 18 左右版本
- MySQL 8.0

## 后端运行方式

默认配置会连接本机的 `testy_auth` 数据库，如果数据库不存在会自动创建。

后端配置文件位置：

- `Spring-boot-test/src/main/resources/application.properties`

在项目根目录执行：

```bash
mvn -pl Spring-boot-test -am spring-boot:run
```

默认后端端口为 `8080`。
默认日志目录为项目根目录下的 `logs/`，可通过环境变量 `TESTY_LOG_PATH` 覆盖。

## 前端运行方式

前端目录：

- `web`

进入前端目录后执行：

```bash
npm install
npm run serve
```

默认前端开发端口为 `8081`，并会将 `/api` 请求代理到 `http://localhost:8080`。

## 常用接口

认证接口：

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me`
- `POST /api/auth/reset-password`
- `POST /api/auth/logout`

项目概览接口：

- `GET /api/overview`

管理员接口：

- `GET /api/admin/users`
- `PUT /api/admin/users/{userId}/roles`
- `PUT /api/admin/users/{userId}/status`
- `GET /api/admin/roles`
- `POST /api/admin/roles`
- `PUT /api/admin/roles/{roleId}`
- `GET /api/admin/permissions`
- `GET /api/admin/login-audits`
- `GET /api/admin/login-audits/alerts`
- `GET /api/admin/operation-logs`

Markdown 文档接口：

- `GET /api/docs`
- `GET /api/docs/{id}`
- `POST /api/docs`
- `PUT /api/docs/{id}`
- `DELETE /api/docs/{id}`
- `GET /api/docs/{id}/versions`
- `POST /api/docs/{id}/versions/{versionId}/restore`

## 测试与构建

后端测试：

```bash
mvn test
```

前端构建：

```bash
cd web
npm run build
```

## Docker 运行

项目已经提供 `Dockerfile` 和 `docker-compose.yml`，可以直接进行一体化构建和启动：

```bash
docker compose up --build
```

启动后：

- MySQL 暴露在 `3306`
- 应用暴露在 `8080`
- 应用日志保存在项目根目录 `logs/`

## 后续扩展建议

- 为操作日志增加时间范围、目标类型和结果状态筛选。
- 为登录审计增加失败告警策略和通知能力。
- 为文档模块增加标签、归档和全文检索。
- 增加管理员前端页面，而不只保留管理员接口。
