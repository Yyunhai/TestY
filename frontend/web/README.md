# TestY Web

这是 TestY 的 Vue 3 前端工程，物理路径为 `frontend/web`。

## 安装依赖

```bash
npm install
```

## 本地开发

```bash
npm run serve
```

默认会启动 `8081` 端口，并将 `/api` 代理到本地 `8080` 后端。

## 生产构建

```bash
npm run build
```

构建产物输出到当前目录下的 `dist/`，Docker 构建时会再复制到后端静态资源目录。

## 代码检查

```bash
npm run lint
```
