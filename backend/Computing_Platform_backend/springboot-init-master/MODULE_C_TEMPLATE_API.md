# 模块C - 成绩录入模板生成功能

## 功能概述

该功能实现了模块C的第一个核心功能：**主讲教师完成模块B配置后，系统自动生成Excel成绩录入空模板**。

## API接口

### 1. 生成并下载成绩录入模板

**接口地址**: `POST /grade-entry/template/download`

**权限要求**: 主讲教师 (`ROLE_TEACHER`)

**请求参数**:
```json
{
  "classId": 123
}
```

**参数说明**:
- `classId`: 教学班级ID（必填）

**返回类型**: Excel文件下载

**响应示例**:
```
Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
Content-Disposition: attachment;filename*=utf-8''2023-2021_软件工程_1班_成绩录入模板_5个考核点.xlsx
```

## 功能特性

### 1. 模板表头结构

生成的Excel模板包含以下列：

| 列名 | 说明 | 示例 |
|------|------|------|
| 学号 | 学生学号 | "2021001" |
| 姓名 | 学生姓名 | "张三" |
| 考核点1 | 按考核点编号排序，包含考核点编号、名称和满分值 | "A01-期末考试\n满分:100" |
| 考核点2 | 同上 | "A02-平时作业\n满分:20" |
| ... | 其他考核点 | ... |

### 2. 文件命名规则

```
{学年学期}_{课程名称}_{班级名称}_成绩录入模板_{考核点数量}个考核点.xlsx
```

示例：`2023-2021_软件工程_1班_成绩录入模板_5个考核点.xlsx`

### 3. 数据验证

系统会进行以下验证：
- 教学班级是否存在
- 课程是否存在
- 主讲教师是否存在
- 学年学期是否存在
- 班级是否有学生（至少1名）
- 课程是否有考核点（至少1个）

### 4. 错误处理

常见错误情况：
- 班级不存在：返回404错误
- 班级无学生：返回错误提示"该班级暂无学生，请先添加学生"
- 课程无考核点：返回错误提示"该课程暂无考核点，请先配置考核点"
- 参数错误：返回400错误

## 使用流程

1. **配置完成模块B**：主讲教师完成课程的考核点配置
2. **创建教学班级**：教务管理员或主讲教师创建教学班级并绑定学生
3. **生成模板**：主讲教师调用API生成成绩录入模板
4. **填写成绩**：主讲教师下载Excel模板，线下填写学生成绩
5. **导入成绩**：（待实现）主讲教师将填写完成的Excel导入系统

## 数据结构

### GradeEntryTemplateRequest
```java
public class GradeEntryTemplateRequest implements Serializable {
    private Long classId; // 教学班级ID
}
```

### GradeEntryExcelData
```java
public class GradeEntryExcelData implements Serializable {
    private String studentNo;           // 学号
    private String name;                // 姓名
    private Double[] assessmentScores;  // 考核点成绩数组
}
```

## 技术实现

- **Excel处理**: 使用EasyExcel库
- **文件编码**: UTF-8，支持中文文件名
- **表头生成**: 动态表头，根据考核点数量自动调整
- **数据排序**: 
  - 学生按绑定ID排序
  - 考核点按编号排序
- **响应设置**: 自动设置正确的Content-Type和Content-Disposition头

## 代码结构

### Controller
- `GradeEntryController`: 处理HTTP请求，权限验证

### Service
- `GradeEntryService`: 业务逻辑接口
- `GradeEntryServiceImpl`: 核心业务逻辑实现

### DTO/VO
- `GradeEntryTemplateRequest`: 请求参数
- `GradeEntryExcelData`: Excel数据模型

## 测试建议

1. **正常场景测试**:
   - 有学生的班级生成模板
   - 多个考核点的课程生成模板
   - 文件名正确包含所有信息

2. **异常场景测试**:
   - 空学生班级
   - 无考核点课程
   - 不存在的班级ID
   - 无权限用户访问

3. **Excel格式测试**:
   - 表头格式正确
   - 数据行数正确
   - 列数与考核点数匹配
   - 中文字符正确显示

## 后续功能

该功能为模块C的起点，后续功能包括：
- 成绩Excel导入功能
- 成绩自动计算功能（达成度计算）
- 成绩锁定功能
- 成绩审核功能

---

**开发日期**: 2026-06-04  
**开发者**: Claude AI  
**版本**: 1.0
