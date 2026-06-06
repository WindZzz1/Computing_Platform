# 模块D-2：专业级报告与穿透式台账API文档

## 功能概述

该模块实现了专业级达成度报告和穿透式数据台账功能，教务管理员和专业负责人可以导出专业级达成度雷达图和穿透式数据台账，用于专业认证专家查阅和归档。

## 核心功能

### 1. 获取专业达成度雷达图数据

**接口地址**: `POST /major-report/radar-data`

**权限要求**: 教务管理员 (`ROLE_EDU`) 或专业负责人 (`ROLE_LEADER`)

**请求参数**:
```json
{
  "majorId": 1,
  "termId": 1,
  "grade": "2021",
  "reportType": "RADAR"
}
```

**参数说明**:
- `majorId`: 专业ID（必填）
- `termId`: 学年学期ID（必填）
- `grade`: 年级（必填）
- `reportType`: 报表类型（可选，默认"ACCOUNT"）

**返回示例**:
```json
{
  "code": 0,
  "data": {
    "majorId": 1,
    "majorName": "软件工程",
    "majorCode": "080902",
    "yearName": "2023-2024",
    "semesterName": "第一学期",
    "grade": "2021",
    "indicatorPoints": [
      {
        "indicatorId": 1,
        "indicatorCode": "1.1",
        "indicatorName": "工程知识",
        "achievement": 0.8500,
        "requirementId": 1,
        "requirementCode": "GR1",
        "requirementName": "工程知识"
      },
      {
        "indicatorId": 2,
        "indicatorCode": "1.2",
        "indicatorName": "问题分析",
        "achievement": 0.8200,
        "requirementId": 1,
        "requirementCode": "GR1",
        "requirementName": "工程知识"
      }
    ],
    "generatedTime": "2024-06-05 15:30:00"
  },
  "message": "ok"
}
```

### 2. 获取穿透式台账数据

**接口地址**: `POST /major-report/penetration-account`

**权限要求**: 教务管理员 (`ROLE_EDU`) 或专业负责人 (`ROLE_LEADER`)

**请求参数**: 与获取雷达图数据相同

**返回示例**:
```json
{
  "code": 0,
  "data": {
    "majorInfo": {
      "majorId": 1,
      "majorName": "软件工程",
      "majorCode": "080902",
      "termId": 1,
      "yearName": "2023-2024",
      "semesterName": "第一学期",
      "grade": "2021",
      "totalCourses": 15,
      "totalStudents": 120,
      "overallAchievement": 0.835
    },
    "courses": [
      {
        "courseId": 1,
        "courseCode": "CS3001",
        "courseName": "软件工程",
        "classId": 1,
        "className": "软件工程2021-1班",
        "teacherName": "张老师",
        "studentCount": 35,
        "objectives": [
          {
            "objectiveId": 1,
            "objectiveCode": "CO1",
            "objectiveName": "掌握软件工程基本概念",
            "indicatorCode": "1.1",
            "indicatorName": "工程知识",
            "macroWeight": 0.6
          }
        ]
      }
    ],
    "studentObjectives": [
      {
        "studentId": 1001,
        "studentNo": "2021001",
        "studentName": "张三",
        "classId": 1,
        "className": "软件工程2021-1班",
        "objectiveAchievements": {
          "CO1": 0.8700,
          "CO2": 0.8500
        },
        "averageAchievement": 0.8600
      }
    ],
    "assessmentPoints": [
      {
        "pointId": 1,
        "pointCode": "A01",
        "pointName": "期中考试",
        "fullScore": 100.0,
        "objectiveId": 1
      }
    ],
    "studentScores": [
      {
        "studentId": 1001,
        "studentNo": "2021001",
        "studentName": "张三",
        "assessmentPointCode": "A01",
        "assessmentPointName": "期中考试",
        "fullScore": 100.0,
        "score": 87.0,
        "achievement": 0.8700
      }
    ]
  },
  "message": "ok"
}
```

### 3. 导出穿透式台账Excel

**接口地址**: `POST /major-report/export/account-excel`

**权限要求**: 教务管理员 (`ROLE_EDU`) 或专业负责人 (`ROLE_LEADER`)

**请求参数**: 与获取雷达图数据相同

