package com.yupi.springbootinit.model.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

/**
 * 课程导入Excel实体
 *
 * @author YU
 */
@Data
@ColumnWidth(20)
public class CourseExcel {

    @ExcelProperty(value = "所属专业*", index = 0)
    private String major;

    @ExcelProperty(value = "课程代码*", index = 1)
    private String courseCode;

    @ExcelProperty(value = "课程名称*", index = 2)
    private String courseName;

    @ExcelProperty(value = "课程性质*", index = 3)
    private String courseNature;

    @ExcelProperty(value = "学分*", index = 4)
    private Double credit;

    @ExcelProperty(value = "专业代码", index = 5)
    private String majorCode;
}
