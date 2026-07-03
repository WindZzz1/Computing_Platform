# OBE 达成度计算平台前端

本目录是 OBE 达成度计算平台的前端项目，技术栈为 Vue 3、Vite、TypeScript、Element Plus、Pinia、Axios 和 ECharts。前端负责登录、基础数据管理、矩阵配置、课程大纲、成绩导入、达成度计算、报表展示与导出等页面。

## 目录位置

从仓库根目录进入前端项目：

```bash
cd frontend/obe-frontend
```

不要在仓库根目录或 `frontend` 目录直接执行 `npm run dev`，否则会因为找不到 `package.json` 而启动失败。

## 环境要求

- Node.js：建议使用 LTS 版本。
- npm：随 Node.js 一起安装即可。
- 后端服务：本地联调时默认要求后端运行在 `http://localhost:8101`。

## 安装依赖

首次拉取代码后执行：

```bash
npm install
```

如果依赖异常，可以删除本地 `node_modules` 后重新安装。

## 本地启动

```bash
npm run dev
```

默认访问地址：

```text
http://localhost:5173
```

开发环境接口地址由 `.env.development` 控制：

```text
VITE_API_BASE_URL=/api
```

前端请求 `/api`，由开发服务器或 Nginx 代理到后端服务，避免浏览器跨域问题。

## 生产构建

```bash
npm run build
```

构建产物默认生成在：

```text
dist/
```

`dist` 是部署产物，不需要手动修改其中的文件。每次构建都会重新生成静态资源。

## 本地预览构建结果

```bash
npm run preview
```

该命令用于预览已经构建好的 `dist` 内容，不能代替开发环境的 `npm run dev`。

## 服务器部署流程

1. 在本地执行 `npm run build` 生成 `dist`。
2. 将 `dist` 目录中的文件上传到服务器站点目录，例如 `/www/wwwroot/obe-frontend`。
3. 在 Nginx 中配置前端静态资源目录。
4. 配置 Nginx 将 `/api/` 反向代理到后端服务，例如 `http://127.0.0.1:8101/api/`。
5. 确认服务器安全组和防火墙已放行 `80` 端口。
6. 执行 `nginx -t` 检查配置，再重载 Nginx。

Nginx 配置示例可参考：

```text
DEPLOY.md
nginx-obe-frontend.conf
```

## 常见问题

### 找不到 package.json

通常是因为命令执行目录不对。请先进入：

```bash
cd frontend/obe-frontend
```

再执行 `npm install`、`npm run dev` 或 `npm run build`。

### 登录时报 502

502 通常说明前端已经访问到 Nginx，但 Nginx 没有成功代理到后端。需要检查：

- 后端服务是否已经启动。
- 后端端口是否为 `8101`。
- Nginx 的 `/api/` 代理地址是否正确。
- 服务器防火墙或安全组是否放行相关端口。

### 页面能打开但接口超时

需要检查后端数据库连接、后端服务日志，以及 Nginx 代理是否能访问后端地址。

### 构建后页面路由刷新 404

Vue 前端使用 history 路由时，Nginx 需要配置：

```nginx
try_files $uri $uri/ /index.html;
```

否则直接刷新 `/dashboard`、`/basic-data` 等页面可能会返回 404。

## 主要功能模块

- 登录与用户信息保存
- 基础数据管理：课程、学生、学院、专业、学年学期、用户、毕业要求、指标点
- 矩阵配置：课程与毕业要求指标点的支撑关系
- 课程大纲管理：课程目标、考核点、内部贡献权重
- 成绩管理与计算：成绩导入、课程级达成度计算
- 专业级计算：读取课程级结果并汇总专业指标点达成度
- 报表中心：结果查看与导出
