package com.yupi.springbootinit.model.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

/**
 * 教学班批量导入Excel实体
 *
 * @author YU
 */
@Data
@ColumnWidth(20)
public class TeachingClassExcel {

    @ExcelProperty(value = "教学班名称*", index = 0)
    private String className;

    @ExcelProperty(value = "课程代码*", index = 1)
    private String courseCode;

    @ExcelProperty(value = "教师用户名*", index = 2)
    private String teacherUsername;

    @ExcelProperty(value = "学年*", index = 3)
    private String yearName;

    @ExcelProperty(value = "学期*", index = 4)
    private String semesterName;
}
