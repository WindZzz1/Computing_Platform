# 阿里云 ECS 部署说明

适用项目：
- 前端：`frontend/obe-frontend`
- 后端：`backend/Computing_Platform_backend/springboot-init-master`
- 部署目标：`120.27.221.219`

## 1. 目录约定

服务器目录：

- 前端静态文件：`/var/www/computing-platform`
- 后端运行目录：`/opt/computing-platform/backend`
- Nginx 配置：`/etc/nginx/conf.d/computing-platform.conf`

## 2. 前端发布

本地打包：

```powershell
cd D:\codex\kczy\Computing_Platform\frontend\obe-frontend
D:\codex\kczy\node-v24.16.0-win-x64\node-v24.16.0-win-x64\npm.cmd run build
```

将 `dist` 目录中的文件上传到：

```bash
/var/www/computing-platform
```

建议上传后确认首页引用的是最新打包文件：

```bash
cat /var/www/computing-platform/index.html
```

## 3. Nginx 配置

推荐配置如下：

```nginx
server {
    listen 80;
    server_name 120.27.221.219;

    root /var/www/computing-platform;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:8101/api/;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

配置完成后执行：

```bash
nginx -t
systemctl reload nginx
```

## 4. 后端部署

当前服务器运行方式不是直接 `java -jar`，而是下面这种目录结构：

- `classes/`
- `lib/`
- `application-prod-ecs.yml`

后端目录：

```bash
cd /opt/computing-platform/backend
```

## 5. 后端生产配置

生产配置文件：

```bash
/opt/computing-platform/backend/application-prod-ecs.yml
```

已验证可用的数据库配置如下：

```yaml
server:
  address: 0.0.0.0
  port: 8101
  servlet:
    context-path: /api

spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/graduation_achievement?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC
    username: yu
    password: 125103Dbqwan

mybatis-plus:
  configuration:
    log-impl: ''

knife4j:
  enable: true
```

注意：

- 数据库地址必须使用 `127.0.0.1`
- 不要写成 `120.27.221.219:3306`
- 否则后端可能出现 MySQL `Connect timed out`

## 6. 启动后端

先停止旧进程：

```bash
pkill -f 'com.yupi.springbootinit.MainApplication'
```

再启动：

```bash
nohup java -cp "classes:lib/*" com.yupi.springbootinit.MainApplication --spring.config.additional-location=application-prod-ecs.yml --spring.profiles.active=prod > app.log 2>&1 &
```

## 7. 启动后检查

查看日志：

```bash
tail -n 80 /opt/computing-platform/backend/app.log
```

启动成功时应看到类似：

```text
Tomcat started on port(s): 8101 (http) with context path '/api'
Started MainApplication
```

查看端口监听：

```bash
ss -tnlp | grep 8101
```

## 8. 登录接口自检

直接测试后端登录接口：

```bash
curl -i -m 20 -H "Content-Type: application/json" -d '{"username":"yuyu","password":"12345678"}' http://127.0.0.1:8101/api/sysuser/login/token
```

如果返回 `200` 和 JSON，说明后端登录链路正常。

## 9. 页面验证

浏览器打开：

- `http://120.27.221.219/login`

首次发布或替换前端文件后，建议执行：

- `Ctrl + F5` 强刷

测试账号：

- 用户名：`yuyu`
- 密码：`12345678`

## 10. 常用排查命令

看后端是否启动：

```bash
ps -ef | grep MainApplication
```

看 8101 是否监听：

```bash
ss -tnlp | grep 8101
```

看数据库是否监听：

```bash
ss -tnlp | grep 3306
```

测试本机数据库：

```bash
mysql -h 127.0.0.1 -P 3306 -u yu -p125103Dbqwan -D graduation_achievement -e "select 1;"
```

抓取关键错误：

```bash
grep -n "ERROR\|Caused by\|Exception" /opt/computing-platform/backend/app.log | tail -n 60
```

## 11. 本次实际踩坑记录

本次线上登录超时，最终定位到两个问题：

1. `application-prod-ecs.yml` 中数据库地址误写为公网地址 `120.27.221.219:3306`
2. 修改配置时一度误写成 `url: url: jdbc:mysql://...`，导致 YAML 配置解析失败

修正后恢复正常。
