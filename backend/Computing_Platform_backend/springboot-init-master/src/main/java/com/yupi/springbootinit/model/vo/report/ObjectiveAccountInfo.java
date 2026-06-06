package com.yupi.springbootinit.model.vo.report;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 课程目标层级信息VO（穿透式台账第三层）
 *
 * @author YU
 */
@Data
public class ObjectiveAccountInfo implements Serializable {

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
     * 班级平均达成度
     */
    private BigDecimal classAverageAchievement;

    /**
     * 关联的指标点编号
     */
    private String indicatorCode;

    /**
     * 关联的指标点名称
     */
    private String indicatorName;

    /**
     * 宏观支撑权重
     */
    private BigDecimal macroWeight;

    /**
     * 考核点列表
     */
    private List<AssessmentPointAccount> assessmentPoints;
}