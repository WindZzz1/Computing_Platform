package com.yupi.springbootinit.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 达成度计算结果VO
 *
 * @author YU
 */
@Data
public class AchievementCalculationResultVO implements Serializable {

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
     * 课程名称
     */
    private String courseName;

    /**
     * 计算时间
     */
    private Date calculateTime;

    /**
     * 是否计算成功
     */
    private Boolean success;

    /**
     * 计算状态：0-未计算，1-已计算
     */
    private Integer calculatedStatus;

    /**
     * 锁定状态：0-未锁定，1-已锁定
     */
    private Integer lockedStatus;

    /**
     * 班级学生人数
     */
    private Integer studentCount;

    /**
     * 一级达成度列表（学生课程目标达成度）
     */
    private List<StudentObjectiveAchievementVO> firstLevelAchievements;

    /**
     * 二级达成度列表（课程指标点达成度）
     */
    private List<CourseIndicatorAchievementVO> secondLevelAchievements;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 学生课程目标达成度VO（内部类）
     */
    @Data
    public static class StudentObjectiveAchievementVO implements Serializable {
        private static final long serialVersionUID = 1L;

        private Long studentId;
        private String studentNo;
        private String studentName;
        private Long objectiveId;
        private String objectiveCode;
        private String objectiveName;
        private BigDecimal achievementValue;
    }

    /**
     * 课程指标点达成度VO（内部类）
     */
    @Data
    public static class CourseIndicatorAchievementVO implements Serializable {
        private static final long serialVersionUID = 1L;

        private Long indicatorId;
        private String indicatorCode;
        private String indicatorName;
        private BigDecimal achievementValue;
        private Integer studentCount;
    }
}
