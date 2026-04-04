# TestY Web

这是 TestY 的前端工程，使用 Vue 3 和 Vue CLI 构建，目录位置为 `frontend/web`。

## 环境要求

- Node.js 18 左右版本
- npm 8 或兼容版本

## 安装依赖

```bash
npm install
```

## 本地开发

```bash
npm run serve
```

默认行为如下：

- 开发服务器端口为 `8081`
- `/api` 请求会代理到 `http://localhost:8080`

代理和开发服务器配置文件：

- `frontend/web/vue.config.js`

## 生产构建

```bash
npm run build
```

构建产物输出到：

- `frontend/web/dist`

如果使用项目根目录下的 `Dockerfile` 构建镜像，`dist` 内容会在构建阶段复制到后端静态资源目录，并由后端服务统一对外提供。

## 代码检查

```bash
npm run lint
```

## 主要脚本

- `npm run serve`：启动本地开发服务器
- `npm run build`：生成生产构建产物
- `npm run lint`：执行 ESLint 检查
