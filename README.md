# TestY 项目说明

TestY 是一个基于 Spring Boot 多模块后端和 Vue 3 前端的示例系统。项目当前已经实现了多表注册登录、基于角色和权限的访问控制、登录审计、root 管理员、项目概览页，以及登录后的 Markdown 文档编写与保存功能。

## 项目目标

这个项目的重点不是单纯展示页面，而是提供一套相对完整的用户认证与内容编写样例，便于继续扩展成后台管理系统、知识库系统或内部协作工具。

## 功能概览

- 用户注册会同时写入账户表、资料表和用户角色关系表。
- 用户登录支持用户名或邮箱，并记录登录 IP、时间、User-Agent、角色快照和权限快照。
- 系统内置 `USER` 和 `ROOT` 两个角色。
- root 管理员拥有所有权限，可以查看用户列表和最近登录审计。
- 普通登录用户可以查看项目概览，并编写、保存、修改自己的 Markdown 文档。
- 前端提供登录、注册、重置密码、工作台、文档列表、文档编辑和实时预览页面。

## 模块结构

- `testy-repository`
  数据实体和 Spring Data JPA 仓储接口。
- `testy-service`
  认证、权限、管理员能力、项目概览等核心业务服务。
- `testy-document`
  Markdown 文档相关服务和只读视图对象。
- `Spring-boot-test`
  启动模块，包含控制器、配置、异常处理和测试。
- `web`
  Vue 3 前端项目。

## 认证与权限设计

系统使用基于 Session 的登录态，不使用 JWT。登录成功后，后端会把当前用户快照写入 HttpSession，并由拦截器统一保护 `/api/**` 路径下的接口。

当前核心表如下：

- `user_account`
  账户主表，保存用户名、邮箱、密码摘要、状态和最近登录信息。
- `user_profile`
  用户资料表，保存展示名和手机号。
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
- `markdown_document`
  Markdown 文档表。

## 默认管理员

系统启动时会自动初始化 root 管理员账号，默认配置如下：

- 用户名：`rootadmin`
- 邮箱：`root@testy.local`
- 密码：`Root@123456`

你可以通过环境变量覆盖这些值：

- `TESTY_ROOT_USERNAME`
- `TESTY_ROOT_EMAIL`
- `TESTY_ROOT_PASSWORD`

## 本地运行要求

- JDK 8
- Maven 3.9 或兼容版本
- Node.js 18 左右版本
- MySQL 8.0

## 后端运行方式

先准备一个可用的 MySQL 数据库。默认配置会连接本机的 `testy_auth` 库，如果数据库不存在会自动创建。

后端配置文件在：

- [application.properties](D:/Y/Desktop/TestY/Spring-boot-test/src/main/resources/application.properties)

在项目根目录执行：

```bash
mvn -pl Spring-boot-test -am spring-boot:run
```

默认后端端口为 `8080`。

## 前端运行方式

前端目录在：

- [web](D:/Y/Desktop/TestY/web)

进入前端目录后执行：

```bash
npm install
npm run serve
```

默认前端开发端口为 `8081`，并会将 `/api` 请求代理到 `http://localhost:8080`。

## 常用接口

认证相关接口：

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me`
- `POST /api/auth/reset-password`
- `POST /api/auth/logout`

项目概览接口：

- `GET /api/overview`

管理员接口：

- `GET /api/admin/users`
- `GET /api/admin/login-audits`

Markdown 文档接口：

- `GET /api/docs`
- `GET /api/docs/{id}`
- `POST /api/docs`
- `PUT /api/docs/{id}`

## 前端页面说明

登录成功后，用户先进入工作台。工作台负责展示账户信息、项目概览和文档入口。点击“编写文档”后进入独立的文档页，在该页面中可以查看自己的文档列表、创建新文档、编辑标题和正文，并实时预览 Markdown 内容。

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

项目已经提供 Dockerfile 和 `docker-compose.yml`，可以直接进行一体化构建和启动：

```bash
docker compose up --build
```

启动后：

- MySQL 暴露在 `3306`
- 应用暴露在 `8080`

## 后续扩展建议

- 增加管理员页面，而不仅是管理员接口。
- 为 Markdown 编辑器增加自动保存、删除文档和版本历史。
- 为角色和权限增加后台维护能力。
- 引入更完整的 Markdown 解析库和样式主题。
- 为登录审计增加分页、筛选和失败告警能力。
