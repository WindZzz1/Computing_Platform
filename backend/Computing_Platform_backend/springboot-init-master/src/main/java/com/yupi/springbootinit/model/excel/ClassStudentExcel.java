package com.yupi.springbootinit.model.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

/**
 * 教学班学生导入Excel实体
 *
 * @author YU
 */
@Data
@ColumnWidth(20)
public class ClassStudentExcel {

    @ExcelProperty(value = "学号*", index = 0)
    private String studentNo;

    @ExcelProperty(value = "姓名", index = 1)
    private String studentName;

}
