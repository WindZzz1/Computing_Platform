package com.yupi.springbootinit.model.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

/**
 * 课程目标-指标点内部贡献权重 Excel 导入模型
 * 约束：同一课程下，支撑同一指标点的所有课程目标的内部权重之和必须为 1.0
 *
 * @author YU
 */
@Data
@ColumnWidth(22)
public class WeightObjectiveIndicatorExcel {

    /**
     * 课程代码（必填，用于匹配课程，如 SE101）
     */
    @ExcelProperty(value = "课程代码*", index = 0)
    private String courseCode;

    /**
     * 课程目标编号（必填，如 CO1）
     */
    @ExcelProperty(value = "课程目标编号*", index = 1)
    private String objCode;

    /**
     * 指标点编号（必填，如 1.1）
     */
    @ExcelProperty(value = "指标点编号*", index = 2)
    private String indicatorCode;

    /**
     * 内部权重（必填，0~1 之间，如 0.25）
     */
    @ExcelProperty(value = "内部权重*", index = 3)
    private String innerWeight;
}
