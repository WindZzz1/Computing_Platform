package com.yupi.springbootinit.model.vo.gradeEntry;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 成绩录入模板动态Excel数据模型
 * 支持动态表头和动态列数
 *
 * @author YU
 */
@Data
@NoArgsConstructor
public class GradeEntryExcelData implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 学号
     */
    @ExcelProperty("学号")
    @ColumnWidth(15)
    private String studentNo;

    /**
     * 姓名
     */
    @ExcelProperty("姓名")
    @ColumnWidth(15)
    private String name;

    /**
     * 考核点成绩（动态列）
     * 构造函数中动态添加 @ExcelProperty 注解
     */
    private Double[] assessmentScores;

    public GradeEntryExcelData(String studentNo, String name, Double[] assessmentScores) {
        this.studentNo = studentNo;
        this.name = name;
        this.assessmentScores = assessmentScores;
    }
}