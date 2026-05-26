package com.yupi.springbootinit.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 课程简化视图对象
 *
 * @author YU
 */
@Data
public class CourseSimpleVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 课程代码
     */
    private String courseCode;

    /**
     * 课程名称
     */
    private String courseName;
}