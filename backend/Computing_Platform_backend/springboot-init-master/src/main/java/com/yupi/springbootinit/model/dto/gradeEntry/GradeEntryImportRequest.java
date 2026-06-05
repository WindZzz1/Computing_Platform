package com.yupi.springbootinit.model.dto.gradeEntry;

import lombok.Data;

import java.io.Serializable;

/**
 * 成绩导入请求
 *
 * @author YU
 */
@Data
public class GradeEntryImportRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 教学班级ID
     */
    private Long classId;

    /**
     * Excel文件Base64编码
     */
    private String excelFile;
}
