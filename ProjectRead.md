# TestY 项目文档

> 本文档面向后续维护者，涵盖项目架构、技术栈、模块组织、数据库设计、API 接口、权限体系、前端结构、日志系统及部署方式。

---

## 目录

1. [项目概览](#1-项目概览)
2. [技术栈](#2-技术栈)
3. [目录结构](#3-目录结构)
4. [后端模块组织](#4-后端模块组织)
5. [数据库设计](#5-数据库设计)
6. [API 接口文档](#6-api-接口文档)
7. [权限系统](#7-权限系统)
8. [前端架构](#8-前端架构)
9. [日志系统](#9-日志系统)
10. [配置文件说明](#10-配置文件说明)
11. [本地开发环境搭建](#11-本地开发环境搭建)
12. [Docker 部署](#12-docker-部署)
13. [安全注意事项](#13-安全注意事项)

---

## 1. 项目概览

TestY 是一个基于 Spring Boot + Vue 3 的 Web 应用，主要功能包括：

- **用户认证**：注册、登录、密码重置、会话管理
- **后台管理**：用户管理、角色权限管理、登录审计、操作日志
- **Markdown 文档**：多人协作的 Markdown 编辑器，支持版本历史与自动保存
- **社区讨论**：帖子发布、回复讨论、标签分类、搜索筛选、点赞、收藏、分享
- **个人中心**：查看个人信息、我的帖子、点赞列表、收藏列表
- **项目概览**：展示系统模块信息
- **动画引擎**：基于 GSAP 的页面过渡、列表入场、计数动画

项目采用**前后端分离**架构，开发时前端通过代理访问后端 API，生产部署时前端静态资源打包进后端 JAR。

---

## 2. 技术栈

### 后端

| 组件 | 技术 | 版本 |
|---|---|---|
| 语言 | Java | 1.8 (JDK 8) |
| 框架 | Spring Boot | 2.7.0 |
| Web | Spring MVC | via starter-web |
| ORM | Spring Data JPA / Hibernate | via starter-data-jpa |
| 校验 | Bean Validation | via starter-validation |
| 密码加密 | Spring Security Crypto (BCrypt) | via spring-security-crypto |
| 数据库 | MySQL 8 | mysql-connector-java |
| 测试数据库 | H2 (内存模式，MySQL 兼容) | com.h2database |
| 日志 | SLF4J + Logback | logback-spring.xml |
| 构建工具 | Maven | 3.9+ |
| 测试 | JUnit 5, Spring Boot Test | via starter-test |
| 容器化 | Docker, Docker Compose | 多阶段构建 |

### 前端

| 组件 | 技术 | 版本 |
|---|---|---|
| 框架 | Vue.js | 3.2.x |
| 构建工具 | Vue CLI | 5.0.x |
| 打包 | Webpack | via @vue/cli-service |
| Markdown 渲染 | markdown-it | 14.1.1 |
| 任务列表扩展 | markdown-it-task-lists | 2.1.1 |
| 代码高亮 | highlight.js | 11.11.1 |
| 动画引擎 | GSAP | 3.12.x |
| 转译 | Babel 7 | @babel/core 7.12.x |
| 代码检查 | ESLint + eslint-plugin-vue | 7.32.x / 8.0.x |
| Node.js | 18.x | — |

---

## 3. 目录结构

```
TestY/
├── pom.xml                    # Maven 根聚合 POM
├── Dockerfile                 # 多阶段 Docker 构建
├── docker-compose.yml         # MySQL + App 编排
├── ProjectRead.md             # 详细项目文档
├── README.md                  # 快速入门指南
├── docs/
│   └── 人机验证集成指南.md
├── backend/
│   ├── app/                   # Spring Boot 启动模块（Web 层 + 配置）
│   │   ├── pom.xml
│   │   └── src/main/java/com/ysalu/
│   │       ├── TestYApplication.java    # 启动类
│   │       ├── config/                  # 安全、CORS、拦截器配置
│   │       └── web/                     # Controller + DTO
│   │           ├── auth/                # 认证接口
│   │           ├── admin/               # 管理接口
│   │           ├── discussion/          # 社区讨论接口
│   │           ├── document/            # 文档接口
│   │           ├── overview/            # 概览接口
│   │           └── common/              # 异常处理、会话授权
│   └── modules/
│       ├── repository/        # 实体类 + 仓库接口
│       │   └── src/main/java/com/ysalu/
│       │       ├── domain/              # JPA 实体（auth/audit/document/discussion）
│       │       └── repository/          # Spring Data 接口
│       ├── service/           # 业务逻辑层
│       │   └── src/main/java/com/ysalu/service/
│       │       ├── auth/                # AuthService（注册/登录/重置密码）
│       │       ├── admin/               # AdminService（用户/角色/审计管理）
│       │       ├── discussion/          # DiscussionService（帖子/回复 CRUD）
│       │       ├── log/                 # OperationLogService
│       │       ├── overview/            # ProjectOverviewService
│       │       ├── security/            # 权限码/角色码常量
│       │       └── common/              # AuthException
│       ├── document/          # Markdown 文档服务
│       │   └── src/main/java/com/ysalu/document/
│       └── logging/           # 日志输出与清理
│           └── src/main/java/com/ysalu/logging/
│               ├── ApplicationLogService.java
│               ├── LogCleanupService.java
│               └── logback-spring.xml
└── frontend/
    └── web/                   # Vue 3 SPA
        ├── package.json
        ├── vue.config.js      # 开发服务器 + API 代理
        ├── public/index.html
        └── src/
            ├── main.js
            ├── App.vue        # 根组件（状态管理 + 路由 + API 调用）
            ├── assets/        # 静态资源（logo 等）
            └── components/
                ├── AuthPage.vue         # 登录/注册/重置密码
                ├── WorkspaceShell.vue   # 工作区布局（侧边栏 + 顶栏）
                ├── DashboardPage.vue    # 仪表盘
                ├── DocsPage.vue         # Markdown 文档编辑器
                ├── CommunityPage.vue    # 社区讨论（帖子列表/详情/回复/点赞/收藏/分享）
                ├── MyProfilePage.vue    # 个人中心（个人信息/我的帖子/点赞/收藏）
                └── AdminPage.vue        # 后台管理（多 Tab）
```

---

## 4. 后端模块组织

项目采用 Maven 多模块结构，模块依赖关系如下：

```
testy-parent (聚合 POM)
├── testy-logging         ← 独立模块：日志服务
├── testy-repository      ← 独立模块：实体 + 仓库
├── testy-service         ← 依赖 logging, repository
├── testy-document        ← 依赖 service, repository
└── testy-app             ← 依赖所有模块 + Spring Boot Web/Data JPA/Validation
```

### 包职责说明

| 包路径 | 职责 |
|---|---|
| `com.ysalu` | 应用入口 `TestYApplication` |
| `com.ysalu.config` | Spring 配置 Bean（安全初始化、CORS、认证拦截器、密码编码器） |
| `com.ysalu.web.auth` | 认证 Controller、请求/响应 DTO、SessionUser 模型 |
| `com.ysalu.web.admin` | 管理 Controller 及管理专用 DTO |
| `com.ysalu.web.discussion` | 社区讨论 Controller 及请求 DTO |
| `com.ysalu.web.document` | 文档 Controller 及请求 DTO |
| `com.ysalu.web.overview` | 项目概览 Controller |
| `com.ysalu.web.common` | 统一异常处理、会话授权辅助、自定义异常 |
| `com.ysalu.domain.auth` | 认证相关 JPA 实体 |
| `com.ysalu.domain.audit` | 操作日志实体 |
| `com.ysalu.domain.document` | 文档及版本实体 |
| `com.ysalu.domain.discussion` | 讨论帖子、回复、点赞、收藏实体 |
| `com.ysalu.repository.*` | Spring Data JPA 仓库接口 |
| `com.ysalu.service.auth` | 认证核心服务 |
| `com.ysalu.service.admin` | 管理服务：用户 CRUD、角色管理、密码重置、视图 DTO |
| `com.ysalu.service.discussion` | 讨论服务：帖子/回复 CRUD 及视图 DTO |
| `com.ysalu.service.log` | 操作日志记录服务 |
| `com.ysalu.service.security` | 权限码/角色码常量定义 |
| `com.ysalu.document` | Markdown 文档业务服务 |
| `com.ysalu.logging` | 日志输出服务、日志清理服务、Logback 配置 |

---

## 5. 数据库设计

项目使用 JPA + Hibernate，DDL 模式为 `update`（自动建表/更新字段）。

数据库名：`TestY`

### 实体关系总览

```
UserAccount ──1:1── UserProfile
UserAccount ──M:N── SystemRole      (通过 UserRole)
SystemRole  ──M:N── SystemPermission (通过 RolePermission)
UserAccount ──1:N── LoginAudit
UserAccount ──1:N── OperationLog
UserAccount ──1:N── MarkdownDocument (作为 owner)
MarkdownDocument ──1:N── MarkdownDocumentVersion
UserAccount ──1:N── DiscussionPost   (作为 author)
UserAccount ──1:N── DiscussionReply  (作为 author)
UserAccount ──1:N── PostLike         (作为 user)
UserAccount ──1:N── PostBookmark     (作为 user)
DiscussionPost ──1:N── DiscussionReply
DiscussionPost ──1:N── PostLike
DiscussionPost ──1:N── PostBookmark
```

### 表结构详情

#### user_account（用户账户）

| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK, 自增 | 主键 |
| username | VARCHAR(64) | NOT NULL, UNIQUE | 用户名 |
| email | VARCHAR(128) | NOT NULL, UNIQUE | 邮箱 |
| password_hash | VARCHAR(100) | NOT NULL | BCrypt 密码哈希 |
| account_status | VARCHAR(24) | NOT NULL | 枚举：ACTIVE / LOCKED / DISABLED / UNKNOWN |
| failed_login_attempts | INT | NOT NULL, 默认 0 | 连续登录失败次数 |
| locked_until | DATETIME | 可空 | 账户锁定截止时间 |
| last_login_at | DATETIME | — | 最后登录时间 |
| last_login_ip | VARCHAR(64) | — | 最后登录 IP |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |

#### user_profile（用户资料）

| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK, 自增 | 主键 |
| user_account_id | BIGINT | FK → user_account, UNIQUE | 关联账户 |
| display_name | VARCHAR(100) | NOT NULL | 显示名称 |
| phone_number | VARCHAR(32) | — | 手机号 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |

#### system_role（系统角色）

| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK, 自增 | 主键 |
| code | VARCHAR(48) | NOT NULL, UNIQUE | 角色编码 |
| name | VARCHAR(100) | NOT NULL | 角色名称 |
| description | VARCHAR(255) | — | 角色描述 |
| built_in | BOOLEAN | NOT NULL | 是否内置角色 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |

#### system_permission（系统权限）

| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK, 自增 | 主键 |
| code | VARCHAR(64) | NOT NULL, UNIQUE | 权限编码 |
| name | VARCHAR(100) | NOT NULL | 权限名称 |
| description | VARCHAR(255) | — | 权限描述 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |

#### user_role（用户-角色关联）

| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK, 自增 | 主键 |
| user_account_id | BIGINT | FK → user_account, NOT NULL | 用户 |
| role_id | BIGINT | FK → system_role, NOT NULL | 角色 |
| assigned_at | DATETIME | NOT NULL | 分配时间 |

#### role_permission（角色-权限关联）

| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK, 自增 | 主键 |
| role_id | BIGINT | FK → system_role, NOT NULL | 角色 |
| permission_id | BIGINT | FK → system_permission, NOT NULL | 权限 |
| assigned_at | DATETIME | NOT NULL | 分配时间 |

#### login_audit（登录审计）

| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK, 自增 | 主键 |
| user_account_id | BIGINT | FK → user_account, 可空 | 登录用户（失败时可为空） |
| principal | VARCHAR(128) | NOT NULL | 登录主体（用户名或邮箱） |
| remote_ip | VARCHAR(64) | NOT NULL | 客户端 IP |
| user_agent | VARCHAR(255) | — | 浏览器 UA |
| success | BOOLEAN | NOT NULL | 是否成功 |
| message | VARCHAR(255) | NOT NULL | 结果消息 |
| roles_snapshot | VARCHAR(500) | — | 登录时角色快照 |
| permissions_snapshot | VARCHAR(2000) | — | 登录时权限快照 |
| logged_in_at | DATETIME | NOT NULL | 登录时间 |

#### operation_log（操作日志）

| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK, 自增 | 主键 |
| operator_user_id | BIGINT | FK → user_account, 可空 | 操作者 |
| operator_username | VARCHAR(128) | NOT NULL | 操作者用户名 |
| module_code | VARCHAR(64) | NOT NULL | 模块编码 |
| action_code | VARCHAR(64) | NOT NULL | 动作编码 |
| target_type | VARCHAR(64) | — | 目标类型 |
| target_id | VARCHAR(128) | — | 目标 ID |
| success | BOOLEAN | NOT NULL | 是否成功 |
| message | VARCHAR(255) | NOT NULL | 描述 |
| detail | VARCHAR(1000) | — | 详细信息 |
| remote_ip | VARCHAR(64) | — | 客户端 IP |
| occurred_at | DATETIME | NOT NULL | 发生时间 |

#### markdown_document（Markdown 文档）

| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK, 自增 | 主键 |
| owner_id | BIGINT | FK → user_account, NOT NULL | 文档所有者 |
| title | VARCHAR(120) | NOT NULL | 文档标题 |
| content | TEXT (Lob) | NOT NULL | 文档内容 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |

#### markdown_document_version（文档版本）

| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK, 自增 | 主键 |
| document_id | BIGINT | FK → markdown_document, NOT NULL | 所属文档 |
| version_number | INT | NOT NULL | 版本号 |
| title | VARCHAR(120) | NOT NULL | 版本标题 |
| content | TEXT (Lob) | NOT NULL | 版本内容 |
| source_type | VARCHAR(24) | NOT NULL | 枚举：MANUAL / AUTOSAVE / IMPORT / RESTORE |
| created_at | DATETIME | NOT NULL | 创建时间 |

#### discussion_post（讨论帖子）

| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK, 自增 | 主键 |
| author_id | BIGINT | FK → user_account, NOT NULL | 作者 |
| title | VARCHAR(120) | NOT NULL | 帖子标题 |
| content | TEXT (Lob) | NOT NULL | 帖子内容 |
| tags | VARCHAR(255) | — | 标签（逗号分隔） |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |

#### discussion_reply（讨论回复）

| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK, 自增 | 主键 |
| post_id | BIGINT | FK → discussion_post, NOT NULL | 所属帖子 |
| author_id | BIGINT | FK → user_account, NOT NULL | 作者 |
| content | TEXT (Lob) | NOT NULL | 回复内容 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |

#### post_like（帖子点赞）

| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK, 自增 | 主键 |
| post_id | BIGINT | FK → discussion_post, NOT NULL | 帖子 |
| user_id | BIGINT | FK → user_account, NOT NULL | 用户 |
| created_at | DATETIME | NOT NULL | 点赞时间 |
| | | UNIQUE(post_id, user_id) | 每人每帖只能点赞一次 |

#### post_bookmark（帖子收藏）

| 字段 | 类型 | 约束 | 说明 |
|---|---|---|---|
| id | BIGINT | PK, 自增 | 主键 |
| post_id | BIGINT | FK → discussion_post, NOT NULL | 帖子 |
| user_id | BIGINT | FK → user_account, NOT NULL | 用户 |
| created_at | DATETIME | NOT NULL | 收藏时间 |
| | | UNIQUE(post_id, user_id) | 每人每帖只能收藏一次 |

---

## 6. API 接口文档

### 认证接口 `/api/auth`

| 方法 | 路径 | 需要登录 | 说明 |
|---|---|---|---|
| POST | `/api/auth/register` | 否 | 注册新用户（自动分配 USER 角色） |
| POST | `/api/auth/login` | 否 | 登录（用户名/邮箱 + 密码），创建 HTTP Session |
| GET | `/api/auth/me` | 是 | 获取当前登录用户信息 |
| POST | `/api/auth/reset-password` | 否 | 通过用户名 + 邮箱重置密码 |
| POST | `/api/auth/logout` | 是 | 注销 Session |

### 项目概览 `/api`

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| GET | `/api/overview` | `overview:read` | 获取应用名称、Java 版本、模块列表 |

### 后台管理 `/api/admin`

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| GET | `/api/admin/users` | `admin:users:read` | 获取所有用户（含角色权限） |
| PUT | `/api/admin/users/{userId}/roles` | `admin:users:write` | 替换用户角色 |
| PUT | `/api/admin/users/{userId}/status` | `admin:users:write` | 修改账户状态 |
| GET | `/api/admin/roles` | `admin:roles:read` | 获取所有角色（含权限） |
| POST | `/api/admin/roles` | `admin:roles:write` | 创建角色 |
| PUT | `/api/admin/roles/{roleId}` | `admin:roles:write` | 更新角色 |
| GET | `/api/admin/permissions` | `admin:permissions:read` | 获取所有权限 |
| GET | `/api/admin/login-audits` | `admin:logins:read` | 分页查询登录审计（支持筛选） |
| GET | `/api/admin/login-audits/alerts` | `admin:logins:read` | 24h 失败登录摘要 + 异常主体 |
| POST | `/api/admin/users` | `admin:users:create` | 管理员创建用户 |
| POST | `/api/admin/users/{id}/reset-password` | `admin:users:reset-password` | 管理员重置用户密码 |
| DELETE | `/api/admin/users/{id}` | `admin:users:delete` | 删除用户（物理删除） |
| DELETE | `/api/admin/roles/{id}` | `admin:roles:delete` | 删除非内置角色 |
| GET | `/api/admin/operation-logs` | `admin:operation-logs:read` | 分页查询操作日志（支持筛选） |

### 文档管理 `/api/docs`

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| GET | `/api/docs` | `docs:read` | 获取文档列表 |
| GET | `/api/docs/{documentId}` | `docs:read` | 获取文档详情 |
| POST | `/api/docs` | `docs:write` | 创建文档 |
| PUT | `/api/docs/{documentId}` | `docs:write` | 更新文档（内容变更时自动创建版本） |
| DELETE | `/api/docs/{documentId}` | `docs:write` | 删除文档及所有版本 |
| GET | `/api/docs/{documentId}/versions` | `docs:read` | 获取版本历史 |
| POST | `/api/docs/{documentId}/versions/{versionId}/restore` | `docs:write` | 恢复到指定版本 |

### 社区讨论 `/api/discussions`

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| GET | `/api/discussions/posts` | `discussion:read` | 获取帖子列表（按时间倒序） |
| GET | `/api/discussions/posts/{postId}` | `discussion:read` | 获取帖子详情（含回复） |
| POST | `/api/discussions/posts` | `discussion:write` | 创建帖子 |
| PUT | `/api/discussions/posts/{postId}` | `discussion:write` | 更新帖子（仅作者可操作） |
| DELETE | `/api/discussions/posts/{postId}` | `discussion:write` | 删除帖子（仅作者可操作） |
| POST | `/api/discussions/posts/{postId}/replies` | `discussion:write` | 添加回复 |
| PUT | `/api/discussions/replies/{replyId}` | `discussion:write` | 编辑回复（仅作者可操作） |
| DELETE | `/api/discussions/replies/{replyId}` | `discussion:write` | 删除回复（仅作者可操作） |
| POST | `/api/discussions/posts/{postId}/like` | `discussion:read` | 切换点赞状态（返回 `{liked, likeCount}`） |
| POST | `/api/discussions/posts/{postId}/bookmark` | `discussion:read` | 切换收藏状态（返回 `{bookmarked, bookmarkCount}`） |
| GET | `/api/discussions/my/posts` | `discussion:read` | 获取当前用户的帖子 |
| GET | `/api/discussions/my/likes` | `discussion:read` | 获取当前用户点赞的帖子 |
| GET | `/api/discussions/my/bookmarks` | `discussion:read` | 获取当前用户收藏的帖子 |

### 认证机制

- 基于 **HTTP Session**（非 JWT）
- `SessionAuthInterceptor` 拦截所有 `/api/**` 请求（白名单：login/register/reset-password/logout）
- `SessionAuthorization` 组件从 Session 获取 `SessionUser` 并校验权限

### 统一错误响应

`ApiExceptionHandler` 提供统一 JSON 错误格式：

| 异常类型 | HTTP 状态码 |
|---|---|
| AuthException | 400 |
| UnauthorizedException | 401 |
| ForbiddenException | 403 |
| Bean Validation 错误 | 400 |
| DocumentException | 400 |

---

## 7. 权限系统

### 内置角色

| 角色编码 | 名称 | 说明 |
|---|---|---|
| `ROOT` | 超级管理员 | 拥有全部权限 |
| `USER` | 普通用户 | 拥有基础读写权限 |

### 权限编码

| 编码 | 说明 |
|---|---|
| `overview:read` | 查看项目概览 |
| `profile:read` | 查看个人资料 |
| `docs:read` | 读取 Markdown 文档 |
| `docs:write` | 创建和编辑 Markdown 文档 |
| `discussion:read` | 阅读社区讨论 |
| `discussion:write` | 发布帖子与回复 |
| `discussion:delete` | 删除讨论内容 |
| `admin:users:read` | 查看用户列表 |
| `admin:users:write` | 修改用户角色和状态 |
| `admin:roles:read` | 查看角色列表 |
| `admin:roles:write` | 创建和编辑角色 |
| `admin:permissions:read` | 查看权限列表 |
| `admin:logins:read` | 查看登录审计 |
| `admin:operation-logs:read` | 查看操作日志 |
| `admin:users:create` | 管理员创建用户 |
| `admin:users:delete` | 删除用户 |
| `admin:users:reset-password` | 管理员重置用户密码 |
| `admin:roles:delete` | 删除非内置角色 |
| `system:manage` | 系统管理 |

### 默认角色权限分配

| 权限 | ROOT | USER |
|---|---|---|
| `overview:read` | ✓ | ✓ |
| `profile:read` | ✓ | ✓ |
| `docs:read` | ✓ | ✓ |
| `docs:write` | ✓ | ✓ |
| `discussion:read` | ✓ | ✓ |
| `discussion:write` | ✓ | ✓ |
| `discussion:delete` | ✓ | — |
| 所有 admin: 权限 | ✓ | — |
| `system:manage` | ✓ | — |

### 初始化逻辑

`SecurityDataInitializer`（实现 `ApplicationRunner`）在应用启动时确保：

1. 所有权限记录存在
2. `ROOT` 和 `USER` 两个内置角色存在
3. 超级管理员账户存在（用户名/邮箱/密码通过 `application.properties` 配置）

---

## 8. 前端架构

### 路由机制

前端**未使用 Vue Router**，而是在 `App.vue` 中实现了基于 URL hash 的自定义路由：

| Hash | 页面 | 权限要求 |
|---|---|---|
| `#dashboard` | 仪表盘（默认） | 登录即可 |
| `#docs` | Markdown 文档编辑器 | `docs:read` |
| `#docs?q=keyword` | 文档搜索 | `docs:read` |
| `#community` | 社区讨论 | `discussion:read` |
| `#profile` | 个人中心 | 登录即可 |
| `#admin/overview` | 后台 - 安全态势 | 对应 admin 权限 |
| `#admin/users` | 后台 - 用户管理 | `admin:users:read` |
| `#admin/roles` | 后台 - 角色权限 | `admin:roles:read` |
| `#admin/audits` | 后台 - 登录审计 | `admin:logins:read` |
| `#admin/operations` | 后台 - 操作日志 | `admin:operation-logs:read` |

### 组件树

```
App.vue（根组件 — 持有全部状态、API 调用、路由逻辑）
├── AuthPage.vue              ← 未登录时显示
│   ├── 登录表单（用户名/邮箱 + 密码 + 记住密码）
│   ├── 注册表单
│   └── 密码重置表单
│
└── WorkspaceShell.vue        ← 登录后显示（布局外壳）
    ├── 侧边栏（可折叠、导航按钮、用户卡片、注销）
    ├── 顶栏（页面标题、面包屑、最后登录时间）
    ├── Toast 通知（Vue Transition 动画）
    │
    └── 内容区（通过 slot）:
        ├── DashboardPage.vue     ← 欢迎横幅、统计卡片、最近文档、账户信息
        ├── DocsPage.vue          ← 文档列表侧栏 + Markdown 编辑器 + 版本历史
        ├── CommunityPage.vue     ← 帖子列表 + 详情 + 回复（带 GSAP 入场动画）+ 点赞/收藏/分享
        ├── MyProfilePage.vue     ← 个人中心（用户信息 + 我的帖子/点赞/收藏 标签页）
        └── AdminPage.vue         ← 多 Tab 后台管理
```

### 动画系统

项目使用 GSAP（GreenSock Animation Platform）提供页面动画效果：

| 组件 | 动画效果 |
|---|---|
| App.vue | 页面切换时整体淡入上移 |
| WorkspaceShell.vue | 侧边栏导航项从左滑入 |
| DashboardPage.vue | 欢迎横幅缩放渐入 → 统计卡片依次滑入 → 数字递增计数 → 文档列表交错入场 |
| CommunityPage.vue | 帖子卡片列表交错从下滑入 → 回复列表依次滑入 |
| DocsPage.vue | 侧边栏从左滑入 → 文档列表交错入场 → 编辑器内容区淡入 |
| AdminPage.vue | 管理标签栏渐入 → 统计卡片/用户卡片/日志卡片交错从下滑入 |

所有动画统一使用 `power2.out` 缓动函数，通过 `gsap.timeline` 编排时序。

### 核心前端特性

- **Markdown 编辑**：分栏编辑器 + 实时预览（markdown-it + highlight.js + task-lists）
- **自动保存**：内容变更后 1.2 秒防抖自动保存
- **记住密码**：用户名/密码存储在 localStorage
- **权限驱动 UI**：侧边栏导航和管理 Tab 根据用户权限条件渲染
- **API 层**：统一 `apiRequest()` 方法，使用 `fetch()` + `credentials: 'include'`，401 时自动重置客户端会话
- **GSAP 动画**：组件挂载时通过 `gsap.timeline` 编排入场动画，支持交错（stagger）和缓动
- **设计系统**：CSS 变量、DM Sans 字体、暖色调（accent: `#c2410c`）、响应式断点 1024px / 768px / 520px

---

## 9. 日志系统

### 日志文件

| 文件 | Logger 名称 | 内容 |
|---|---|---|
| `application.log` | `TESTY.APPLICATION` | 通用应用事件 |
| `security.log` | `TESTY.SECURITY` | 登录成功/失败事件 |
| `operation.log` | `TESTY.OPERATION` | 用户操作审计 |
| `error.log` | ROOT (ERROR 级别) | 所有错误 |

### 日志轮转策略

- 单文件最大：10MB
- 历史保留：30 天
- 总大小上限：1GB
- 归档格式：gzip

### 日志清理

`LogCleanupService` 每天执行一次（初始延迟 5 分钟），删除超过保留天数（默认 14 天）的日志文件。

### 日志格式

```
时间戳 [线程] 级别 logger - event=... | message=... | key=value ...
```

---

## 10. 配置文件说明

### application.properties（主配置）

所有配置项均支持环境变量覆盖：

| 配置项 | 环境变量 | 默认值 | 说明 |
|---|---|---|---|
| `server.port` | `SERVER_PORT` | `8080` | 后端端口 |
| `server.servlet.session.timeout` | `SERVER_SESSION_TIMEOUT` | `30m` | Session 超时 |
| `spring.datasource.url` | `SPRING_DATASOURCE_URL` | `jdbc:mysql://localhost:3306/TestY...` | 数据库连接 |
| `spring.datasource.username` | `SPRING_DATASOURCE_USERNAME` | `root` | 数据库用户名 |
| `spring.datasource.password` | `SPRING_DATASOURCE_PASSWORD` | — | 数据库密码 |
| `spring.jpa.hibernate.ddl-auto` | `SPRING_JPA_HIBERNATE_DDL_AUTO` | `update` | DDL 策略 |
| `testy.logging.base-path` | `TESTY_LOG_PATH` | `${user.dir}/logs` | 日志目录 |
| `testy.logging.cleanup.retention-days` | `TESTY_LOG_RETENTION_DAYS` | `14` | 日志保留天数 |
| `testy.security.root.username` | `TESTY_ROOT_USERNAME` | `root` | 超管用户名 |
| `testy.security.root.email` | `TESTY_ROOT_EMAIL` | `root@root.local` | 超管邮箱 |
| `testy.security.root.password` | `TESTY_ROOT_PASSWORD` | `rootYsalu` | 超管密码 |
| `testy.security.login.max-attempts` | `TESTY_LOGIN_MAX_ATTEMPTS` | `5` | 登录失败最大尝试次数 |
| `testy.security.login.lock-minutes` | `TESTY_LOGIN_LOCK_MINUTES` | `30` | 账户锁定时长（分钟） |
| `testy.security.password.min-length` | `TESTY_PASSWORD_MIN_LENGTH` | `8` | 密码最小长度 |

### vue.config.js（前端开发配置）

- 开发服务器端口：8081
- API 代理：`/api` → `http://localhost:8080`

---

## 11. 本地开发环境搭建

### 前置条件

- JDK 8
- Maven 3.9+
- Node.js 18.x + npm
- MySQL 8.0

### 启动步骤

**1. 创建数据库**

```sql
CREATE DATABASE TestY CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

**2. 配置数据库连接**

编辑 `backend/app/src/main/resources/application.properties`，修改数据库用户名和密码。

**3. 启动后端**

```bash
cd backend
mvn clean install
cd app
mvn spring-boot:run
```

后端启动在 `http://localhost:8080`。

**4. 启动前端**

```bash
cd frontend/web
npm install
npm run serve
```

前端启动在 `http://localhost:8081`，API 请求自动代理到后端。

**5. 登录**

- 超管账户：`rootadmin` / `Root@123456`

---

## 12. Docker 部署

### docker-compose 一键启动

```bash
docker-compose up -d
```

### 服务说明

| 服务 | 镜像 | 端口 | 说明 |
|---|---|---|---|
| mysql | mysql:8.0 | 3306 | 数据库，数据持久化到 `mysql-data` 卷 |
| app | 构建自 Dockerfile | 8080 | Spring Boot 应用（含前端静态资源） |

### Dockerfile 构建流程（三阶段）

1. **前端构建**：`node:18-alpine` 执行 `npm ci && npm run build` → 生成 `dist/`
2. **后端构建**：`maven:3.9.9-eclipse-temurin-8` 将前端 `dist/` 复制到 `resources/static/`，执行 `mvn package`
3. **运行时**：`eclipse-temurin:8-jre-alpine` 运行 fat jar

### 环境变量配置

通过 `docker-compose.yml` 或 `.env` 文件设置：

```yaml
SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/TestY?...
SPRING_DATASOURCE_USERNAME: root
SPRING_DATASOURCE_PASSWORD: your_password
TESTY_ROOT_PASSWORD: your_admin_password
```

---

## 13. 安全注意事项

- 密码使用 **BCrypt** 哈希存储，不可逆
- 认证基于 **HTTP Session**（非 JWT），Session 超时 30 分钟
- CORS 仅允许 `localhost` 来源
- `root` 用户名在注册时被显式拦截
- **账户锁定**：连续登录失败达到上限（默认 5 次）后，账户自动锁定 30 分钟
- **密码策略**：密码至少 8 位，必须同时包含大写字母、小写字母、数字和特殊字符
- 登录审计记录每次登录尝试（含 IP、UA、角色权限快照）
- 操作日志记录所有关键用户行为
- **部署前必须修改** `application.properties` 和 `docker-compose.yml` 中的默认密码

---

## 附录：常见维护任务

### 新增权限

1. 在 `PermissionCodes.java` 中添加权限常量
2. 在 `SecurityDataInitializer.java` 中添加初始化逻辑
3. 在 `all()` 方法中添加新常量
4. 在 `run()` 方法中添加 `permissions.put(...)` 条目
5. 需要为 USER 角色开启时，在 `ensureRolePermission(userRole, ...)` 中添加调用
6. 在 Controller 方法上添加 `sessionAuthorization.requirePermission(session, PermissionCodes.XXX)`
7. 在前端 `App.vue` 中添加对应的计算属性和条件渲染

### 新增 API 接口

1. 在 `backend/app/src/main/java/com/ysalu/web/` 下创建或编辑 Controller
2. 如需新实体，在 `backend/modules/repository/src/main/java/com/ysalu/domain/` 下创建 JPA 实体
3. 在 `backend/modules/repository/src/main/java/com/ysalu/repository/` 下创建 Repository 接口
4. 如需新业务逻辑，在 `backend/modules/service/` 下创建 Service（如果是一个独立模块，遵循 document 模块的 pom 依赖模式）
5. 前端在 `App.vue` 的 `apiRequest()` 方法调用新接口，在相应组件中处理 UI

### 新增前端页面

1. 在 `frontend/web/src/components/` 下创建新组件
2. 在 `App.vue` 的 `components` 选项中注册
3. 在 template 的 WorkspaceShell slot 中添加 `v-if` / `v-else-if` 分支
4. 在 `syncFromHash()` 和 `updateHash()` 中添加 hash 路由
5. 在 `WorkspaceShell.vue` 中添加侧边栏导航按钮和 `pageTitle` 映射
6. 将 `:can-read-xxx` prop 从 App.vue 传递到 WorkspaceShell

### 修改数据库字段

1. 修改对应的 JPA 实体类（`domain/` 包下）
2. Hibernate 会在启动时自动执行 DDL 更新（`ddl-auto=update`）
3. 注意：删除字段或修改类型需要手动处理数据迁移

### 添加页面动画

项目使用 GSAP 实现动画，遵循以下模式：

1. 在组件 `<script>` 中导入 `import { gsap } from "gsap"`
2. 在 `mounted()` 中调用动画方法
3. 使用 `gsap.timeline({ defaults: { ease: "power2.out" } })` 编排动画序列
4. 使用 `$refs` 获取 DOM 元素引用
5. 配合 `stagger` 参数实现列表交错动画
6. 使用 `clearProps: "transform"` 在动画结束后清除内联样式（避免影响后续布局）
