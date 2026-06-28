package com.yupi.springbootinit.model.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

/**
 * 课程目标Excel导入模型
 *
 * @author YU
 */
@Data
@ColumnWidth(20)
public class CourseObjectiveExcel {

    /**
     * 课程代码（必填，用于匹配课程）
     */
    @ExcelProperty(value = "课程代码*", index = 0)
    private String courseCode;

    /**
     * 目标编号（必填，例如 CO1）
     */
    @ExcelProperty(value = "目标编号*", index = 1)
    private String objCode;

    /**
     * 目标名称（必填）
     */
    @ExcelProperty(value = "目标名称*", index = 2)
    private String objName;

    /**
     * 目标描述（选填）
     */
    @ExcelProperty(value = "目标描述", index = 3)
    private String objDesc;
}
