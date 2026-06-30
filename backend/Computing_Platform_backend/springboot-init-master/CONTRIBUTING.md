# 贡献指南

感谢您对 Computing Platform Backend 项目的关注！我们欢迎任何形式的贡献。

## 📋 目录

- [行为准则](#行为准则)
- [如何贡献](#如何贡献)
- [开发流程](#开发流程)
- [代码规范](#代码规范)
- [提交规范](#提交规范)
- [问题报告](#问题报告)
- [功能建议](#功能建议)

## 🤝 行为准则

请阅读并遵守我们的 [行为准则](CODE_OF_CONDUCT.md)。

## 🚀 如何贡献

### 环境准备

1. **Fork 仓库**
   ```bash
   # 在 GitHub 上点击 Fork 按钮
   ```

2. **克隆到本地**
   ```bash
   git clone https://github.com/your-username/Computing_Platform.git
   cd Computing_Platform/springboot-init-master
   ```

3. **设置上游仓库**
   ```bash
   git remote add upstream https://github.com/WindZzz1/Computing_Platform.git
   ```

4. **配置开发环境**
   - 安装 JDK 17
   - 安装 Maven 3.6+
   - 安装 MySQL 8.0+
   - 参考 [README](README.md) 完成环境配置

## 🔄 开发流程

### 1. 创建功能分支

```bash
git checkout -b feature/your-feature-name
# 或
git checkout -b fix/your-bug-fix
```

### 2. 进行开发

- 遵循项目代码规范
- 添加必要的测试用例
- 更新相关文档
- 确保本地测试通过

### 3. 提交代码

```bash
git add .
git commit -m "feat: add user authentication"
```

### 4. 同步最新代码

```bash
git fetch upstream
git rebase upstream/main
```

### 5. 推送到你的仓库

```bash
git push origin feature/your-feature-name
```

### 6. 创建 Pull Request

- 在 GitHub 上创建 PR
- 使用我们的 [PR 模板](.github/PULL_REQUEST_TEMPLATE.md)
- 等待代码审查

## 📝 代码规范

### Java 代码规范

1. **命名规范**
   - 类名：大驼峰 `UserService`
   - 方法名：小驼峰 `getUserById`
   - 常量：全大写 `MAX_COUNT`

2. **注释规范**
   ```java
   /**
    * 根据用户ID获取用户信息
    *
    * @param userId 用户ID
    * @return 用户信息
    * @throws UserNotFoundException 用户不存在异常
    */
   public User getUserById(Long userId) {
       // 实现逻辑
   }
   ```

3. **异常处理**
   - 使用自定义业务异常
   - 提供清晰的错误信息
   - 记录异常日志

4. **数据库操作**
   - 使用 MyBatis-Plus
   - 避免在循环中查询数据库
   - 使用事务管理

### Git 提交规范

使用 [Conventional Commits](https://www.conventionalcommits.org/) 规范：

- `feat`: 新功能
- `fix`: Bug 修复
- `docs`: 文档更新
- `style`: 代码格式调整
- `refactor`: 代码重构
- `test`: 测试相关
- `chore`: 构建/工具链相关

示例：
```bash
git commit -m "feat: 添加用户登录功能"
git commit -m "fix: 修复数据库连接超时问题"
git commit -m "docs: 更新 API 文档"
```

## 🐛 问题报告

使用 [Bug 报告模板](.github/ISSUE_TEMPLATE/bug_report.md) 提交问题：

1. 搜索现有 Issues，避免重复
2. 使用清晰的标题
3. 提供详细的重现步骤
4. 附上相关截图和日志
5. 标注环境信息

## 💡 功能建议

使用 [功能请求模板](.github/ISSUE_TEMPLATE/feature_request.md) 提出建议：

1. 说明功能的使用场景
2. 描述预期行为
3. 提供实现思路（如有）
4. 讨论影响范围

## ✅ 检查清单

提交 PR 前请确认：

- [ ] 代码符合项目规范
- [ ] 已添加单元测试
- [ ] 所有测试通过 (`mvn test`)
- [ ] 代码构建成功 (`mvn clean package`)
- [ ] 已更新相关文档
- [ ] 提交信息清晰规范
- [ ] PR 描述详细完整

## 📞 联系方式

如有疑问，请通过以下方式联系：

- 提交 GitHub Issue
- 发送邮件到项目邮箱
- 加入项目讨论群

## 📄 许可证

提交代码即表示您同意将代码以项目许可证发布。

再次感谢您的贡献！🎉
