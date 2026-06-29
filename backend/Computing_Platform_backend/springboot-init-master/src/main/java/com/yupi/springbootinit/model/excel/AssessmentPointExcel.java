package com.yupi.springbootinit.model.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

/**
 * 考核点Excel导入模型（含考核点-课程目标支撑权重）
 *
 * @author YU
 */
@Data
@ColumnWidth(20)
public class AssessmentPointExcel {

    /**
     * 课程代码（必填，用于匹配课程）
     */
    @ExcelProperty(value = "课程代码*", index = 0)
    private String courseCode;

    /**
     * 考核点编号（必填，例如 AP1）
     */
    @ExcelProperty(value = "考核点编号*", index = 1)
    private String pointCode;

    /**
     * 考核点名称（必填）
     */
    @ExcelProperty(value = "考核点名称*", index = 2)
    private String pointName;

    /**
     * 满分值（必填）
     */
    @ExcelProperty(value = "满分值*", index = 3)
    private java.math.BigDecimal fullScore;

    /**
     * 关联目标编号（必填，多个用英文逗号分隔，例如 "CO1,CO2"）
     */
    @ExcelProperty(value = "关联目标编号*", index = 4)
    private String objectiveCodes;

    /**
     * 支撑权重（必填，多个用英文逗号分隔，与关联目标编号按位置一一对应，例如 "0.3,0.7"）
     */
    @ExcelProperty(value = "支撑权重*", index = 5)
    private String weights;
}
