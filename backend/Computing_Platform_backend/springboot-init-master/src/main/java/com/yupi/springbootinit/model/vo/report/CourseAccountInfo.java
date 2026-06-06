package com.yupi.springbootinit.model.vo.report;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 课程层级信息VO（穿透式台账第二层）
 *
 * @author YU
 */
@Data
public class CourseAccountInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 课程编号
     */
    private String courseCode;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 教学班级ID
     */
    private Long classId;

    /**
     * 教学班级名称
     */
    private String className;

    /**
     * 主讲教师姓名
     */
    private String teacherName;

    /**
     * 课程指标点达成度（二级达成度）
     */
    private BigDecimal courseIndicatorAchievement;

    /**
     * 学生人数
     */
    private Integer studentCount;

    /**
     * 课程目标列表
     */
    private List<ObjectiveAccountInfo> objectives;
}