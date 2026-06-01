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
