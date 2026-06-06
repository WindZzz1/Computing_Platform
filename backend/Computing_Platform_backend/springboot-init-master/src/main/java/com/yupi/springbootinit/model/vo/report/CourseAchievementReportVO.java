package com.yupi.springbootinit.model.vo.report;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 课程目标达成情况评价报表VO
 * 包含教学班级基本信息、课程目标列表、学生达成度明细、指标点达成度明细
 *
 * @author YU
 */
@Data
public class CourseAchievementReportVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 教学班级ID
     */
    private Long classId;

    /**
     * 教学班级名称
     */
    private String className;

    /**
     * 课程编号
     */
    private String courseCode;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 主讲教师姓名
     */
    private String teacherName;

    /**
     * 学年名称
     */
    private String yearName;

    /**
     * 学期名称
     */
    private String semesterName;

    /**
     * 学生总数
     */
    private Integer studentCount;

    /**
     * 课程目标达成度汇总
     */
    private List<ObjectiveAchievementSummaryVO> objectiveSummaries;

    /**
     * 学生达成度明细（完整数据）
     */
    private List<StudentAchievementDetailVO> studentDetails;

    /**
     * 课程指标点达成度明细
     */
    private List<CourseIndicatorAchievementVO> indicatorAchievements;

    /**
     * 报表生成时间
     */
    private Date reportGeneratedTime;

    /**
     * 达成度计算时间
     */
    private Date calculationTime;
}