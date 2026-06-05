# 模块C第二子模块 - 原始成绩提交功能

## 功能概述

该功能实现了模块C的第二个核心功能：**主讲教师上传填写完成的Excel成绩模板，系统自动解析、校验并存储成绩数据，同时提供在线预览和补录功能**。

## 核心功能

### 1. 成绩导入功能

**接口地址**: `POST /grade-entry/import`

**权限要求**: 主讲教师 (`ROLE_TEACHER`)

**请求参数**:
```json
{
  "classId": 123,
  "excelFile": "base64编码的Excel文件内容"
}
```

**参数说明**:
- `classId`: 教学班级ID（必填）
- `excelFile`: 填写完成的Excel文件的Base64编码字符串（必填）

**返回示例**:
```json
{
  "code": 0,
  "data": {
    "success": true,
    "studentCount": 35,
    "scoreCount": 175,
    "errorMessages": [],
    "warningMessages": ["以下学生未导入成绩：张三"]
  },
  "message": "ok"
}
```

### 2. 成绩查询功能

**接口地址**: `POST /grade-entry/query`

**权限要求**: 主讲教师 (`ROLE_TEACHER`)

**请求参数**:
```json
{
  "classId": 123,
  "studentId": 456,
  "assessmentPointId": 789,
  "current": 1,
  "pageSize": 10
}
```

**参数说明**:
- `classId`: 教学班级ID（必填）
- `studentId`: 学生ID（可选，用于筛选特定学生）
- `assessmentPointId`: 考核点ID（可选，用于筛选特定考核点）
- `current`: 当前页码（默认1）
- `pageSize`: 每页大小（默认10）

**返回示例**:
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
        "assessmentPointId": 789,
        "pointCode": "A01",
        "pointName": "期末考试",
        "score": 85.5,
        "fullScore": 100.0,
        "isLocked": 0,
        "enteredBy": 123,
        "enterTime": "2024-06-04T10:30:00"
      }
    ],
    "total": 175,
    "size": 10,
    "current": 1
  },
  "message": "ok"
}
```

### 3. 成绩更新功能

**接口地址**: `POST /grade-entry/update`

**权限要求**: 主讲教师 (`ROLE_TEACHER`)

**请求参数**:
```json
{
  "classId": 123,
  "scores": [
    {
      "id": 1,
      "studentId": 456,
      "assessmentPointId": 789,
      "score": 88.0
    },
    {
      "studentId": 457,
      "assessmentPointId": 789,
      "score": 90.5
    }
  ]
}
```

**参数说明**:
- `classId`: 教学班级ID（必填）
- `scores`: 成绩列表（必填）
  - `id`: 成绩记录ID（可选，存在则更新，不存在则新增）
  - `studentId`: 学生ID（必填）
  - `assessmentPointId`: 考核点ID（必填）
  - `score`: 得分（必填）

**返回示例**:
```json
{
  "code": 0,
  "data": true,
  "message": "ok"
}
```

### 4. 删除班级成绩功能

**接口地址**: `POST /grade-entry/delete`

**权限要求**: 主讲教师 (`ROLE_TEACHER`)

**请求参数**:
```json
{
  "id": 123
}
```

**参数说明**:
- `id`: 教学班级ID（必填）

**返回示例**:
```json
{
  "code": 0,
  "data": true,
  "message": "ok"
}
```

## 数据校验规则

### 1. 基础校验
- 教学班级必须存在
- 教学班级必须有学生
- 课程必须有考核点

### 2. Excel数据校验
- **学号校验**: 学号必须存在于该教学班级中
- **姓名校验**: 姓名应与学生信息匹配（不匹配会发出警告）
- **分数校验**: 
  - 分数不能超过考核点满分
  - 分数不能为负数
  - 分数格式必须正确

### 3. 数据完整性校验
- 自动检测未导入成绩的学生并发出警告
- 统计导入的学生数量和成绩记录数量

## 数据结构

### GradeImportResultVO
```java
public class GradeImportResultVO {
    private Boolean success;           // 是否成功
    private Integer studentCount;       // 导入的学生数量
    private Integer scoreCount;         // 导入的成绩记录数量
    private List<String> errorMessages; // 错误信息列表
    private List<String> warningMessages; // 警告信息列表
}
```

### StudentScoreVO
```java
public class StudentScoreVO {
    private Long id;                    // 成绩ID
    private Long studentId;            // 学生ID
    private String studentNo;          // 学号
    private String name;               // 姓名
    private Long assessmentPointId;    // 考核点ID
    private String pointCode;          // 考核点编号
    private String pointName;          // 考核点名称
    private BigDecimal score;          // 得分
    private BigDecimal fullScore;      // 满分
    private Integer isLocked;          // 是否锁定
    private Long enteredBy;            // 录入人ID
    private Date enterTime;            // 录入时间
}
```

### StudentScoreUpdateRequest
```java
public class StudentScoreUpdateRequest {
    private Long classId;              // 教学班级ID
    private List<ScoreItem> scores;   // 成绩列表
    
