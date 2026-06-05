package com.yupi.springbootinit.model.vo.majorCalculation;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 课程计算状态VO
 *
 * @author YU
 */
@Data
public class CourseCalculationStatusVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 教学班级ID
     */
    private Long classId;

    /**
     * 班级名称
     */
    private String className;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 课程代码
     */
    private String courseCode;

    /**
     * 主讲教师ID
     */
    private Long teacherId;

    /**
     * 主讲教师姓名
     */
    private String teacherName;

    /**
     * 计算状态：0-未计算，1-计算中，2-计算完成，3-计算失败
     */
    private Integer calcStatus;

    /**
     * 是否已锁定
     */
    private Boolean isLocked;

    /**
     * 学生人数
     */
    private Integer studentCount;

    /**
     * 计算完成时间
     */
    private Date calcEndTime;

    /**
     * 锁定时间
     */
    private Date lockTime;

    /**
     * 状态描述
     */
    private String statusDescription;
}