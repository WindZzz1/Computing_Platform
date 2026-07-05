# OBE 前端部署说明

## 1. 构建产物

本项目构建命令：

```bash
npm install
npm run build
```

构建输出目录为：

```text
release/
```

### 本地 `dist` 目录权限异常处理

如果本地曾经执行过默认构建，项目中可能残留 `dist` 目录。部分 Windows 环境下，`dist` 可能因为文件被浏览器、编辑器、压缩工具或同步软件占用，导致再次执行构建时出现无法删除、无法写入或权限不足的报错。

遇到这类问题时，优先按下面顺序排查：

1. 关闭正在访问 `dist` 目录的浏览器、预览服务、压缩软件或文件管理器窗口。
2. 手动删除 `frontend/obe-frontend/dist` 后重新执行构建。
3. 如果只是为了验证构建或临时部署，可以指定新的输出目录，避开受权限影响的 `dist`：

```bash
npm exec -- vite build --outDir verify-dist --emptyOutDir
```

用于部署时也可以生成临时目录：

```bash
npm exec -- vite build --outDir deploy-dist --emptyOutDir
```

`verify-dist` 和 `deploy-dist` 只属于本地验证/部署临时产物，不需要提交到 Git。项目 `.gitignore` 已忽略这些目录。

## 2. 前后端接口约定

前端默认请求：

```text
/api
```

开发环境通过 Vite 代理到：

```text
http://localhost:8101
```

生产环境建议通过 Nginx 同域反向代理：

```text
/api -> http://127.0.0.1:8101/api
```

这样浏览器访问前端域名时，请求 `/api/sysuser/login/token` 会被 Nginx 转发给后端，避免跨域问题。

## 3. Nginx 配置示例

假设前端文件放在：

```text
/www/wwwroot/obe-frontend
```

Nginx server 可配置为：

```nginx
server {
    listen 80;
    server_name 47.97.194.150;

    root /www/wwwroot/obe-frontend;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:8101/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

## 4. 阿里云部署步骤

1. 在服务器安装 Nginx。
2. 将 `release` 目录内的文件上传到 `/www/wwwroot/obe-frontend`。
3. 将上面的 Nginx 配置写入站点配置。
4. 确认后端服务运行在 `8101` 端口，且接口前缀为 `/api`。
5. 在阿里云控制台安全组/防火墙放行 `80` 端口。
6. 重载 Nginx：

```bash
nginx -t
systemctl reload nginx
```

## 5. 当前联调注意点

后端本地启动需要满足：

- JDK 17：当前后端 `pom.xml` 使用 Java 17 编译配置。
- MySQL：后端 `application.yml` 指向 `graduation_achievement` 数据库。
- Swagger 地址：`http://localhost:8101/api/doc.html`。
- 登录接口：`POST /api/sysuser/login/token`。
- JWT 请求头：`Authorization: Bearer <token>`。
