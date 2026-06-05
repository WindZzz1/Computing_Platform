package com.yupi.springbootinit.model.vo.gradeEntry;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.io.Serializable;

/**
 * 成绩录入模板Excel数据模型
 *
 * @author YU
 */
@Data
public class GradeEntryTemplateVO implements Serializable {

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
     * 考核点成绩列（动态生成）
     * 使用索引方式访问：scores[0]对应第一个考核点，scores[1]对应第二个考核点，以此类推
     */
    @ExcelProperty(value = "")
    @ColumnWidth(20)
    private Double[] scores;
}