**返回类型**: Excel文件流 (`application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`)

**文件命名**: `专业穿透式台账_{majorId}_{grade}.xlsx`

**Excel文件结构**:
1. **专业概览** - 专业基本信息和统计数据
2. **指标点-课程关联** - 指标点与课程的关联关系及达成度
3. **课程目标-考核点** - 课程目标与考核点的对应关系
4. **学生成绩明细** - 学生在各考核点的原始得分

## 权限控制

### 访问权限验证

该模块使用双重权限验证机制：

1. **角色级验证**: 使用`@AuthCheck(anyRole = ...)`注解，确保教务管理员或专业负责人角色可以访问
2. **业务级验证**: 在服务层执行`validateMajorPermission()`方法

### 权限规则

#### 教务管理员 (ROLE_EDU)
- 可以查看所有专业数据
- 可以导出所有专业的报表
- 拥有完全访问权限

#### 专业负责人 (ROLE_LEADER)
- 可以查看所有专业数据（仅查看）
- 可以导出所有专业的报表（仅查看）
- 拥有只读访问权限

### 权限验证逻辑

```java
@Override
public boolean validateMajorPermission(Long majorId, Long userId, String userRole) {
    // 教务管理员可以查看所有专业
    if (SysUserConstant.ROLE_EDU.equals(userRole)) {
        return true;
    }

    // 专业负责人可以查看所有专业（只读权限）
    if (SysUserConstant.ROLE_LEADER.equals(userRole)) {
        return true;
    }

    return false;
}
```

## 数据追溯层级

穿透式台账提供完整的五层数据追溯：

### 第一层：专业层级
- 专业基本信息（名称、代码）
- 学年学期信息
- 年级信息
- 课程总数统计
- 学生总数统计
- 整体达成度

### 第二层：课程层级
- 课程基本信息（编号、名称）
- 教学班级信息
- 主讲教师信息
- 学生人数
- 课程指标点达成度
- 课程目标列表

### 第三层：课程目标层级
- 课程目标编号和名称
- 班级平均达成度
- 关联的指标点信息
- 宏观支撑权重
- 考核点列表

### 第四层：考核点层级
- 考核点编号和名称
- 满分和班级平均分
- 关联的课程目标
- 学生成绩列表

### 第五层：学生得分层级
- 学生基本信息（学号、姓名）
- 考核点得分
- 达成度计算结果
- 原始得分数据

## 雷达图数据结构

### 前端ECharts集成

#### 数据格式
```javascript
{
  "majorId": 1,
  "majorName": "软件工程",
  "indicatorPoints": [
    {
      "indicatorCode": "1.1",
      "indicatorName": "工程知识",
      "achievement": 0.8500
    },
    {
      "indicatorCode": "1.2",
      "indicatorName": "问题分析",
      "achievement": 0.8200
    }
  ]
}
```

#### ECharts雷达图配置
```javascript
const option = {
  title: {
    text: '专业达成度雷达图 - ' + radarData.majorName,
    left: 'center',
    top: 20
  },
  radar: {
    indicator: radarData.indicatorPoints.map(point => ({
      name: point.indicatorCode + ': ' + point.indicatorName,
      max: 1.0
    })),
    radius: '60%'
  },
  series: [{
    type: 'radar',
    data: [{
      value: radarData.indicatorPoints.map(p => p.achievement),
      name: '达成度',
      areaStyle: { color: 'rgba(128, 128, 128, 0.3)' }
    }]
  }]
};
```

## 错误处理

### 常见错误情况

#### 1. 参数错误
```json
{
  "code": 400,
  "message": "专业ID、学年学期ID和年级不能为空"
}
```

#### 2. 权限不足
```json
{
  "code": 403,
  "message": "无权访问该专业数据"
}
```

#### 3. 数据不存在
```json
{
  "code": 404,
  "message": "专业不存在"
}
```

#### 4. 操作失败
```json
{
  "code": 50001,
  "message": "该专业尚未计算达成度，请先进行专业级计算"
}
```

## 使用流程

### 标准使用流程

1. **确认专业级计算完成**
   - 确保专业已完成课程级达成度计算
   - 确认已进行专业级达成度计算

