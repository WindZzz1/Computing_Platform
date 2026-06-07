# 模块C第二子模块 - 原始成绩提交功能

## 功能概述

当前已提供成绩模板下载、Excel 成绩导入、成绩分页查询、在线补录更新、按教学班清空成绩等接口。

当前文档以代码实现为准，重点说明已经可用的字段和现阶段的导入行为。

## 已实现接口

### 1. 下载成绩模板

- 接口地址: `POST /grade-entry/template/download`
- 权限要求: 主讲教师 `ROLE_TEACHER`

请求参数:

```json
{
  "classId": 123
}
```

说明:
- `classId`: 教学班 ID，必填

返回:
- Excel 文件流

### 2. 导入成绩

- 接口地址: `POST /grade-entry/import`
- 权限要求: 主讲教师 `ROLE_TEACHER`

请求参数:

```json
{
  "classId": 123,
  "excelFile": "base64编码的Excel文件内容"
}
```

说明:
- `classId`: 教学班 ID，必填
- `excelFile`: Excel 文件的 Base64 字符串，必填

返回示例:

```json
{
  "code": 0,
  "data": {
    "success": true,
    "studentCount": 35,
    "scoreCount": 175,
    "errorMessages": [],
    "warningMessages": [
      "以下学生未导入成绩：张三"
    ]
  },
  "message": "ok"
}
```

当前导入行为:
- 导入前会先解析并校验整份 Excel
- 只要出现任意错误，接口会直接返回错误信息
- 有错误时不会删除该教学班原有成绩
- 只有整份数据校验通过后，才会先清空旧成绩再写入新成绩

### 3. 成绩分页查询

- 接口地址: `POST /grade-entry/query`
- 权限要求: 主讲教师 `ROLE_TEACHER`

请求参数:

```json
{
  "classId": 123,
  "studentId": 456,
  "pointId": 789,
  "current": 1,
  "pageSize": 10
}
```

说明:
- `classId`: 教学班 ID，必填
- `studentId`: 学生 ID，可选
- `pointId`: 考核点 ID，可选
- `current`: 页码，可选，默认 `1`
- `pageSize`: 每页条数，可选，默认 `10`

返回示例:

```json
{
  "code": 0,
  "data": {
    "records": [
      {
        "id": 1,
        "studentId": 456,
        "studentNo": "2021001",
        "name": "张三",
        "pointId": 789,
        "pointCode": "A01",
        "pointName": "期末考试",
        "score": 85.5,
        "fullScore": 100.0
      }
    ],
    "total": 175,
    "size": 10,
    "current": 1
  },
  "message": "ok"
}
```

说明:
- 查询条件字段实际为 `pointId`，不是 `assessmentPointId`
- 返回中的 `score` 来自数据库实体字段 `actualScore`

### 4. 在线补录 / 更新成绩

- 接口地址: `POST /grade-entry/update`
- 权限要求: 主讲教师 `ROLE_TEACHER`

请求参数:

```json
{
  "classId": 123,
  "scores": [
    {
      "id": 1,
      "studentId": 456,
      "pointId": 789,
      "score": 88.0
    },
    {
      "studentId": 457,
      "pointId": 789,
      "score": 90.5
    }
  ]
}
```

说明:
- `classId`: 教学班 ID，必填
- `scores`: 成绩列表，必填
- `id`: 成绩记录 ID，可选
- `studentId`: 学生 ID，必填
- `pointId`: 考核点 ID，必填
- `score`: 得分，必填

返回示例:

```json
{
  "code": 0,
  "data": true,
  "message": "ok"
}
```

### 5. 清空教学班成绩

- 接口地址: `POST /grade-entry/delete`
- 权限要求: 主讲教师 `ROLE_TEACHER`

请求参数:

```json
{
  "id": 123
}
```

说明:
- `id`: 教学班 ID，必填

返回示例:

```json
{
  "code": 0,
  "data": true,
  "message": "ok"
}
```

## 当前校验规则

### 基础校验

- 教学班必须存在
- 教学班必须已经分配学生
- 教学班所属课程必须已经配置考核点

### Excel 数据校验

- 学号必须属于该教学班
- 姓名不一致会记为警告，不会直接阻断导入
- 分数不能大于考核点满分
- 分数不能小于 0
- 分数字段必须能解析为数值

### 导入结果校验

- 会统计已处理学生数
- 会统计待保存成绩记录数
- 会提示未出现在导入文件中的学生

## 当前数据结构

### GradeImportResultVO

```java
public class GradeImportResultVO {
    private Boolean success;
    private Integer studentCount;
    private Integer scoreCount;
    private List<String> errorMessages;
    private List<String> warningMessages;
}
```

### StudentScoreVO

当前代码实际返回字段如下:

```java
public class StudentScoreVO {
    private Long id;
    private Long studentId;
    private String studentNo;
    private String name;
    private Long pointId;
    private String pointCode;
    private String pointName;
    private BigDecimal score;
    private BigDecimal fullScore;
}
```

说明:
- 当前 `StudentScoreVO` 不包含 `isLocked`、`enteredBy`、`enterTime`

### StudentScoreUpdateRequest

```java
public class StudentScoreUpdateRequest {
    private Long classId;
    private List<ScoreItem> scores;

    public static class ScoreItem {
        private Long id;
        private Long studentId;
        private Long pointId;
        private BigDecimal score;
    }
}
```

## 当前限制

- 当前接口主要面向教师侧成绩录入
- 锁定成绩、审核流程等扩展能力未在本模块中实现
- 删除接口是按教学班整体清空，不是按单条成绩删除

---

**文档更新时间**: 2026-06-07
**说明**: 本文档已按当前代码实现状态校正
