package com.yupi.springbootinit.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

// 课程目标视图对象

@Data
public class CourseObjectiveVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 课程目标ID
     */
    private Long id;

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

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
