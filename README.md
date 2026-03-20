# TestY

TestY 是一个前后端一体化的示例项目，提供基础的用户注册、登录、重置密码能力，并在登录后展示项目运行概览。后端采用 Spring Boot 多模块结构，前端采用 Vue 3，支持本地开发和 Docker 一键部署。

## 功能概览

- 用户注册
- 用户登录
- 重置密码
- 登录后查看项目概览
- 前后端分离开发
- Docker 一键启动应用和 MySQL

## 技术栈

- 后端：Spring Boot 2.7、Spring MVC、Spring Data JPA、Bean Validation
- 服务安全：BCrypt 密码加密
- 数据库：MySQL 8
- 测试：Spring Boot Test、MockMvc、H2
- 前端：Vue 3、Vue CLI 5
- 构建：Maven 多模块、npm
- 部署：Docker、Docker Compose

## 项目结构

```text
TestY
├─ Spring-boot-test     # 启动模块，提供 Web 接口、配置和测试
├─ testy-service        # 业务服务层
├─ testy-repository     # 实体与仓储层
├─ web                  # Vue 前端
├─ Dockerfile           # 前后端一体化镜像构建
├─ docker-compose.yml   # 应用 + MySQL 编排
└─ pom.xml              # Maven 聚合父工程
```

## 核心模块说明

### 后端模块

- `testy-repository`
  负责 `UserAccount` 实体定义、JPA 仓储接口，以及项目模块信息读取。
- `testy-service`
  负责认证业务逻辑，包括用户名和邮箱校验、密码规则校验、BCrypt 加密、登录和密码重置。
- `Spring-boot-test`
  作为启动模块，暴露 REST API、统一异常处理、CORS 配置和集成测试。

### 前端模块

- `web`
  提供登录、注册、找回密码和登录后概览页。
- 开发模式下默认运行在 `8081` 端口，并通过代理把 `/api` 请求转发到 `http://localhost:8080`。
- 生产构建会输出到 `Spring-boot-test/src/main/resources/static`，由 Spring Boot 统一托管。

## 运行要求

### 本地开发

- JDK 8
- Maven 3.9+
- Node.js 18+
- npm
- MySQL 8

### Docker 部署

- Docker Desktop
- `docker compose`

## 快速开始

### 方式一：使用 Docker

项目已经提供完整的前后端一体化容器配置。执行下面命令即可同时启动应用和数据库：

```bash
docker compose up --build -d
```

启动完成后访问：

- 应用首页：`http://localhost:8080`
- MySQL：`localhost:3306`

查看日志：

```bash
docker compose logs -f app
```

停止服务：

```bash
docker compose down
```

同时删除数据库卷：

```bash
docker compose down -v
```

### Docker 默认数据库配置

- 数据库名：`testy_auth`
- 用户名：`root`
- 密码：`200612yhx`

## 本地开发启动

### 1. 启动 MySQL

先准备一个本地 MySQL 8 数据库，并确保存在以下连接信息：

- 数据库名：`testy_auth`
- 用户名：`root`
- 密码：`200612yhx`
- 端口：`3306`

如果你希望使用其他配置，可以通过环境变量覆盖：

```bash
SERVER_PORT=8080
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/testy_auth?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=200612yhx
SPRING_JPA_HIBERNATE_DDL_AUTO=update
```

### 2. 启动后端

在项目根目录执行：

```bash
mvn -pl Spring-boot-test -am spring-boot:run
```

默认启动地址：

- 后端服务：`http://localhost:8080`

### 3. 启动前端

进入前端目录执行：

```bash
cd web
npm install
npm run serve
```

默认启动地址：

- 前端开发服务：`http://localhost:8081`

## 打包构建

### 前端构建

```bash
cd web
npm run build
```

构建产物会写入后端静态资源目录，随后可由 Spring Boot 直接提供页面。

### 后端打包

在项目根目录执行：

```bash
mvn -pl Spring-boot-test -am package
```

生成的可执行 JAR 位于：

```text
Spring-boot-test/target/spring-boot-test-1.0-SNAPSHOT.jar
```

运行方式：

```bash
java -jar Spring-boot-test/target/spring-boot-test-1.0-SNAPSHOT.jar
```

## API 概览

### 1. 获取项目概览

- 方法：`GET`
- 路径：`/api/overview`

示例响应：

```json
{
  "applicationName": "TestY",
  "javaVersion": "1.8.0_xxx",
  "message": "Spring Boot multi-module application is running.",
  "modules": [
    "testy-repository",
    "testy-service",
    "Spring-boot-test"
  ]
}
```

### 2. 用户注册

- 方法：`POST`
- 路径：`/api/auth/register`

请求体：

```json
{
  "username": "tester01",
  "email": "tester01@example.com",
  "password": "secret01"
}
```

### 3. 用户登录

- 方法：`POST`
- 路径：`/api/auth/login`

请求体：

```json
{
  "usernameOrEmail": "tester01",
  "password": "secret01"
}
```

### 4. 重置密码

- 方法：`POST`
- 路径：`/api/auth/reset-password`

请求体：

```json
{
  "username": "tester01",
  "email": "tester01@example.com",
  "newPassword": "secret02"
}
```

### 认证接口统一响应

```json
{
  "success": true,
  "message": "Login successful.",
  "username": "tester01",
  "email": "tester01@example.com"
}
```

## 校验规则

- 用户名至少 6 位
- 密码至少 6 位
- 邮箱必须符合标准格式
- 用户名唯一
- 邮箱唯一
- 登录支持用户名或邮箱
- 重置密码时必须同时匹配用户名和邮箱

## 测试

项目包含基础集成测试，测试时使用 H2 内存数据库，不依赖本地 MySQL。

在项目根目录执行：

```bash
mvn test
```

当前测试覆盖：

- 项目概览接口
- 前端开发环境跨域访问
- 注册、登录、重置密码完整流程

## 配置说明

后端默认配置位于：

- `Spring-boot-test/src/main/resources/application.properties`

关键配置包括：

- `server.port`
- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`
- `spring.jpa.hibernate.ddl-auto`

## 注意事项

- 当前仓库中的数据库默认密码是明文示例配置，仅适合本地开发和演示环境。
- 前端构建会覆盖后端静态资源目录，发布前应确认构建内容符合预期。
- Docker 镜像会先构建前端，再打包后端，最终对外暴露 `8080` 端口。

## 后续可扩展方向

- 增加 JWT 或 Session 鉴权
- 增加接口文档，例如 Swagger / OpenAPI
- 增加用户信息管理页面
- 增加更完整的单元测试和接口测试
- 将敏感配置迁移到环境变量或密钥管理系统
