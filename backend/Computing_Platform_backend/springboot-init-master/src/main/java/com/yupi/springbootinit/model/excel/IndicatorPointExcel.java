package com.yupi.springbootinit.model.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

/**
 * 二级指标点Excel导入模型
 *
 * @author YU
 */
@Data
@ColumnWidth(20)
public class IndicatorPointExcel {

    /**
     * 毕业要求编码（必填，用于匹配毕业要求）
     */
    @ExcelProperty(value = "毕业要求编码*", index = 0)
    private String requirementCode;

    /**
     * 指标点编号（必填，例如 1.1、2.3）
     */
    @ExcelProperty(value = "指标点编号*", index = 1)
    private String indicatorCode;

    /**
     * 指标点名称（必填）
     */
    @ExcelProperty(value = "指标点名称*", index = 2)
    private String indicatorName;

    /**
     * 指标点描述（选填）
     */
    @ExcelProperty(value = "指标点描述", index = 3)
    private String description;
}