2. **生成雷达图数据**
   - 调用`/major-report/radar-data`接口
   - 前端使用ECharts渲染雷达图
   - 认证专家直观查看专业达成度整体情况

3. **获取穿透式台账**
   - 调用`/major-report/penetration-account`接口
   - 验证五层数据追溯完整性

4. **导出Excel台账**
   - 调用`/major-report/export/account-excel`接口
   - 获得多工作表Excel文件
   - 提供给认证专家查阅和归档

### 示例代码

#### JavaScript/TypeScript示例

```typescript
// 获取雷达图数据
async function getRadarData(majorId: number, termId: number, grade: string) {
  const response = await fetch('/major-report/radar-data', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      majorId: majorId,
      termId: termId,
      grade: grade
    })
  });

  const result = await response.json();
  if (result.code === 0) {
    console.log('雷达图数据：', result.data);
    return result.data;
  } else {
    console.error('获取雷达图数据失败：', result.message);
  }
}

// 导出穿透式台账Excel
async function exportAccountExcel(majorId: number, termId: number, grade: string) {
  const response = await fetch('/major-report/export/account-excel', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      majorId: majorId,
      termId: termId,
      grade: grade
    })
  });

  if (response.ok) {
    const blob = await response.blob();
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `专业穿透式台账_${majorId}_${grade}.xlsx`;
    a.click();
    window.URL.revokeObjectURL(url);
  }
}
```

## Excel文件结构说明

### 工作表1：专业概览
| 项目 | 内容 |
|------|------|
| 专业名称 | 软件工程 |
| 专业代码 | 080902 |
| 学年学期 | 2023-2024 第一学期 |
| 年级 | 2021 |
| 课程总数 | 15 |
| 学生总数 | 120 |

### 工作表2：指标点-课程关联
| 指标点编号 | 指标点名称 | 课程编号 | 课程名称 | 教学班级 | 主讲教师 | 课程达成度 | 宏观权重 |
|------------|------------|----------|----------|----------|----------|------------|----------|
| 1.1 | 工程知识 | CS3001 | 软件工程 | 软件2021-1班 | 张老师 | 0.850 | 0.6 |

### 工作作表3：课程目标-考核点
| 课程编号 | 课程名称 | 课程目标编号 | 课程目标名称 | 考核点编号 | 考核点名称 | 满分 | 班级平均分 |
|----------|----------|--------------|--------------|------------|------------|------|------------|
| CS3001 | 软件工程 | CO1 | 掌握软件工程基本概念 | A01 | 期中考试 | 100 | 85.5 |

### 工作表4：学生成绩明细
| 学号 | 姓名 | 课程编号 | 课程名称 | 考核点编号 | 考核点名称 | 满分 | 得分 | 达成度 |
|------|------|----------|----------|------------|------------|------|------|--------|
| 2021001 | 张三 | CS3001 | 软件工程 | A01 | 期中考试 | 100 | 87 | 0.87 |

## 技术特性

### 1. 性能优化

- **批量查询**: 使用批量查询减少数据库访问次数
- **分页处理**: 大数据量时采用分页导出
- **事务管理**: 使用只读事务确保数据一致性

### 2. 数据完整性

- **五层追溯**: 确保数据可追溯到最底层的学生原始得分
- **关联验证**: 验证各层级数据的关联关系正确性
- **计算精度**: 使用BigDecimal确保计算精度

### 3. 导出格式特性

#### Excel多工作表
- **专业概览**: 基本信息和统计数据
- **指标点-课程关联**: 支撑关系和达成度
- **课程目标-考核点**: 详细的教学设计信息
- **学生成绩明细**: 原始得分数据

#### 雷达图数据
- **JSON格式**: 便于前端解析和渲染
- **标准化结构**: 适配ECharts等图表库
- **完整信息**: 包含所有必要的元数据

## 后续扩展

该模块为后续功能提供基础：
- **多专业对比**: 支持多个专业的达成度对比分析
- **历史趋势**: 支持不同学期的数据趋势对比
- **质量评估**: 提供专业层面的教学质量评估报告
- **认证支撑**: 为专业认证提供完整的数据支撑材料

---

**开发日期**: 2026-06-05  
**开发者**: Claude AI  
**版本**: 1.0