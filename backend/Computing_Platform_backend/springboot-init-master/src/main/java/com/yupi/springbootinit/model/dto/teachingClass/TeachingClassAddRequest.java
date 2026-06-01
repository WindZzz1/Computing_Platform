package com.yupi.springbootinit.model.dto.teachingClass;

import lombok.Data;

import java.io.Serializable;

/**
 * 教学班级新增请求
 *
 * @author YU
 */
@Data
public class TeachingClassAddRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 班级名称
     */
    private String className;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 主讲教师ID
     */
    private Long teacherId;

    /**
     * 学年学期ID
     */
    private Long termId;
}