#!/bin/bash

echo "# 实体类字段检查"
echo ""

# 定义要检查的实体类
entities=(
    "SysUser"
    "TeachingClass"
    "Student"
    "StudentScore"
    "Course"
    "CourseObjective"
    "IndicatorPoint"
    "MatrixCourseIndicator"
    "WeightObjectiveIndicator"
)

for entity in "${entities[@]}"; do
    file="src/main/java/com/yupi/springbootinit/model/entity/$entity.java"
    if [ -f "$file" ]; then
        echo "## $entity"
        grep -E "@TableField|private.*;" "$file" | grep -v "^//" | head -20
        echo ""
    fi
done
