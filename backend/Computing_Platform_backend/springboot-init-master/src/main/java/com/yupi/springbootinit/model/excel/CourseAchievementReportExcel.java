package com.yupi.springbootinit.model.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 课程目标达成情况评价报表Excel模型
 *
 * @author YU
 */
@Data
@ColumnWidth(15)
public class CourseAchievementReportExcel implements Serializable {

    private static final long serialVersionUID = 1L;

    @ExcelProperty(value = "学号", index = 0)
    private String studentNo;

    @ExcelProperty(value = "姓名", index = 1)
    private String studentName;

    /**
     * 课程目标达成度（动态列）
     * 这个字段用于EasyExcel动态列填充
     */
    @ExcelProperty(value = "课程目标达成度", index = 2)
    private String objectiveAchievements;

    @ExcelProperty(value = "平均达成度", index = 3)
    private BigDecimal averageAchievement;
}