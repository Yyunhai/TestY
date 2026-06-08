# TestY

TestY 是一个前后端分离的示例项目，后端使用 Spring Boot 多模块结构，前端使用 Vue 3。当前项目包含用户认证、管理员能力、Markdown 文档管理、社区讨论（含点赞/收藏/分享）、个人中心、登录审计和操作日志等基础功能。

## 项目结构

```text
.
├─ backend
│  ├─ app                      # Spring Boot 启动模块、Web 接口、配置
│  └─ modules
│     ├─ logging               # 日志配置与日志清理
│     ├─ repository            # 实体、仓储、枚举
│     ├─ service               # 认证、管理、讨论、日志等业务服务
│     └─ document              # Markdown 文档服务
├─ frontend
│  └─ web                      # Vue 3 前端工程
├─ logs                        # 默认运行日志目录
├─ docs                        # 项目文档
├─ Dockerfile
├─ docker-compose.yml
└─ pom.xml                     # Maven 聚合工程
```

## 技术栈

- 后端：Spring Boot 2.7、Spring MVC、Spring Data JPA、Hibernate
- 前端：Vue 3、Vue CLI、GSAP（动画）、markdown-it（Markdown 渲染）
- 数据库：MySQL 8
- 测试：JUnit 5、Spring Boot Test、H2
- 容器化：Docker、Docker Compose

## 功能列表

| 模块 | 说明 |
|------|------|
| 用户认证 | 注册、登录、密码重置、会话管理、记住密码 |
| Markdown 文档 | 分栏编辑器、实时预览、版本历史、自动保存 |
| 社区讨论 | 帖子发布、回复讨论、标签分类、搜索筛选、点赞、收藏、分享 |
| 个人中心 | 查看个人信息、我的帖子、点赞列表、收藏列表 |
| 后台管理 | 用户管理、角色权限管理、登录审计、操作日志 |
| 动画引擎 | 基于 GSAP 的页面切换、列表入场、计数动画 |

## 环境要求

- JDK 8
- Maven 3.9 或兼容版本
- Node.js 18 左右版本
- MySQL 8

## 快速启动

### 1. 创建数据库

```sql
CREATE DATABASE TestY CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

### 2. 启动后端

在项目根目录执行：

```bash
mvn -pl backend/app -am spring-boot:run
```

默认监听端口：`8080`

后端主配置文件：`backend/app/src/main/resources/application.properties`

### 3. 启动前端

进入前端目录后执行：

```bash
cd frontend/web
npm install
npm run serve
```

开发服务器默认端口：`8081`

前端会将 `/api` 请求代理到 `http://localhost:8080`

### 4. 登录

- 超管账户：`rootadmin` / `Root@123456`
- 普通用户：通过注册页面自行创建

## 常用命令

后端测试：

```bash
mvn -pl backend/app -am test
```

后端打包：

```bash
mvn -pl backend/app -am package -DskipTests
```

前端构建：

```bash
cd frontend/web
npm run build
```

前端代码检查：

```bash
cd frontend/web
npm run lint
```

## Docker 运行

项目提供了完整的容器化构建链路，前端构建产物会在镜像构建时复制到后端静态资源目录，再由 Spring Boot 统一提供。

启动命令：

```bash
docker compose up --build
```

默认暴露端口：

- MySQL：`3306`
- 应用：`8080`

容器日志挂载目录：`./logs:/app/logs`

## 关键配置

`application.properties` 中的核心默认值如下：

- 应用名：`testy-multi-module`
- 服务端口：`8080`
- Session 过期时间：`30m`
- 默认数据库：`jdbc:mysql://localhost:3306/TestY`
- 默认日志目录：`${user.dir}/logs`
- 默认超级管理员账号：`rootadmin`
- 默认超级管理员邮箱：`root@testy.local`
- 默认超级管理员密码：`Root@123456`

可通过环境变量覆盖的常用配置包括：

- `SERVER_PORT`
- `SERVER_SESSION_TIMEOUT`
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SPRING_JPA_HIBERNATE_DDL_AUTO`
- `TESTY_LOG_PATH`
- `TESTY_ROOT_USERNAME`
- `TESTY_ROOT_EMAIL`
- `TESTY_ROOT_PASSWORD`
- `TESTY_LOGIN_MAX_ATTEMPTS` — 登录失败最大尝试次数（默认 5）
- `TESTY_LOGIN_LOCK_MINUTES` — 账户锁定时长，分钟（默认 30）
- `TESTY_PASSWORD_MIN_LENGTH` — 密码最小长度（默认 8）



## 详细文档

更多关于架构设计、API 接口、数据库设计、权限系统和前端结构的详细说明，请参见 [ProjectRead.md](ProjectRead.md)。
