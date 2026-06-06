# 模块D-1：课程级评价报表（主讲教师）API文档

## 功能概述

该模块实现了课程目标达成情况评价报表的生成和导出功能，主讲教师可以导出所授课程的达成度评价报表，用于课程教学效果分析和认证材料归档。

## 核心功能

### 1. 获取报表数据

**接口地址**: `POST /course-achievement-report/data`

**权限要求**: 主讲教师 (`ROLE_TEACHER`)

**请求参数**:
```json
{
  "classId": 123,
  "exportFormat": "EXCEL",
  "includeStudentDetails": true,
  "includeIndicatorAchievement": true
}
```

**参数说明**:
- `classId`: 教学班级ID（必填）
- `exportFormat`: 导出格式（可选，默认"EXCEL"）
- `includeStudentDetails`: 是否包含学生明细（可选，默认true）
- `includeIndicatorAchievement`: 是否包含指标点达成度（可选，默认true）

**返回示例**:
```json
{
  "code": 0,
  "data": {
    "classId": 123,
    "className": "软件工程2021-1班",
    "courseCode": "CS3001",
    "courseName": "软件工程",
    "teacherName": "张老师",
    "yearName": "2023-2024",
    "semesterName": "第一学期",
    "studentCount": 35,
    "objectiveSummaries": [
      {
        "objectiveId": 1,
        "objectiveCode": "CO1",
        "objectiveName": "掌握软件工程基本概念",
        "classAverage": 0.8250,
        "maxScore": 0.9500,
        "minScore": 0.6500,
        "passRate": 0.9143,
        "studentCount": 35
      }
    ],
    "studentDetails": [
      {
        "studentId": 1001,
        "studentNo": "2021001",
        "studentName": "张三",
        "objectiveAchievements": {
          "CO1": 0.8700,
          "CO2": 0.8500
        },
        "averageAchievement": 0.8600
      }
    ],
    "indicatorAchievements": [
      {
        "indicatorId": 1,
        "indicatorCode": "1.1",
        "indicatorName": "工程知识",
        "achievement": 0.8500,
        "calculationTime": "2024-06-04 10:00:15"
      }
    ],
    "reportGeneratedTime": "2024-06-05 14:30:00",
    "calculationTime": "2024-06-04 10:00:15"
  },
  "message": "ok"
}
```

### 2. 导出Excel格式报表

**接口地址**: `POST /course-achievement-report/export/excel`

**权限要求**: 主讲教师 (`ROLE_TEACHER`)

**请求参数**: 与获取报表数据相同

**返回类型**: Excel文件流 (`application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`)

**文件命名**: `课程目标达成情况评价表_123.xlsx`

### 3. 导出PDF格式报表

**接口地址**: `POST /course-achievement-report/export/pdf`

**权限要求**: 主讲教师 (`ROLE_TEACHER`)

**请求参数**: 与获取报表数据相同

**返回类型**: PDF文件流 (`application/pdf`)

**文件命名**: `课程目标达成情况评价表_123.pdf`

## 权限控制

### 访问权限验证

该模块使用双重权限验证机制：

1. **角色级验证**: 使用`@AuthCheck(mustRole = SysUserConstant.ROLE_TEACHER)`注解，确保只有教师角色的用户可以访问

2. **业务级验证**: 在服务层执行`validateReportPermission()`方法，验证当前用户是否为该教学班级的主讲教师

### 权限验证逻辑

```java
@Override
public boolean validateReportPermission(Long classId, Long userId) {
    TeachingClass teachingClass = teachingClassMapper.selectById(classId);
    return teachingClass != null && teachingClass.getTeacherId().equals(userId);
}
```

## 错误处理

### 常见错误情况

#### 1. 参数错误
```json
{
  "code": 400,
  "message": "教学班级ID不能为空"
}
```

#### 2. 权限不足
```json
{
  "code": 403,
  "message": "您不是该课程的主讲教师，无权访问报表"
}
```

#### 3. 数据不存在
```json
{
  "code": 404,
  "message": "教学班级不存在"
}
```

#### 4. 操作失败
```json
{
  "code": 50001,
  "message": "该班级尚未计算达成度，请先进行成绩计算"
}
```

## 使用流程

### 标准使用流程

1. **确认达成度计算完成**
   - 确保教学班级已完成成绩录入
   - 确认已进行达成度计算

2. **获取报表数据**
   - 调用`/course-achievement-report/data`接口
   - 验证报表数据正确性

3. **导出报表文件**
   - 根据需求选择Excel或PDF格式
   - 调用对应的导出接口

4. **文件归档和分析**
   - Excel格式用于数据编辑和后续分析
   - PDF格式用于打印归档

### 示例代码

#### JavaScript/TypeScript示例

```typescript
// 获取报表数据
async function getReportData(classId: number) {
  const response = await fetch('/course-achievement-report/data', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      classId: classId,
      exportFormat: 'EXCEL',
      includeStudentDetails: true,
      includeIndicatorAchievement: true
    })
  });
  
  const result = await response.json();
  if (result.code === 0) {
    console.log('报表数据：', result.data);
    return result.data;
  } else {
    console.error('获取报表失败：', result.message);
  }
}

// 导出Excel报表
async function exportExcelReport(classId: number) {
  const response = await fetch('/course-achievement-report/export/excel', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ classId: classId })
  });
  
  if (response.ok) {
    const blob = await response.blob();
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `课程目标达成情况评价表_${classId}.xlsx`;
    a.click();
    window.URL.revokeObjectURL(url);
  }
}

// 导出PDF报表
async function exportPdfReport(classId: number) {
  const response = await fetch('/course-achievement-report/export/pdf', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ classId: classId })
  });
  
  if (response.ok) {
    const blob = await response.blob();
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `课程目标达成情况评价表_${classId}.pdf`;
    a.click();
    window.URL.revokeObjectURL(url);
  }
}
```

## 报表内容说明

### 1. 教学班级基本信息

- 课程名称和编号
- 教学班级名称
- 主讲教师姓名
- 学年学期
- 学生人数
- 报表生成时间

### 2. 课程目标达成度汇总

- 课程目标编号和名称
- 班级平均达成度
- 最高分和最低分
- 及格率（达成度≥0.7的学生比例）

### 3. 学生达成度明细

- 学生学号和姓名
- 各课程目标达成度
- 平均达成度

### 4. 课程指标点达成度

- 指标点编号和名称
- 二级达成度值
- 计算时间

## 技术特性

### 1. 性能优化

- **批量查询**: 使用批量查询减少数据库访问次数
- **内存管理**: 采用分页处理避免内存溢出
- **缓存策略**: 对相同查询条件的数据进行短期缓存

### 2. 数据精度

- **高精度计算**: 使用BigDecimal确保计算精度
- **标准舍入**: 采用HALF_UP舍入模式
- **四位小数**: 计算结果保留4位小数

### 3. 导出格式特性

#### Excel格式
- **动态表头**: 根据课程目标数量自动调整列数
- **数据完整**: 包含所有学生详细数据
- **便于编辑**: 支持后续数据分析

#### PDF格式
- **专业排版**: 标准A4页面格式
- **中文支持**: 使用系统中文字体
- **表格清晰**: 自动分页和格式化
- **适合归档**: 便于打印和长期保存

## 后续扩展

该模块为后续功能提供基础：
- **专业级评价报表**: 扩展专业层面的达成度分析
- **多维度统计**: 增加更多统计维度和图表
- **历史对比**: 支持不同学期的数据对比
- **质量评估**: 提供课程教学质量评估报告

---

**开发日期**: 2026-06-05  
**开发者**: Claude AI  
**版本**: 1.0