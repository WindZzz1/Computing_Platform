package com.yupi.springbootinit.util;

import com.alibaba.excel.EasyExcel;
import com.yupi.springbootinit.model.excel.CourseExcel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 模板生成工具类
 * 用于生成静态的 Excel 导入模板文件
 *
 * @author YU
 */
public class TemplateGenerator {

    public static void main(String[] args) {
        generateCourseTemplate();
    }

    /**
     * 生成课程导入模板
     */
    public static void generateCourseTemplate() {
        // 模板文件路径
        File templateDir = new File("src/main/resources/templates");
        File templateFile = new File(templateDir, "course_template.xlsx");

        // 确保目录存在
        if (!templateDir.exists()) {
            templateDir.mkdirs();
        }

        // 生成只包含表头的模板（不含示例数据）
        EasyExcel.write(templateFile, CourseExcel.class)
                .sheet("课程导入模板")
                .doWrite(new ArrayList<>());

        System.out.println("课程模板生成成功: " + templateFile.getAbsolutePath());
    }
}
