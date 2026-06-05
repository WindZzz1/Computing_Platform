# 模块C第三子模块 - 课程级达成度一键计算功能

## 功能概述

该功能实现了模块C的第三个核心功能：**主讲教师确认原始成绩无误后，触发课程级达成度一键计算，系统自动完成一级和二级达成度计算，并自动锁定成绩**。

## 核心功能

### 1. 一键计算功能

**接口地址**: `POST /achievement-calculation/calculate`

**权限要求**: 主讲教师 (`ROLE_TEACHER`)

**请求参数**:
```json
{
  "classId": 123,
  "forceRecalculate": false
}
```

**参数说明**:
- `classId`: 教学班级ID（必填）
- `forceRecalculate`: 是否强制重新计算（可选，默认false）

**返回示例**:
```json
{
  "code": 0,
  "data": {
    "success": true,
    "calcStatus": 2,
    "isLocked": true,
    "calcStartTime": "2024-06-04T10:00:00",
    "calcEndTime": "2024-06-04T10:00:15",
    "lockTime": "2024-06-04T10:00:15",
    "levelOneStats": {
      "totalStudents": 35,
      "totalObjectives": 4,
      "totalRecords": 140,
      "averageAchievement": 0.8250,
      "minAchievement": 0.6500,
      "maxAchievement": 0.9500
    },
    "levelTwoStats": {
      "totalIndicators": 6,
      "totalRecords": 6,
      "averageAchievement": 0.8000,
      "minAchievement": 0.7200,
      "maxAchievement": 0.8900,
      "achievements": [
        {
          "indicatorId": 1,
          "indicatorCode": "1.1",
          "indicatorName": "工程知识",
          "achievement": 0.8500
        }
      ]
    }
  },
  "message": "ok"
}
```

### 2. 计算状态查询功能

**接口地址**: `POST /achievement-calculation/status`

**权限要求**: 主讲教师 (`ROLE_TEACHER`)

**请求参数**:
```json
{
  "classId": 123
}
```

**返回示例**:
```json
{
  "code": 0,
  "data": {
    "calcStatus": 2,
    "isLocked": true,
    "calcStartTime": "2024-06-04T10:00:00",
    "calcEndTime": "2024-06-04T10:00:15",
    "lockTime": "2024-06-04T10:00:15",
    "errorMessage": null
  },
  "message": "ok"
}
```

### 3. 成绩解锁功能（管理员）

**接口地址**: `POST /achievement-calculation/unlock`

**权限要求**: 管理员 (`ROLE_ADMIN`)

**请求参数**:
```json
{
  "classId": 123,
  "forceRecalculate": true
}
```

**返回示例**:
```json
{
  "code": 0,
  "data": true,
  "message": "ok"
}
```

## 计算逻辑

### 1. 一级达成度计算（学生课程目标达成度）

**计算公式**:
```
学生课程目标达成度 = Σ(考核点得分/考核点满分 × 支撑权重) / Σ(支撑权重)
```

**计算步骤**:
1. 获取教学班级的所有学生
2. 获取课程的所有课程目标
3. 获取考核点-课程目标关联关系及支撑权重
4. 对于每个学生和每个课程目标：
   - 找到支撑该课程目标的所有考核点
   - 计算每个考核点的贡献度：得分/满分 × 支撑权重
   - 汇总所有考核点的贡献度，除以权重总和
5. 保存一级达成度数据

**示例计算**:
```
假设课程目标CO1由两个考核点支撑：
- 考核点A01：满分100，学生得分85，支撑权重0.6
- 考核点A02：满分20，学生得分18，支撑权重0.4

学生CO1达成度 = (85/100 × 0.6 + 18/20 × 0.4) / (0.6 + 0.4)
             = (0.85 × 0.6 + 0.9 × 0.4) / 1.0
             = (0.51 + 0.36) / 1.0
             = 0.87
```

### 2. 二级达成度计算（课程级指标点达成度）

**计算公式**:
```
课程指标点达成度 = Σ(平均一级达成度 × 内部贡献权重) / Σ(内部贡献权重)
```

**计算步骤**:
1. 获取课程的所有课程目标
2. 获取课程目标-指标点权重关系及内部贡献权重
3. 对于每个指标点：
   - 找到支撑该指标点的所有课程目标
   - 计算每个课程目标的平均一级达成度
   - 计算每个课程目标的贡献度：平均达成度 × 内部权重
   - 汇总所有课程目标的贡献度，除以权重总和
4. 保存二级达成度数据

**示例计算**:
```
假设指标点1.1由两个课程目标支撑：
- 课程目标CO1：学生平均达成度0.87，内部权重0.7
- 课程目标CO2：学生平均达成度0.82，内部权重0.3

指标点1.1达成度 = (0.87 × 0.7 + 0.82 × 0.3) / (0.7 + 0.3)
                = (0.609 + 0.246) / 1.0
                = 0.855
```

## 计算状态管理

### 计算状态枚举
- `0` - 未计算
- `1` - 计算中
- `2` - 计算完成
- `3` - 计算失败

### 状态流转规则
```
未计算 → [触发计算] → 计算中 → [计算成功] → 计算完成
                          ↓
                     [计算失败] → 计算失败
```

### 锁定机制
- **自动锁定**: 计算完成后自动锁定成绩
- **防止重复计算**: 计算中状态不允许重复触发
- **解锁条件**: 仅管理员可解锁，或使用强制重新计算

## 数据验证

### 前置条件检查
1. **教学班级检查**: 班级必须存在
2. **学生数据检查**: 班级必须有学生
3. **成绩数据检查**: 班级必须有成绩数据
4. **课程目标检查**: 课程必须有课程目标
5. **权重配置检查**: 课程目标必须有指标点权重配置

