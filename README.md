# TestY

TestY 是一个基于 `Spring Boot 2.7 + Vue 3` 的多模块示例项目，包含统一认证、Markdown 文档管理、后台管理、登录审计和操作日志能力。项目采用前后端分离开发方式，前端构建产物可直接输出到后端静态目录中，便于本地开发和一体化部署。

## 功能概览

- 统一认证
  - 注册、登录、登出、重置密码
  - 基于 `HttpSession` 的会话认证，不使用 JWT
  - 登录记录包含时间、IP、角色快照、权限快照
- 文档中心
  - Markdown 文档的创建、编辑、删除、版本恢复
  - 文档列表支持搜索，搜索条件保存在 URL hash 中
  - 自动保存与实时预览
- 后台管理
  - 用户管理、角色管理、权限目录
  - 登录审计查询
  - 操作日志查询
  - 普通 `USER` 账号不会显示后台管理入口
- 日志能力
  - 独立日志模块 `testy-logging`
  - 应用日志、登录安全日志、操作日志、错误日志分别落盘
  - 支持日志定期清理
- 前端体验
  - 登录页自动记住上次输入的用户名
  - 可选“记住密码”

## 技术栈

- 后端
  - Java 8
  - Spring Boot 2.7.0
  - Spring MVC
  - Spring Data JPA
  - MySQL 8
- 前端
  - Vue 3
  - Vue CLI 5
  - `markdown-it`
  - `highlight.js`
- 构建与部署
  - Maven 多模块
  - Docker / Docker Compose

## 项目结构

```text
TestY
├─ Spring-boot-test      # 启动模块，控制器、配置、异常处理、集成测试
├─ testy-document        # 文档领域服务
├─ testy-logging         # 日志模块，文件输出与清理任务
├─ testy-repository      # JPA 实体与 Repository
├─ testy-service         # 认证、管理员、日志等核心业务服务
├─ web                   # Vue 3 前端
├─ Dockerfile
├─ docker-compose.yml
└─ pom.xml               # 父工程
```

## 内置角色与权限

系统默认初始化两个内置角色：

- `USER`
  - `overview:read`
  - `profile:read`
  - `docs:read`
  - `docs:write`
- `ROOT`
  - 拥有系统全部权限

后台相关权限包括：

- `admin:users:read`
- `admin:users:write`
- `admin:roles:read`
- `admin:roles:write`
- `admin:permissions:read`
- `admin:logins:read`
- `admin:operation-logs:read`
- `system:manage`

说明：

- 没有任何 `admin:*` 权限的账号，前端不会显示“后台管理”按钮。
- 默认注册用户会获得 `USER` 角色，不会自动获得后台管理能力。

## 默认管理员账号

应用启动时会自动初始化 root 管理员账号，默认值如下：

- 用户名：`rootadmin`
- 邮箱：`root@testy.local`
- 密码：`Root@123456`

可通过环境变量覆盖：

- `TESTY_ROOT_USERNAME`
- `TESTY_ROOT_EMAIL`
- `TESTY_ROOT_PASSWORD`

## 日志说明

日志统一由 `testy-logging` 模块管理，默认输出到项目根目录下的 `logs/`。

默认日志文件：

- `logs/application.log`
- `logs/security.log`
- `logs/operation.log`
- `logs/error.log`

日志清理相关环境变量：

- `TESTY_LOG_PATH`
- `TESTY_LOG_CLEANUP_ENABLED`
- `TESTY_LOG_RETENTION_DAYS`
- `TESTY_LOG_CLEANUP_INITIAL_DELAY_MS`
- `TESTY_LOG_CLEANUP_INTERVAL_MS`

默认策略：

- 启动后延迟 5 分钟开始第一次清理
- 之后每 24 小时清理一次
- 默认保留 14 天日志文件

## 运行环境

- JDK 8
- Maven 3.9 或兼容版本
- Node.js 18 左右
- MySQL 8.0

## 本地运行

### 1. 准备数据库

默认后端连接：

- 数据库：`testy_auth`
- 地址：`jdbc:mysql://localhost:3306/testy_auth`
- 用户名：`root`
- 密码：`200612yhx`

如果本地 MySQL 未启动，后端会在启动时因数据库连接失败而无法完成初始化。

后端主配置文件：

- `Spring-boot-test/src/main/resources/application.properties`

### 2. 启动后端

在项目根目录执行：

```bash
mvn -pl Spring-boot-test -am spring-boot:run
```

默认端口：

- 后端：`8080`

### 3. 启动前端

进入前端目录后执行：

```bash
cd web
npm install
npm run serve
```

默认端口：

- 前端开发服务：`8081`

前端开发代理：

- `/api` -> `http://localhost:8080`

前端构建产物会输出到：

- `Spring-boot-test/src/main/resources/static`

## Docker 运行

项目已提供 `Dockerfile` 和 `docker-compose.yml`。

在项目根目录执行：

```bash
docker compose up --build
```

启动后默认服务：

- MySQL：`3306`
- 应用：`8080`

Docker 环境下：

- 应用容器会连接 `mysql:3306/testy_auth`
- 项目根目录 `logs/` 会挂载到容器内 `/app/logs`

## 常用接口

### 认证接口

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me`
- `POST /api/auth/reset-password`
- `POST /api/auth/logout`

### 项目概览

- `GET /api/overview`

### 文档接口

- `GET /api/docs`
- `GET /api/docs/{id}`
- `POST /api/docs`
- `PUT /api/docs/{id}`
- `DELETE /api/docs/{id}`
- `GET /api/docs/{id}/versions`
- `POST /api/docs/{id}/versions/{versionId}/restore`

### 后台管理接口

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

## 已实现的前端页面

- 登录 / 注册 / 重置密码
- 项目概览
- 文档中心
- 后台管理
  - 安全态势
  - 用户管理
  - 角色权限
  - 登录审计
  - 操作日志

## 开发说明

- 后台入口只对拥有后台权限的账号显示。
- 文档页搜索使用 hash 保存状态，例如：`#docs?q=keyword`
- 登录页会记住上次输入的用户名；勾选“记住密码”后会将密码保存到浏览器本地存储。
- 当前前端主界面集中在 `web/src/App.vue`。

## 后续可扩展方向

- 后台管理拆分为独立页面和组件
- 操作日志增加时间范围与更多筛选条件
- 文档中心增加标签、归档和全文检索
- 登录审计增加告警策略和通知能力
- 增加更细粒度的测试覆盖
