# Computing Platform Backend

毕业成果管理系统后端服务

> 本项目基于 [SpringBoot 项目初始模板](https://github.com/liyupi) 开发

## 环境要求

- **JDK 17** - 本系统采用 Java 17 开发
- Maven 3.6+
- MySQL 8.0+
- IDE（推荐 IntelliJ IDEA）

## 快速开始

### 第一步：配置 JDK 17

拉取代码后，**必须**将项目语言级别设置为 JDK 17，否则项目无法正常运行！

**IntelliJ IDEA 设置步骤：**
1. 右键项目 -> `Open Module Settings`（或按 `Ctrl+Alt+Shift+S`）
2. 在左侧选择 `Project`：
   - `SDK` 选择 `JDK 17`（如果没有，点击 `Add SDK` -> `Download JDK` 下载）
   - `Language level` 选择 `17 - Sealed types, always-strict floating-point semantics`
3. 在左侧选择 `Modules` -> 选择项目模块：
   - 将 `Language level` 也设置为 `17`
4. 点击 `Apply` 和 `OK` 保存

**验证配置是否成功：**
- 打开 `pom.xml`，确认 `<java.version>17</java.version>`
- 项目编译器显示为 `17`

### 第二步：初始化数据库

找到项目根目录下的 SQL 文件并执行：

```
springboot-init-master/sql/create_table.sql
```

该脚本会自动：
- 创建名为 `graduation_achievement` 的数据库
- 初始化系统所需的所有数据表（用户表、学院字典表、专业字典表、学年学期表等）

**执行方式：**
- 使用 Navicat / MySQL Workbench / DBeaver 等数据库工具直接运行该 SQL 文件

### 第三步：修改数据库配置

打开配置文件修改为你的本地数据库连接信息：

**文件路径：** `src/main/resources/application.yml`

**找到以下内容并修改：**
```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/graduation_achievement?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC
    username: root        # 改为你的数据库用户名
    password: 125103      # 改为你的数据库密码
```

### 第四步：启动项目

找到主启动类并运行：

```
src/main/java/com/yupi/springbootinit/SpringbootInitApplication.java
```

启动成功后，服务地址为：**`http://localhost:8101/api`**

### 第五步：测试接口

**后端接口测试网址：** `http://localhost:8101/api/doc.html`

项目运行后登录上述网址，可以在 Knife4j 在线接口文档页面直接测试所有接口，无需编写前端代码。

## 接口文档

## 项目结构

```
springboot-init-master/
├── src/main/java/com/yupi/springbootinit/
│   ├── controller/        # 接口层
│   ├── service/           # 业务逻辑层
│   ├── model/             # 数据模型
│   │   ├── entity/        # 实体类
│   │   ├── dto/           # 请求/响应对象
│   │   └── vo/            # 视图对象
│   ├── common/            # 公共类
│   ├── exception/         # 异常处理
│   └── constant/          # 常量定义
├── src/main/resources/
│   └── application.yml    # 配置文件
└── sql/
    └── create_table.sql   # 数据库初始化脚本
```

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 2.7.18 | 基础框架 |
| MyBatis-Plus | 3.5.2 | ORM 框架 |
| MySQL | 8.0+ | 数据库 |
| Knife4j | 4.4.0 | 接口文档 |
| JWT | 0.11.5 | 登录认证 |
| Hutool | 5.8.8 | 工具库 |

## 常见问题

**Q: 启动报错 "Unsupported class file major version" 或 "Release version 17 not supported"**
- A: 确认 JDK 版本是否为 17，项目语言级别是否设置为 17（见第一步）

**Q: 数据库连接失败 "Communications link failure"**
- A: 检查 MySQL 服务是否启动，application.yml 中的数据库账号密码是否正确

**Q: 接口返回 401 未授权**
- A: 部分接口需要管理员权限，先调用登录接口获取 token，然后在请求头中添加 `Authorization: token`

## 许可证

仅用于内部学习交流
