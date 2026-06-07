# 模块D-1：课程级评价报表 API 文档

## 功能概述

当前控制器已经开放课程级报表相关入口，但服务层仍是占位实现。

也就是说:

- 接口地址、权限拦截、导出响应头等已经具备
- 真实报表数据生成、Excel 导出、PDF 导出暂未完成

前端或联调时请把它视为“接口框架已接通，业务内容待补完”。

## 当前已开放接口

### 1. 获取报表数据

- 接口地址: `POST /course-achievement-report/data`
- 权限要求: 主讲教师 `ROLE_TEACHER`

请求参数:

```json
{
  "classId": 123
}
```

当前行为:
- 会先校验 `classId`
- 会先做教师角色校验
- 会再调用 `validateReportPermission(classId, userId)`
- 然后进入服务层 `generateReportData(classId)`

当前真实状态:
- 控制器已实现
- 服务层方法当前直接抛出 `报表生成功能待完整实现`

### 2. 导出 Excel 报表

- 接口地址: `POST /course-achievement-report/export/excel`
- 权限要求: 主讲教师 `ROLE_TEACHER`

请求参数:

```json
{
  "classId": 123
}
```

当前真实状态:
- 控制器已实现
- 文件名规则已实现
- 服务层方法当前直接抛出 `Excel导出功能待完整实现`

### 3. 导出 PDF 报表

- 接口地址: `POST /course-achievement-report/export/pdf`
- 权限要求: 主讲教师 `ROLE_TEACHER`

请求参数:

```json
{
  "classId": 123
}
```

当前真实状态:
- 控制器已实现
- 文件名规则已实现
- 服务层方法当前直接抛出 `PDF导出功能待完整实现`

### 4. 下载报表模板

- 接口地址: `GET /course-achievement-report/template`
- 权限要求: 主讲教师 `ROLE_TEACHER`

当前真实状态:
- 控制器已实现
- 会从类路径 `templates/course_achievement_report_template.xlsx` 读取模板并输出

## 当前权限行为

### 认证来源

当前控制器已兼容两种登录态来源:

- JWT 拦截器写入的 `request.attribute("currentUser")`
- 会话中的 `session.userId`

这意味着:
- Token 登录可以走通控制器
- 老的 session 登录方式也仍可兼容

### 当前业务权限校验现状

控制器会调用:

```java
courseAchievementReportService.validateReportPermission(classId, userId)
```

但当前服务实现中:

```java
public boolean validateReportPermission(Long classId, Long userId) {
    return true;
}
```

说明:
- 当前并没有真正校验“该教师是否是该教学班主讲教师”
- 旧文档中的严格业务权限校验逻辑并未落地

## 当前错误返回

### 参数缺失

```json
{
  "code": 400,
  "message": "教学班级ID不能为空"
}
```

### 权限不足

```json
{
  "code": 403,
  "message": "您不是该课程的主讲教师，无权访问报表"
}
```

说明:
- 这条错误分支在控制器中存在
- 但由于当前 `validateReportPermission()` 恒为 `true`，正常情况下不会因为业务归属校验触发

### 功能未完成

```json
{
  "code": 500,
  "message": "获取报表数据失败: 报表生成功能待完整实现"
}
```

或

```json
{
  "code": 500,
  "message": "Excel导出失败: Excel导出功能待完整实现"
}
```

## 联调建议

当前阶段前端如果要对接本模块，建议按下面理解处理:

- 可以先把页面入口、按钮、参数结构接好
- 可以先走教师权限和 token 登录流程
- 数据展示页、Excel/PDF 成果页要预留“开发中 / 暂未开放”提示

不建议当前就依赖这些接口完成正式业务联调，因为核心服务还未实现。

---

**文档更新时间**: 2026-06-07
**说明**: 本文档已按当前控制器与服务实现状态校正
