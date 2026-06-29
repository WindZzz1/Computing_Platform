package com.yupi.springbootinit.model.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

/**
 * 宏观支撑矩阵Excel导入模型（课程-指标点支撑关系）
 *
 * @author YU
 */
@Data
@ColumnWidth(20)
public class MatrixCourseIndicatorExcel {

    /**
     * 专业代码（必填，例如 080902SE）
     */
    @ExcelProperty(value = "专业代码*", index = 0)
    private String majorCode;

    /**
     * 课程代码（必填，例如 SE101）
     */
    @ExcelProperty(value = "课程代码*", index = 1)
    private String courseCode;

    /**
     * 指标点编号（必填，例如 1.1）
     */
    @ExcelProperty(value = "指标点编号*", index = 2)
    private String indicatorCode;

    /**
     * 宏观总支撑权重（必填，0~1之间的小数）
     */
    @ExcelProperty(value = "宏观总支撑权重*", index = 3)
    private java.math.BigDecimal totalWeight;
}
