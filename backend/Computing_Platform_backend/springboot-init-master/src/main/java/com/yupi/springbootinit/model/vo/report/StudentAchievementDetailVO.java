package com.yupi.springbootinit.model.vo.report;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

/**
 * 学生达成度明细VO
 *
 * @author YU
 */
@Data
public class StudentAchievementDetailVO implements Serializable {

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