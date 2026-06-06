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
     * 学生成绩列表
     */
    private List<StudentScoreAccount> studentScores;
}