### 计算过程验证
1. **关联关系验证**: 考核点-课程目标关联关系完整
2. **权重数据验证**: 权重值合理，总和不为零
3. **成绩范围验证**: 成绩值在合理范围内
4. **达成度范围验证**: 达成度值在0-1之间

## 错误处理

### 常见错误情况

1. **数据不完整**
   - 班级无学生数据
   - 班级无成绩数据
   - 课程无课程目标配置

2. **计算中状态**
   - 重复触发计算
   - 计算超时

3. **成绩已锁定**
   - 非管理员尝试修改
   - 未使用强制重新计算

### 错误信息示例
```json
{
  "success": false,
  "calcStatus": 3,
  "errorMessage": "计算失败：该课程暂无课程目标配置"
}
```

## 数据结构

### AchievementCalculationResultVO
```java
public class AchievementCalculationResultVO {
    private Boolean success;                    // 是否成功
    private Integer calcStatus;                 // 计算状态
    private Boolean isLocked;                   // 是否已锁定
    private Date calcStartTime;                // 计算开始时间
    private Date calcEndTime;                  // 计算完成时间
    private Date lockTime;                     // 锁定时间
    private String errorMessage;               // 错误信息
    private LevelOneAchievementStats levelOneStats;   // 一级达成度统计
    private LevelTwoAchievementStats levelTwoStats;   // 二级达成度统计
}
```

### GradeCalculationStatus
```java
public class GradeCalculationStatus {
    private Long id;                           // 主键ID
    private Long classId;                      // 教学班级ID
    private Integer isLocked;                  // 是否已锁定
    private Integer calcStatus;                // 计算状态
    private Date calcStartTime;               // 计算开始时间
    private Date calcEndTime;                 // 计算完成时间
    private Date lockTime;                    // 锁定时间
    private Long lockedBy;                    // 锁定人ID
    private String lockReason;                // 锁定原因
    private String errorMessage;              // 错误信息
}
```

## 数据库表结构

### student_objective_achievement表
```sql
CREATE TABLE student_objective_achievement (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    teaching_class_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    objective_id BIGINT NOT NULL,
    objective_code VARCHAR(50),
    objective_name VARCHAR(255),
    achievement DECIMAL(10,4),
    calculate_time DATETIME,
    create_time DATETIME,
    update_time DATETIME,
    is_deleted TINYINT DEFAULT 0,
    INDEX idx_class_student_objective (teaching_class_id, student_id, objective_id)
);
```

### course_indicator_achievement表
```sql
CREATE TABLE course_indicator_achievement (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    teaching_class_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    indicator_id BIGINT NOT NULL,
    indicator_code VARCHAR(50),
    indicator_name VARCHAR(255),
    achievement DECIMAL(10,4),
    calculate_time DATETIME,
    create_time DATETIME,
    update_time DATETIME,
    is_deleted TINYINT DEFAULT 0,
    INDEX idx_class_indicator (teaching_class_id, indicator_id)
);
```

### grade_calculation_status表
```sql
CREATE TABLE grade_calculation_status (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    teaching_class_id BIGINT NOT NULL,
    is_locked TINYINT DEFAULT 0,
    calc_status TINYINT DEFAULT 0,
    calc_start_time DATETIME,
    calc_end_time DATETIME,
    lock_time DATETIME,
    locked_by BIGINT,
    lock_reason VARCHAR(500),
    error_message TEXT,
    create_time DATETIME,
    update_time DATETIME,
    is_deleted TINYINT DEFAULT 0,
    UNIQUE INDEX uk_teaching_class_id (teaching_class_id)
);
```

## 使用流程

### 1. 标准计算流程
```
1. 主讲教师确认成绩无误
2. 调用计算接口触发一键计算
3. 系统自动执行一级和二级达成度计算
4. 计算完成后自动锁定成绩
5. 主讲教师查看计算结果
```

### 2. 强制重新计算流程
```
1. 管理员解锁已锁定的成绩
2. 主讲教师修改原始数据
3. 使用forceRecalculate=true重新计算
4. 系统重新计算并重新锁定
```

### 3. 状态监控流程
```
1. 触发计算后定期查询状态
2. 根据状态显示进度提示
3. 计算完成后展示结果
4. 失败时显示错误信息
```

## 技术特性

### 1. 性能优化
- **批量处理**: 使用批量插入提高数据库操作效率
- **索引优化**: 数据库表索引优化查询性能
- **事务管理**: 确保数据一致性和完整性

### 2. 计算精度
- **高精度计算**: 使用BigDecimal确保计算精度
- **标准舍入**: 采用HALF_UP舍入模式
- **四位小数**: 计算结果保留4位小数

### 3. 并发控制
- **状态锁定**: 计算中状态防止重复触发
- **事务隔离**: 确保并发操作的数据一致性
- **错误回滚**: 计算失败自动回滚数据

### 4. 安全特性
- **权限控制**: 仅主讲教师可触发计算
- **数据锁定**: 计算完成后自动锁定数据
- **操作记录**: 记录计算时间和操作人信息
- **错误追踪**: 详细记录失败原因

## 后续扩展

该功能为后续功能提供数据基础：
- **达成度报告**: 基于计算结果生成详细报告
- **统计分析**: 多维度达成度统计分析
- **趋势分析**: 历史达成度趋势对比
- **质量评估**: 课程教学质量评估

---

**开发日期**: 2026-06-04  
**开发者**: Claude AI  
**版本**: 1.0