    public static class ScoreItem {
        private Long id;               // 成绩ID（可选）
        private Long studentId;        // 学生ID
        private Long assessmentPointId;// 考核点ID
        private BigDecimal score;      // 得分
    }
}
```

## 使用流程

### 1. 成绩导入流程
1. 主讲教师下载Excel成绩录入模板
2. 填写学生成绩数据
3. 将Excel文件转换为Base64编码
4. 调用导入接口上传数据
5. 系统自动解析、校验并存储数据
6. 查看导入结果，处理错误和警告

### 2. 在线预览与补录流程
1. 调用查询接口获取班级成绩数据
2. 在Web界面展示成绩表格
3. 检查数据的完整性和准确性
4. 对缺失或错误的数据进行在线修改
5. 调用更新接口保存修改后的数据

## 错误处理

### 常见错误情况

1. **参数错误**
   - 教学班级ID为空
   - Excel文件为空
   - 成绩数据格式错误

2. **权限错误**
   - 用户不是主讲教师
   - 教学班级不属于当前教师

3. **数据验证错误**
   - 学号不存在
   - 分数超过满分
   - 分数为负数
   - Excel格式错误

### 错误信息示例
```json
{
  "success": false,
  "errorMessages": [
    "学号2021999不存在于该班级",
    "学号2021001在考核点A01的得分105超过满分100",
    "学号2021002在考核点A02的得分格式错误：abc"
  ],
  "warningMessages": [
    "学号2021003的姓名不匹配，期望：李四，实际：李四四",
    "以下学生未导入成绩：王五、赵六"
  ]
}
```

## 数据库表结构

### student_score表
```sql
CREATE TABLE student_score (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    teaching_class_id BIGINT NOT NULL COMMENT '教学班级ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    assessment_point_id BIGINT NOT NULL COMMENT '考核点ID',
    score DECIMAL(10,2) COMMENT '得分',
    is_locked TINYINT DEFAULT 0 COMMENT '是否锁定：0-未锁定，1-已锁定',
    entered_by BIGINT COMMENT '录入人ID',
    enter_time DATETIME COMMENT '录入时间',
    update_time DATETIME COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除',
    INDEX idx_class_id (teaching_class_id),
    INDEX idx_student_id (student_id),
    INDEX idx_assessment_point_id (assessment_point_id)
);
```

## 技术特性

### 1. 数据处理
- **Base64编码**: 支持Excel文件的Base64编码传输
- **批量导入**: 使用EasyExcel高效解析大量数据
- **事务处理**: 确保数据一致性，导入失败自动回滚

### 2. 数据验证
- **多层级验证**: 参数校验 → 业务校验 → 数据校验
- **智能匹配**: 自动关联学生、考核点信息
- **友好提示**: 详细的错误和警告信息

### 3. 性能优化
- **批量操作**: 使用批量插入提高性能
- **索引优化**: 数据库表索引优化查询性能
- **分页查询**: 支持大数据量的分页展示

### 4. 安全特性
- **权限控制**: 仅主讲教师可操作
- **数据隔离**: 不同教学班级数据完全隔离
- **操作记录**: 记录录入人和录入时间

## 后续扩展

该功能为后续功能提供数据基础：
- **成绩锁定**: 支持成绩锁定功能，防止误修改
- **达成度计算**: 基于成绩数据自动计算课程达成度
- **成绩审核**: 专业负责人和教务管理员审核流程
- **统计分析**: 成绩统计分析和报表生成

---

**开发日期**: 2026-06-04  
**开发者**: Claude AI  
**版本**: 1.0
