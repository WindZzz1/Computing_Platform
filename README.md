# 面向专业认证的毕业要求达成度统一计算平台

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-green.svg)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.4-brightgreen.svg)](https://vuejs.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.3-blue.svg)](https://www.typescriptlang.org/)

> 面向专业认证的毕业要求达成度统一计算平台，帮助高校实现工程教育专业认证的毕业要求达成度自动化计算与管理。

## 📖 项目简介

本项目是为满足工程教育专业认证要求而开发的毕业要求达成度统一计算平台。系统通过课程级和专业级两套计算模型，支持：

- 📊 **毕业要求指标点管理**：灵活配置毕业要求与指标点
- 📚 **课程体系管理**：课程库、教学班、学生信息维护
- 🎯 **宏观支撑矩阵配置**：课程-指标点权重 W 设置与校验
- 📝 **课程大纲配置**：课程目标、内部贡献权重 w、考核点管理
- 📈 **成绩导入与计算**：Excel 批量导入、课程级达成度计算
- 📋 **专业级报表生成**：专业达成度自动计算与报表导出

系统采用 **RBAC 权限模型**，支持管理员、教务管理员、专业负责人、主讲教师四种角色，确保数据安全与权限隔离。

## 📁 项目结构

```
Computing_Platform/
├── backend/                          # 后端服务
│   └── Computing_Platform_backend/
│       └── springboot-init-master/  # Spring Boot 后端项目
├── frontend/                         # 前端页面
│   └── obe-frontend/                 # Vue 3 前端项目
├── ppt/                              # 项目汇报PPT
├── deploy-aliyun.md                 # 阿里云部署文档
└── README.md                        # 项目说明文档
```

## 🛠 技术栈

### 后端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 2.7.18 | 基础框架 |
| JDK | 17 | 开发环境 |
| MyBatis-Plus | 3.5.2 | ORM 框架 |
| MySQL | 8.0+ | 关系型数据库 |
| Redis | - | 缓存与 Session 存储 |
| Knife4j | 4.4.0 | 接口文档 |
| JWT | 0.11.5 | 用户认证 |
| Hutool | 5.8.8 | 工具库 |
| EasyExcel | 3.1.1 | Excel 处理 |
| PDFBox | 2.0.29 | PDF 生成 |

### 前端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.4 | 前端框架 |
| TypeScript | 5.3 | 类型系统 |
| Vite | 5.0 | 构建工具 |
| Element Plus | - | UI 组件库 |
| Axios | - | HTTP 客户端 |
| Vue Router | 4.x | 路由管理 |

## 🔧 环境要求

### 后端环境
- **JDK 17**
- **Maven 3.6+**
- **MySQL 8.0+**
- **Redis** (可选，用于 Session 管理)
- **IntelliJ IDEA** (推荐)

### 前端环境
- **Node.js 18+**
- **npm 9+** 或 **pnpm 8+**

## 🚀 快速开始

### 后端部署

#### 1. 配置 JDK 17

拉取代码后，**必须**将项目语言级别设置为 JDK 17：

**IntelliJ IDEA 设置：**
1. 右键项目 → `Open Module Settings` (或 `Ctrl+Alt+Shift+S`)
2. `Project` 设置：
   - SDK: `JDK 17`
   - Language level: `17 - Sealed types`
3. `Modules` 设置：
   - Language level: `17`
4. 点击 `Apply` 和 `OK`

#### 2. 初始化数据库

执行 SQL 脚本：

```bash
# 在 MySQL 中执行
mysql -u root -p < backend/Computing_Platform_backend/springboot-init-master/sql/create_table.sql
```

该脚本会创建 `graduation_achievement` 数据库并初始化所有数据表。

#### 3. 修改数据库配置

编辑 `backend/Computing_Platform_backend/springboot-init-master/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/graduation_achievement?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC
    username: root        # 改为你的数据库用户名
    password: 125103      # 改为你的数据库密码
```

#### 4. 启动后端服务

运行主启动类：

```
backend/Computing_Platform_backend/springboot-init-master/src/main/java/com/yupi/springbootinit/SpringbootInitApplication.java
```

启动成功后，服务地址：`http://localhost:8101/api`

接口文档地址：`http://localhost:8101/api/doc.html`

### 前端部署

#### 1. 安装依赖

```bash
cd frontend/obe-frontend
npm install
```

#### 2. 启动开发服务器

```bash
npm run dev
```

默认访问地址：`http://localhost:5173`

#### 3. 构建生产版本

```bash
npm run build
```

构建产物在 `dist/` 目录。

## 📝 使用说明

### 登录系统

默认测试账号：

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 管理员 | admin | 123456 | 系统管理员 |
| 教务管理员 | edu_admin | 123456 | 教务管理 |
| 专业负责人 | major_leader | 123456 | 专业管理 |
| 主讲教师 | teacher | 123456 | 课程教学 |

### 接口认证

系统采用 **JWT Token** 认证方式：

**请求头携带 Token：**
```javascript
headers: {
  'Authorization': 'Bearer ' + token
}
```

**统一响应格式：**
```typescript
interface BaseResponse<T> {
  code: number;      // 0 = 成功
  data: T;          // 响应数据
  message: string;  // 提示信息
}
```

**错误码说明：**

| code | 说明 | 处理建议 |
|------|------|----------|
| 0 | 成功 | 正常处理数据 |
| 40000 | 请求参数错误 | 检查请求参数格式 |
| 40100 | 未登录 | 跳转登录页 |
| 40101 | 无权限 | 提示权限不足 |
| 40400 | 数据不存在 | 提示数据不存在 |
| 40300 | 禁止访问 | 账号已被禁用 |
| 50000 | 系统内部异常 | 联系管理员 |
| 50001 | 操作失败 | 提示操作失败 |

详细的前端对接说明请参考：[前端开发指南.md](backend/Computing_Platform_backend/前端开发指南.md)

### 主要功能模块

#### 1. 基础数据管理
- **学院管理**：维护学院信息
- **专业管理**：维护专业信息
- **课程管理**：课程库维护、支持 Excel 导入导出
- **学生管理**：学生信息维护
- **教学班管理**：教学班创建、学生绑定

#### 2. 毕业要求管理
- **毕业要求配置**：配置专业毕业要求
- **指标点管理**：维护指标点信息

#### 3. 权重配置
- **宏观支撑矩阵**：配置课程-指标点权重 W
- **课程大纲配置**：配置课程目标、内部权重 w、考核点

#### 4. 成绩管理
- **成绩导入**：Excel 批量导入学生成绩
- **课程级计算**：计算课程级达成度

#### 5. 报表导出
- **专业级计算**：计算专业达成度
- **报表导出**：导出计算结果报表

## 🌐 生产部署

### 阿里云 ECS 部署

详细的阿里云部署步骤请参考：[deploy-aliyun.md](deploy-aliyun.md)

**部署架构：**
- 前端：Nginx 静态文件服务
- 后端：Java 应用运行在 8101 端口
- 数据库：MySQL 8.0
- 反向代理：Nginx 将 `/api/*` 代理到后端服务

**快速部署命令：**

```bash
# 后端启动
cd /opt/computing-platform/backend
nohup java -cp "classes:lib/*" com.yupi.springbootinit.MainApplication \
  --spring.config.additional-location=application-prod-ecs.yml \
  --spring.profiles.active=prod > app.log 2>&1 &

# 前端部署
cp -r frontend/obe-frontend/dist/* /var/www/computing-platform/

# Nginx 重载
nginx -t && systemctl reload nginx
```

## 📚 文档资料

- [后端接口文档](http://localhost:8101/api/doc.html) (Knife4j)
- [前端开发指南](backend/Computing_Platform_backend/前端开发指南.md)
- [阿里云部署文档](deploy-aliyun.md)
- [模块 C 模板 API](backend/Computing_Platform_backend/springboot-init-master/MODULE_C_TEMPLATE_API.md)
- [模块 D 报表 API](backend/Computing_Platform_backend/springboot-init-master/MODULE_D_1_REPORT_API.md)
- [模块 D 专业报表 API](backend/Computing_Platform_backend/springboot-init-master/MODULE_D_2_MAJOR_REPORT_API.md)

## ❓ 常见问题

### 后端启动失败

**问题：** `Unsupported class file major version`
**解决：** 确认 JDK 版本为 17，项目语言级别设置为 17

**问题：** `Communications link failure`
**解决：** 检查 MySQL 服务是否启动，数据库配置是否正确

### 前端访问失败

**问题：** 接口返回 401
**解决：** 检查 Token 是否正确携带在请求头中

**问题：** 跨域错误
**解决：** 后端已配置 CORS，检查后端是否正常启动

### 部署问题

**问题：** 登录超时
**解决：** 检查数据库地址是否使用 `127.0.0.1` 而非公网 IP

## 🤝 贡献指南

欢迎贡献代码、报告问题或提出改进建议！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交 Pull Request

## 📄 许可证

本项目仅用于内部学习交流。

---

**开发团队：** 计算平台开发组

**最后更新：** 2026-06-07
