package com.yupi.springbootinit.model.dto.gradeEntry;

import lombok.Data;

import java.io.Serializable;

/**
 * 生成成绩录入模板请求
 *
 * @author YU
 */
@Data
public class GradeEntryTemplateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 教学班级ID
     */
    private Long classId;
}
