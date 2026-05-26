package com.yupi.springbootinit.model.dto.course;

import lombok.Data;

import java.io.Serializable;

// 课程目标新增请求

@Data
public class CourseObjectiveAddRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 课程目标编号，例如 CO1、CO2
     */
    private String objCode;

    /**
     * 课程目标名称
     */
    private String objName;

    /**
     * 课程目标描述
     */
    private String objDesc;
}
