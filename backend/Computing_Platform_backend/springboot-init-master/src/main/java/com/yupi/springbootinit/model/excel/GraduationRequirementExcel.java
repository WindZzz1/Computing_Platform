package com.yupi.springbootinit.model.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

/**
 * 毕业要求Excel导入模型
 *
 * @author YU
 */
@Data
@ColumnWidth(20)
public class GraduationRequirementExcel {

    /**
     * 专业代码（必填，例如 080902SE）
     */
    @ExcelProperty(value = "专业代码*", index = 0)
    private String majorCode;

    /**
     * 毕业要求编号（必填，例如 GR1）
     */
    @ExcelProperty(value = "毕业要求编号*", index = 1)
    private String requirementCode;

    /**
     * 毕业要求名称（必填，例如 工程知识）
     */
    @ExcelProperty(value = "毕业要求名称*", index = 2)
    private String requirementName;

    /**
     * 毕业要求描述（选填）
     */
    @ExcelProperty(value = "毕业要求描述", index = 3)
    private String description;
}
