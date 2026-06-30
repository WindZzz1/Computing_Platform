package com.yupi.springbootinit.model.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

/**
 * 学生导入Excel实体
 *
 * @author YU
 */
@Data
@ColumnWidth(20)
public class StudentExcel {

    @ExcelProperty(value = "姓名*", index = 0)
    private String studentName;

    @ExcelProperty(value = "学号*", index = 1)
    private String studentNo;

    @ExcelProperty(value = "年级", index = 2)
    private String grade;

    @ExcelProperty(value = "专业代码*", index = 3)
    private String majorCode;

    @ExcelProperty(value = "班级", index = 4)
    private String className;

}
