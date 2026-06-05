package com.yupi.springbootinit.model.vo.gradeEntry;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 学生课程目标达成度VO（一级达成度）
 *
 * @author YU
 */
@Data
public class StudentObjectiveAchievementVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 成绩ID
     */
    private Long id;

    /**
     * 学生ID
     */
    private Long studentId;

    /**
     * 学号
     */
    private String studentNo;

    /**
     * 姓名
     */
    private String name;

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
     * 一级达成度值
     */
    private BigDecimal achievement;

    /**
     * 计算时间
     */
    private Date calculateTime;
}
