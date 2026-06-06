package com.yupi.springbootinit.model.vo.report;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 学生得分层级信息VO（穿透式台账第五层）
 *
 * @author YU
 */
@Data
public class StudentScoreAccount implements Serializable {

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
     * 课程编号
     */
    private String courseCode;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 考核点编号
     */
    private String assessmentPointCode;

    /**
     * 考核点名称
     */
    private String assessmentPointName;

    /**
     * 满分
     */
    private BigDecimal fullScore;

    /**
     * 得分
     */
    private BigDecimal score;

    /**
     * 达成度
     */
    private BigDecimal achievement;
}