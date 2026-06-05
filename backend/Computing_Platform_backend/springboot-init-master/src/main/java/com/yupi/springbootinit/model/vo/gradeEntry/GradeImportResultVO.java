package com.yupi.springbootinit.model.vo.gradeEntry;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 成绩导入结果VO
 *
 * @author YU
 */
@Data
public class GradeImportResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 导入的学生数量
     */
    private Integer studentCount;

    /**
     * 导入的成绩记录数量
     */
    private Integer scoreCount;

    /**
     * 错误信息列表
     */
    private List<String> errorMessages;

    /**
     * 警告信息列表
     */
    private List<String> warningMessages;
}
