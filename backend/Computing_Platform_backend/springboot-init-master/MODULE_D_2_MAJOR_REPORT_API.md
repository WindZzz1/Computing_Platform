# 模块D-2 / D-3：专业级报告与穿透式台账 API 文档

## 功能概述

专业报告模块的**控制器入口、权限框架、服务层实现均已完整落地**。

- 接口路径已固定，共 5 个接口
- 角色校验已接入（`@AuthCheck(anyRole = ROLE_LEADER, ROLE_EDU)`，由 `AuthInterceptor` 真正生效）
- Token 登录访问链路已补通（JWT 拦截器写入 `currentUser`，兼容老 session）
- 雷达图数据、穿透式台账数据、台账 Excel、专业指标点达成度 Excel/PDF **均返回真实数据/文件流，不再返回「待完整实现」**

> 前置约束：雷达图与穿透式台账直接消费**三级达成度**结果（`major_indicator_achievement`）。若目标专业/学期/年级尚未执行专业级计算，服务层会返回业务错误 `该专业本学期本年级尚未计算三级达成度，请先执行专业级计算`，需先到「专业级计算」模块计算后再查询。

## 当前已开放接口

所有接口请求体统一为 `MajorReportRequest`：

```json
{
  "majorId": 1,
  "termId": 1,
  "grade": "2021"
}
```

权限要求统一为 `ROLE_LEADER` 或 `ROLE_EDU`（管理员隐式放行）。

### 1. 获取专业达成度雷达图数据

- 接口地址：`POST /major-report/radar-data`
- 实现：`MajorReportServiceImpl.getRadarChartData`
- 返回：`MajorAchievementRadarVO`（专业/学年学期/年级元信息 + `indicatorPoints` 各指标点达成度，含所属毕业要求编号/名称）
- 数据源：`major_indicator_achievement`（三级达成度结果）

### 2. 获取穿透式台账数据

- 接口地址：`POST /major-report/penetration-account`
- 实现：`MajorReportServiceImpl.getPenetrationAccount`（五层追溯，批量预取避免 N+1）
- 返回：`PenetrationAccountVO`，五层结构：
  - `majorInfo`：专业概况（课程数、学生数、整体达成度）
  - `courses`：课程/教学班粒度二级达成度
  - `studentObjectives`：学生课程目标达成度（按 studentId×classId 聚合）
  - `assessmentPoints`：考核点 × 课程目标（多对多展开）
  - `studentScores`：学生原始得分明细
- scope 圈定：`MajorScopeHelper.getTeachingClasses` 按 `course.major_id` + `termId` + `grade` 过滤

### 3. 导出穿透式台账 Excel

- 接口地址：`POST /major-report/export/account-excel`
- 实现：`MajorReportServiceImpl.exportPenetrationAccountExcel`
- 返回：`.xlsx` 文件流，5 个工作表（专业信息 / 课程达成度 / 学生课程目标 / 考核点 / 学生原始成绩）
- 文件名：`专业穿透式台账_{majorId}_{grade}.xlsx`

### 4. 导出专业指标点达成度（三级）Excel — D-3

- 接口地址：`POST /major-report/export/indicator-excel`
- 实现：`MajorReportServiceImpl.exportIndicatorAchievementExcel`
- 返回：`.xlsx` 文件流，单表「专业毕业要求达成度」（毕业要求编号/名称 + 指标点编号/名称 + 达成度，附整体达成度汇总行）
- 文件名：`专业指标点达成度_{majorId}_{grade}.xlsx`

### 5. 导出专业指标点达成度（三级）PDF — D-3

- 接口地址：`POST /major-report/export/indicator-pdf`
- 实现：`MajorReportServiceImpl.exportIndicatorAchievementPdf`
- 返回：`.pdf` 文件流，含报表信息表 + 指标点达成度表；**内嵌 LXGW WenKai 中文字体**（`resources/fonts/LXGWWenKai-Regular.ttf`，OFL 许可，subset 嵌入）
- 文件名：`专业指标点达成度_{majorId}_{grade}.pdf`

## 当前认证与权限行为

### 登录态来源

控制器已兼容：

- JWT 拦截器写入的 `request.attribute("currentUser")`
- 老 session 中的 `userId`、`userRole`

即 Token 登录与 session 登录均可正确识别用户。

### 当前业务权限规则（`validateMajorPermission`）

- `ROLE_ADMIN`：放行全部专业（超级管理员）
- `ROLE_EDU`：放行所有专业
- `ROLE_LEADER`：放行所有专业（当前为角色级放行）
- 其他角色：拒绝

> 已知遗留：`ROLE_LEADER` 当前未按 `user.major_id` 做专业归属校验（`SysUser` 无 `major_id`，仅 `collegeId`），专业级收紧需 schema 变更，已标注待后续单独处理。

## 当前错误返回

### 参数错误

```json
{ "code": 400, "message": "专业ID、学年学期ID和年级不能为空" }
```

### 权限不足

```json
{ "code": 403, "message": "无权访问该专业数据" }
```

### 前置数据未就绪（最常见，非缺陷）

```json
{ "code": 500, "message": "获取雷达图数据失败: 该专业本学期本年级尚未计算三级达成度，请先执行专业级计算" }
```

或

```json
{ "code": 500, "message": "获取穿透式台账失败: 该专业本学期本年级无教学班级数据" }
```

> 前端（`ReportView.vue`）已针对 `尚未计算三级达成度` 错误给出「请先到计算中心执行专业级计算」提示，并提供「前往计算中心」跳转入口。

## 联调建议

- 接口已完整实现，前端可直接按上述返回结构渲染雷达图、五层台账，并对接 Excel/PDF 下载，无需保留「功能开发中」占位。
- 若返回「尚未计算三级达成度」，引导用户先到「专业级计算」模块执行三级计算，再回报表页查询。

---

**文档更新时间**：2026-06-21
**说明**：本文档已按当前控制器与服务层真实实现状态校正（D-2 三接口 + D-3 两导出接口均已实现）。
