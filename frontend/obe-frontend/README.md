# 面向专业认证的毕业要求达成度统一计算平台 - 前端框架

这是一个 Vue 3 + Vite + TypeScript + Element Plus 的前端基础框架，已经按项目业务流程搭好页面、路由、布局、Mock 数据和接口封装目录。

## 启动方式

```bash
npm install
npm run dev
```

默认访问：`http://localhost:5173`

## 已完成内容

- 登录页与角色选择
- 后台管理布局：左侧菜单、顶部栏、内容区
- 工作台 Dashboard
- 基础数据页面：课程库、毕业要求指标点
- 宏观支撑矩阵页面：课程-指标点权重 W 配置，带列合计校验
- 课程大纲配置页面：课程目标、内部贡献权重 w、考核点
- 成绩导入与课程级计算页面：Excel 上传占位、成绩预览、课程锁定
- 专业级计算页面：课程锁定状态看板、全局计算触发
- 报表导出页面
- `api/request.ts` 已预留 Axios 封装，后续可直接对接 Swagger 接口

## 推荐后续对接方式

后端 Swagger 完成后，把接口统一写到 `src/api` 目录，例如：

```ts
// src/api/course.ts
import request from './request'
export const getCourseList = () => request.get('/courses')
```

页面中逐步替换 `src/api/mock.ts` 里的假数据即可。

## 相关文档

- `DEMO_FLOW.md`：从基础数据到专业级达成度的完整前端演示流程。
