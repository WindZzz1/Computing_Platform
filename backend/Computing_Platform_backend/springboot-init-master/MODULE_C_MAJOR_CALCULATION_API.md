# 模块C第四子模块 - 专业级全局达成度计算功能

## 功能概述

当前后端已经提供专业级达成度计算的基础接口，包括:

- 看板概览 `dashboard`
- 触发计算 `calculate`
- 查询结果 `result`
- 删除结果 `delete`

需要特别说明的是，当前实现是“可运行的基础版本”，并不是完整业务闭环版本。文档中以下内容均以现有代码为准。

## 当前已实现接口

### 1. 获取看板概览

- 接口地址: `POST /major-calculation/dashboard`
- 权限要求: `ROLE_LEADER` 或 `ROLE_EDU`

请求参数:

```json
{
  "majorId": 1,
  "termId": 2,
  "grade": "2021级"
}
```

说明:
- `majorId`: 专业 ID，必填
- `termId`: 学年学期 ID，可选
- `grade`: 年级，可选

当前返回核心字段:

```json
{
  "code": 0,
  "data": {
    "majorId": 1,
    "majorName": "计算机科学与技术",
    "termId": 2,
    "termName": "2023-2024学年第一学期",
    "grade": "2021级",
    "totalCourses": 8,
    "coursesWithData": 6,
    "canCalculate": false,
    "errorMessage": "还有课程未计算达成度，无法进行专业级计算",
    "courseStatusList": [
      {
        "classId": 1,
        "className": "软件工程1班",
        "courseId": 12,
        "hasAchievementData": true,
        "achievementDataCount": 4
      }
    ]
  },
  "message": "ok"
}
```

说明:
- 当前接口返回的是简化看板数据
- 当前并没有返回完整的锁定状态、教师姓名、学生人数等字段

### 2. 执行专业级达成度计算

- 接口地址: `POST /major-calculation/calculate`
- 权限要求: `ROLE_LEADER` 或 `ROLE_EDU`

请求参数:

```json
{
  "majorId": 1,
  "termId": 2,
  "grade": "2021级"
}
```

返回示例:

```json
{
  "code": 0,
  "data": {
    "success": true,
    "majorId": 1,
    "majorName": "计算机科学与技术",
    "termId": 2,
    "termName": "2023-2024学年第一学期",
    "grade": "2021级",
    "totalCourses": 8,
    "totalIndicators": 12,
    "totalRecords": 12,
    "averageAchievement": 0.8150,
    "minAchievement": 0.7200,
    "maxAchievement": 0.9100,
    "threshold": 0.7,
    "meetsGraduationRequirement": true,
    "achievements": [
      {
        "indicatorId": 1,
        "indicatorCode": "1.1",
        "indicatorName": "工程知识",
        "requirementId": 1,
        "requirementCode": "GR1",
        "requirementName": "工程知识",
        "achievement": 0.8500,
        "meetsThreshold": true,
        "supportingCourseCount": 3
      }
    ],
    "calcStatus": 2,
    "calcEndTime": "2026-06-07T10:00:00"
  },
  "message": "ok"
}
```

### 3. 查询专业级计算结果

- 接口地址: `POST /major-calculation/result`
- 权限要求: `ROLE_LEADER` 或 `ROLE_EDU`

请求参数:

```json
{
  "majorId": 1,
  "termId": 2,
  "grade": "2021级"
}
```

说明:
- 若已有三级达成度记录，返回 `calcStatus = 2`
- 若当前没有记录，返回 `calcStatus = 0`

### 4. 删除专业级计算结果

- 接口地址: `POST /major-calculation/delete`
- 权限要求: `ROLE_ADMIN`

请求参数:

```json
{
  "majorId": 1,
  "termId": 2,
  "grade": "2021级"
}
```

## 当前未提供的接口

以下接口在旧文档中出现过，但当前控制器中并不存在:

- `POST /major-calculation/course-status`

如前端需要该接口，需后续单独补做。

## 当前实现逻辑

### 计算基础

当前服务会:

- 读取教学班对应的课程级指标点达成度
- 读取专业课程与指标点的宏观支撑矩阵
- 按指标点做加权平均
- 将结果写入 `major_indicator_achievement`

计算公式:

```text
专业级指标点达成度 = Σ(课程级指标点达成度 × 宏观总支撑权重) / Σ(宏观总支撑权重)
```

### 当前前置检查

- `majorId` 必填
- `termId` 如果传入，则会校验学期是否存在
- 必须能查到教学班
- 每个教学班必须已有课程级达成度数据
- 必须已配置宏观支撑矩阵

## 当前实现限制

### 专业 / 年级筛选限制

当前 `getTeachingClasses(majorId, termId, grade)` 的实现仍然是基础版:

- `termId` 会真正参与筛选
- `majorId` 与 `grade` 目前没有完整落到学生维度过滤逻辑
- 代码中明确保留了“后续按实际业务逻辑筛选”的注释

这意味着:
- 现阶段看板和计算结果主要依赖 `termId`
- 当同一学期下有多个专业或多个年级混合数据时，结果可能比最终设计更宽

### 锁定状态限制

当前版本不会校验:

- 课程是否已锁定
- 是否存在单独的“计算中”状态流转
- 是否存在教学班级级别的完成审批流程

当前判断条件只有:
- 该教学班是否已有课程级指标点达成度数据

### 返回结构限制

当前返回值是 `Map<String, Object>` 形式的简化结构，不是完整强类型 VO。

## 当前状态字段

现有实现中常见状态值:

- `0`: 未计算
- `2`: 计算完成
- `3`: 计算失败

说明:
- 旧文档中的更细粒度流程状态并未完整实现

## 适合前端对接的范围

当前前端适合先对接:

- 看板概览展示
- 发起专业级计算
- 查询历史计算结果
- 删除历史结果

暂不建议前端按照旧文档直接实现:

- 单独的课程状态分页页签
- 锁定状态驱动的复杂流程
- 过度依赖 `majorId + grade` 精确过滤的统计页面

---

**文档更新时间**: 2026-06-07
**说明**: 本文档已按当前控制器与服务实现状态校正
