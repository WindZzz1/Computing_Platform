package com.yupi.springbootinit.model.vo.report;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 考核点层级信息VO（穿透式台账第四层）
 *
 * @author YU
 */
@Data
public class AssessmentPointAccount implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 考核点ID
     */
    private Long pointId;

    /**
     * 考核点编号
     */
    private String pointCode;

    /**
     * 考核点名称
     */
    private String pointName;

    /**
     * 满分
     */
    private BigDecimal fullScore;

    /**
     * 班级平均分
     */
    private BigDecimal classAverageScore;

    /**
     * 关联的课程目标ID
     */
    private Long objectiveId;

    /**
     * 考核点编号（前端契约字段，等同 pointCode）
     */
    private String assessmentPointCode;

    /**
     * 考核点名称（前端契约字段，等同 pointName）
     */
    private String assessmentPointName;

    /**
     * 所属课程名称
     */
    private String courseName;

    /**
     * 关联的课程目标编号（来自 course_objective.obj_code）
     */
    private String objectiveCode;

    /**
     * 考核点对该课程目标的支撑权重（来自 rel_point_objective.weight）
     */
    private BigDecimal weight;

    /**
     * 学生成绩列表
     */
    private List<StudentScoreAccount> studentScores;
}