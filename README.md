# TestY

TestY 是一个前后端分离的示例项目，后端使用 Spring Boot 多模块结构，前端使用 Vue 3。当前项目包含用户认证、管理员能力、Markdown 文档管理、登录审计和操作日志等基础功能。

## 项目结构

```text
.
├─ backend
│  ├─ app                      # Spring Boot 启动模块、Web 接口、配置
│  └─ modules
│     ├─ logging               # 日志配置与日志清理
│     ├─ repository            # 实体、仓储、枚举
│     ├─ service               # 认证、管理、日志等业务服务
│     └─ document              # Markdown 文档服务
├─ frontend
│  └─ web                      # Vue 3 前端工程
├─ logs                        # 默认运行日志目录
├─ Dockerfile
├─ docker-compose.yml
└─ pom.xml                     # Maven 聚合工程
```

## 技术栈

- 后端：Spring Boot 2.7、Spring MVC、Spring Data JPA
- 前端：Vue 3、Vue CLI
- 数据库：MySQL 8
- 测试：JUnit 5、Spring Boot Test、H2

## 环境要求

- JDK 8
- Maven 3.9 或兼容版本
- Node.js 18 左右版本
- MySQL 8

## 快速启动

### 1. 启动后端

在项目根目录执行：

```bash
mvn -pl backend/app -am spring-boot:run
```

默认监听端口：

- `8080`

后端主配置文件：

- `backend/app/src/main/resources/application.properties`

### 2. 启动前端

进入前端目录后执行：

```bash
cd frontend/web
npm install
npm run serve
```

开发服务器默认端口：

- `8081`

前端会将 `/api` 请求代理到：

- `http://localhost:8080`

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

容器日志挂载目录：

- `./logs:/app/logs`

## 关键配置

`application.properties` 中的核心默认值如下：

- 应用名：`testy-multi-module`
- 服务端口：`8080`
- Session 过期时间：`30m`
- 默认数据库：`jdbc:mysql://localhost:3306/testy_auth`
- 默认日志目录：`${user.dir}/logs`
- 默认超级管理员账号：`rootadmin`
- 默认超级管理员邮箱：`root@testy.local`

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

## 安全说明

仓库里的数据库密码和超级管理员初始密码当前写有默认值，只适合本地开发或演示环境。部署到真实环境前，应至少完成以下处理：

- 替换数据库密码
- 替换管理员初始密码
- 通过环境变量注入敏感配置
- 检查日志目录和数据卷权限
