# web 前端说明

这是 TestY 项目的 Vue 3 前端工程，负责登录、注册、重置密码、工作台展示以及 Markdown 文档编辑与实时预览。

## 安装依赖

```bash
npm install
```

## 本地开发

```bash
npm run serve
```

默认会启动前端开发服务器，并将 `/api` 请求代理到本地 `8080` 端口的 Spring Boot 后端。

## 生产构建

```bash
npm run build
```

构建产物会输出到后端静态资源目录，便于和 Spring Boot 一起部署。

## 代码检查

```bash
npm run lint
```
