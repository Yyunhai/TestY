# TestY

## Docker 部署

项目已经包含前后端一体化的 Docker 部署配置：

- `Dockerfile`：多阶段构建，自动打包 Vue 前端和 Spring Boot 后端
- `docker-compose.yml`：同时启动应用和 MySQL 8

启动前请确保本机已经安装 Docker Desktop，并启用了 `docker compose`。

### 一键启动

```bash
docker compose up --build -d
```

启动后访问：

- 应用首页：`http://localhost:8080`
- MySQL：`localhost:3306`

### 默认数据库配置

- 数据库名：`testy_auth`
- 用户名：`root`
- 密码：`200612yhx`

### 常用命令

查看日志：

```bash
docker compose logs -f app
```

停止服务：

```bash
docker compose down
```

停止并删除数据库数据卷：

```bash
docker compose down -v
```

## 本地开发

前端：

```bash
cd web
npm install
npm run serve
```

后端：

```bash
mvn -pl Spring-boot-test spring-boot:run
```
