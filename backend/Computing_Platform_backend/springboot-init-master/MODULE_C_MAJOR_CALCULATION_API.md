# 模块C第四子模块 - 专业级全局达成度计算功能

## 功能概述

该功能实现了模块C的第四个核心功能：**专业负责人或教务管理员在所有支撑课程完成计算并锁定后，触发专业级全局达成度计算（三级达成度）**。

## 核心功能

### 1. 课程状态监控看板

**接口地址**: `POST /major-calculation/dashboard`

**权限要求**: 专业负责人 (`ROLE_LEADER`) 或 教务管理员 (`ROLE_EDU`)

**请求参数**:
```json
{
  "majorId": 1,
  "termId": 2,
  "grade": "2021级"
}
```

**参数说明**:
- `majorId`: 专业ID（必填）
- `termId`: 学年学期ID（可选）
- `grade`: 年级（可选）

**返回示例**:
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
    "calculatedCourses": 8,
    "lockedCourses": 8,
    "calcStatus": 0,
    "courseStatusList": [
      {
        "classId": 1,
        "className": "软件工程1班",
        "courseName": "软件工程",
        "teacherName": "张老师",
        "calcStatus": 2,
        "isLocked": true,
        "studentCount": 35,
        "statusDescription": "已完成并锁定"
      }
    ]
  },
  "message": "ok"
}
```

### 2. 课程计算状态分页查询

**接口地址**: `POST /major-calculation/course-status`

**权限要求**: 专业负责人 (`ROLE_LEADER`) 或 教务管理员 (`ROLE_EDU`)

**请求参数**:
```json
{
  "majorId": 1,
  "termId": 2,
  "grade": "2021级",
  "current": 1,
  "pageSize": 10
}
```

### 3. 专业级达成度计算

**接口地址**: `POST /major-calculation/calculate`

**权限要求**: 专业负责人 (`ROLE_LEADER`) 或 教务管理员 (`ROLE_EDU`)

**请求参数**:
```json
{
  "majorId": 1,
  "termId": 2,
  "grade": "2021级",
  "forceRecalculate": false
}
```

**返回示例**:
```json
{
  "code": 0,
  "data": {
    "success": true,
    "majorId": 1,
    "majorName": "计算机科学与技术",
    "termId": 2,
    "grade": "2021级",
    "totalCourses": 8,
    "calculatedCourses": 8,
    "lockedCourses": 8,
    "calcStatus": 2,
    "calcStartTime": "2024-06-04T14:00:00",
    "calcEndTime": "2024-06-04T14:00:30",
    "achievementStats": {
      "totalIndicators": 12,
      "totalRecords": 12,
      "averageAchievement": 0.8150,
      "minAchievement": 0.7200,
      "maxAchievement": 0.9100,
      "meetsGraduationRequirement": true,
      "threshold": 0.7,
      "achievements": [
        {
          "indicatorId": 1,
          "indicatorCode": "1.1",
          "indicatorName": "工程知识",
          "requirementCode": "GR1",
          "requirementName": "工程知识",
          "achievement": 0.8500,
          "meetsThreshold": true,
          "supportingCourseCount": 3
        }
      ]
    }
  },
  "message": "ok"
}
```

### 4. 查询专业级计算结果

**接口地址**: `POST /major-calculation/result`

**权限要求**: 专业负责人 (`ROLE_LEADER`) 或 教务管理员 (`ROLE_EDU`)

**请求参数**:
```json
{
  "majorId": 1,
  "termId": 2,
  "grade": "2021级"
}
```

### 5. 删除专业级计算结果

**接口地址**: `POST /major-calculation/delete`

**权限要求**: 管理员 (`ROLE_ADMIN`)

**请求参数**:
```json
{
  "majorId": 1,
  "termId": 2,
  "grade": "2021级"
}
```

## 计算逻辑

### 三级达成度计算（专业级指标点达成度）

**计算公式**:
```
专业级指标点达成度 = Σ(课程级指标点达成度 × 宏观总支撑权重Wc) / Σ(宏观总支撑权重Wc)
```

**计算步骤**:
1. 获取专业涉及的所有课程和教学班级
2. 验证所有课程都已完成二级达成度计算并锁定
3. 查询课程-指标点宏观支撑矩阵（权重Wc）
4. 对于每个指标点：
   - 找到支撑该指标点的所有课程
   - 获取每个课程该指标点的二级达成度
   - 计算加权平均：Σ(二级达成度 × Wc) / Σ(Wc)
5. 保存三级达成度结果
6. 统计分析并判断是否满足毕业要求

**计算示例**:
```
假设指标点1.1由三门课程支撑：
- 软件工程：二级达成度0.85，宏观权重Wc=0.4
- 数据结构：二级达成度0.82，宏观权重Wc=0.3
- 算法设计：二级达成度0.88，宏观权重Wc=0.3

