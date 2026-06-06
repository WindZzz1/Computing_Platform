package com.yupi.springbootinit.model.vo.report;

import lombok.Data;

import java.io.Serializable;

/**
 * 专业层级信息VO（穿透式台账第一层）
 *
 * @author YU
 */
@Data
public class MajorAccountInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 专业ID
     */
    private Long majorId;

    /**
     * 专业名称
     */
    private String majorName;

    /**
     * 专业代码
     */
    private String majorCode;

    /**
     * 学年学期ID
     */
    private Long termId;

    /**
     * 学年名称
     */
    private String yearName;

    /**
     * 学期名称
     */
    private String semesterName;

    /**
     * 年级
     */
    private String grade;

    /**
     * 涉及课程总数
     */
    private Integer totalCourses;

    /**
     * 学生总数
     */
    private Integer totalStudents;

    /**
     * 整体达成度
     */
    private Double overallAchievement;
}