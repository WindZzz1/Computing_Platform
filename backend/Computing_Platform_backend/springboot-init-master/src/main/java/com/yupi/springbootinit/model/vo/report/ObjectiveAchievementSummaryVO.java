package com.yupi.springbootinit.model.vo.report;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 课程目标达成度汇总VO
 *
 * @author YU
 */
@Data
public class ObjectiveAchievementSummaryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 课程目标ID
     */
    private Long objectiveId;

    /**
     * 课程目标编号
     */
    private String objectiveCode;

    /**
     * 课程目标名称
     */
    private String objectiveName;

    /**
     * 班级平均分
     */
    private BigDecimal classAverage;

    /**
     * 最高分
     */
    private BigDecimal maxScore;

    /**
     * 最低分
     */
    private BigDecimal minScore;

    /**
     * 及格率（达成度>=0.7的学生比例）
     */
    private BigDecimal passRate;

    /**
     * 学生总数
     */
    private Integer studentCount;
}