专业级指标点达成度 = (0.85×0.4 + 0.82×0.3 + 0.88×0.3) / (0.4+0.3+0.3)
                    = (0.34 + 0.246 + 0.264) / 1.0
                    = 0.85
```

## 前置校验机制

### 1. 数据完整性检查
- **专业信息验证**: 专业必须存在
- **学年学期验证**: 学年学期必须存在
- **课程数据验证**: 必须有相关教学班级

### 2. 计算状态检查
- **课程计算状态**: 所有课程必须完成计算（calcStatus=2）
- **课程锁定状态**: 所有课程必须已锁定（isLocked=1）
- **重复触发检查**: 防止计算中状态重复触发

### 3. 宏观支撑矩阵检查
- **矩阵配置验证**: 必须配置课程-指标点宏观支撑矩阵
- **权重数据验证**: 宏观权重值必须合理且总和不为零

### 4. 二级达成度数据检查
- **数据完整性**: 所有课程必须有二级达成度数据
- **数据有效性**: 达成度值必须在合理范围内

## 状态管理

### 计算状态枚举
- `0` - 未计算（满足计算条件）
- `1` - 计算中
- `2` - 计算完成
- `3` - 不满足计算条件

### 课程计算状态描述
- `未计算`: 课程尚未开始达成度计算
- `计算中`: 课程正在计算一级和二级达成度
- `已完成并锁定`: 课程完成计算并锁定成绩
- `已完成未锁定`: 课程完成计算但未锁定
- `计算失败`: 课程计算过程中出现错误

## 数据结构

### MajorCalculationResultVO
```java
public class MajorCalculationResultVO {
    private Boolean success;                    // 是否成功
    private Long majorId;                       // 专业ID
    private String majorName;                   // 专业名称
    private Long termId;                        // 学年学期ID
    private String termName;                    // 学年学期名称
    private String grade;                       // 年级
    private Integer calcStatus;                 // 计算状态
    private Integer totalCourses;               // 涉及课程总数
    private Integer calculatedCourses;          // 已计算课程数
    private Integer lockedCourses;              // 已锁定课程数
    private Date calcStartTime;                // 计算开始时间
    private Date calcEndTime;                  // 计算完成时间
    private Long calculatedBy;                  // 计算人ID
    private String errorMessage;               // 错误信息
    private LevelThreeAchievementStats achievementStats; // 三级达成度统计
    private List<CourseCalculationStatusVO> courseStatusList; // 课程状态列表
}
```

### CourseCalculationStatusVO
```java
public class CourseCalculationStatusVO {
    private Long classId;                      // 教学班级ID
    private String className;                  // 班级名称
    private Long courseId;                     // 课程ID
    private String courseName;                 // 课程名称
    private String courseCode;                 // 课程代码
    private Long teacherId;                    // 主讲教师ID
    private String teacherName;                // 主讲教师姓名
    private Integer calcStatus;                // 计算状态
    private Boolean isLocked;                  // 是否已锁定
    private Integer studentCount;              // 学生人数
    private Date calcEndTime;                  // 计算完成时间
    private Date lockTime;                     // 锁定时间
    private String statusDescription;          // 状态描述
}
```

## 数据库表结构

### major_indicator_achievement表
```sql
CREATE TABLE major_indicator_achievement (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    major_id BIGINT NOT NULL COMMENT '专业ID',
    term_id BIGINT COMMENT '学年学期ID',
    grade VARCHAR(20) COMMENT '年级',
    indicator_id BIGINT NOT NULL COMMENT '指标点ID',
    indicator_code VARCHAR(50) COMMENT '指标点编号',
    indicator_name VARCHAR(255) COMMENT '指标点名称',
    requirement_id BIGINT COMMENT '毕业要求ID',
    requirement_code VARCHAR(50) COMMENT '毕业要求编号',
    requirement_name VARCHAR(255) COMMENT '毕业要求名称',
    achievement DECIMAL(10,4) COMMENT '三级达成度值',
    calculate_time DATETIME COMMENT '计算时间',
    calc_status TINYINT DEFAULT 0 COMMENT '计算状态',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除',
    INDEX idx_major_term_grade (major_id, term_id, grade),
    INDEX idx_indicator_id (indicator_id)
);
```

### major_calculation_summary表
```sql
CREATE TABLE major_calculation_summary (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    major_id BIGINT NOT NULL COMMENT '专业ID',
    term_id BIGINT COMMENT '学年学期ID',
    grade VARCHAR(20) COMMENT '年级',
    total_courses INT DEFAULT 0 COMMENT '涉及课程总数',
    calculated_courses INT DEFAULT 0 COMMENT '已计算课程数',
    locked_courses INT DEFAULT 0 COMMENT '已锁定课程数',
    calc_status TINYINT DEFAULT 0 COMMENT '计算状态',
    calc_start_time DATETIME COMMENT '计算开始时间',
    calc_end_time DATETIME COMMENT '计算完成时间',
    calculated_by BIGINT COMMENT '计算人ID',
    error_message TEXT COMMENT '错误信息',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除',
    UNIQUE INDEX uk_major_term_grade (major_id, term_id, grade)
);
```

## 使用流程

### 1. 标准专业级计算流程
```
1. 专业负责人登录系统
2. 进入专业达成度计算模块
3. 查看课程状态监控看板
4. 确认所有课程都已完成并锁定
5. 点击"执行专业级计算"按钮
6. 系统自动执行三级达成度计算
7. 查看计算结果和统计信息
8. 确认是否满足毕业要求
```

### 2. 监控看板使用流程
```
1. 选择专业、学年学期和年级
2. 系统展示所有相关课程状态
3. 查看各课程计算进度和锁定状态
4. 识别未完成或未锁定的课程
5. 联系相关主讲教师完善数据
6. 满足条件后触发全局计算
```

### 3. 结果查询和分析流程
```
1. 查询历史计算结果
2. 分析各项指标点达成度
3. 查看是否满足毕业要求阈值
4. 生成专业达成度报告
5. 为教学改进提供依据
```

## 错误处理

### 常见错误情况

1. **前置条件不满足**
   - 课程未完成计算
   - 课程未锁定
   - 缺少宏观支撑矩阵配置

2. **数据不完整**
   - 没有相关教学班级
   - 缺少二级达成度数据
   - 宏观权重配置错误

3. **计算状态异常**
   - 重复触发计算
   - 计算超时
   - 权限不足

### 错误信息示例
```json
{
  "success": false,
  "calcStatus": 3,
  "errorMessage": "课程软件工程未完成计算或未锁定，无法进行专业级计算"
}
```

## 技术特性

### 1. 数据汇聚机制
- **多课程整合**: 汇聚所有课程的二级达成度结果
- **宏观权重应用**: 结合宏观支撑矩阵进行加权计算
- **层次化计算**: 保持三级达成度的层次关系

### 2. 实时监控功能
- **状态追踪**: 实时跟踪各课程计算和锁定状态
- **进度展示**: 可视化展示专业级计算进度
- **异常预警**: 及时发现和处理计算异常

### 3. 高精度计算
- **BigDecimal计算**: 使用高精度数值计算
- **标准化舍入**: 统一使用HALF_UP舍入模式
- **四位小数精度**: 保持0.0001的计算精度

### 4. 权限控制
- **多角色支持**: 支持专业负责人和教务管理员
- **分级权限**: 不同角色有不同的操作权限
- **操作审计**: 记录计算操作人和时间

## 毕业要求判定

### 阈值设定
- **默认阈值**: 0.7（70%）
- **阈值可配置**: 支持不同专业设定不同阈值
- **逐项判定**: 每个指标点都要达到阈值要求

### 判定逻辑
```java
// 所有指标点都达到阈值则满足毕业要求
boolean meetsRequirement = allAchievements.stream()
    .allMatch(achievement -> achievement.compareTo(THRESHOLD) >= 0);
```

### 结果展示
- **总体判定**: 是否满足毕业要求
- **详细分析**: 每个指标点的达成情况
- **问题识别**: 标识未达标的指标点
- **改进建议**: 为教学改进提供数据支持

## 后续扩展

该功能为后续功能提供数据基础：
- **达成度报告生成**: 基于三级达成度生成专业报告
- **趋势分析**: 分析历届学生的达成度变化趋势
- **质量评估**: 评估专业教学质量和培养效果
- **认证支持**: 为工程教育认证提供数据支撑
- **持续改进**: 基于达成度结果进行教学持续改进

---

**开发日期**: 2026-06-04  
**开发者**: Claude AI  
**版本**: 1.0
