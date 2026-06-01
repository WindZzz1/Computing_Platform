package com.yupi.springbootinit.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 课程视图对象
 *
 * @author YU
 */
@Data
public class CourseVO implements Serializable {

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

    /**
     * 所属专业名称
     */
    private String majorName;

    /**
     * 所属学院ID
     */
    private Long collegeId;

    /**
     * 所属学院名称
     */
    private String collegeName;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}