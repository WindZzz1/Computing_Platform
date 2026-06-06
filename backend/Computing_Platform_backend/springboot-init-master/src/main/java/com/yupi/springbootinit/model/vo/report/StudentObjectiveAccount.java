package com.yupi.springbootinit.model.vo.report;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

/**
 * 学生课程目标达成度VO（穿透式台账第三层数据）
 *
 * @author YU
 */
@Data
public class StudentObjectiveAccount implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 学生ID
     */
    private Long studentId;

    /**
     * 学号
     */
    private String studentNo;

    /**
     * 学生姓名
     */
    private String studentName;

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
     * 课程目标达成度映射
     * Key: objectiveCode (课程目标编号)
     * Value: achievement (达成度)
     */
    private Map<String, BigDecimal> objectiveAchievements;

    /**
     * 平均达成度
     */
    private BigDecimal averageAchievement;
}