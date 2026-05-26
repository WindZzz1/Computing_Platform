package com.yupi.springbootinit.model.dto.course;

import lombok.Data;

import java.io.Serializable;

/**
 * 课程更新请求
 *
 * @author YU
 */
@Data
public class CourseUpdateRequest implements Serializable {

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

    /**
     * 课程性质（必修/选修）
     */
    private String courseNature;

    /**
     * 学分
     */
    private Double credit;

    /**
     * 所属专业ID
     */
    private Long majorId;
}