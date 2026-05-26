package com.yupi.springbootinit.model.dto.teachingClass;

import lombok.Data;

import java.io.Serializable;

/**
 * 教学班级更新请求
 *
 * @author YU
 */
@Data
public class TeachingClassUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

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
     * 学年ID
     */
    private Long schoolYearId;

    /**
     * 学期（1/2）
     */
    private Integer semester;

    /**
     * 所属专业ID
     */
    private Long majorId;
}