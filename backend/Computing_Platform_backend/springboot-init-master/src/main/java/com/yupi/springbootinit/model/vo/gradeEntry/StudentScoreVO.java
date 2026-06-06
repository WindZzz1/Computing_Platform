package com.yupi.springbootinit.model.vo.gradeEntry;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 学生成绩VO
 *
 * @author YU
 */
@Data
public class StudentScoreVO implements Serializable {

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
     * 得分
     */
    private BigDecimal score;

    /**
     * 满分
     */
    private BigDecimal fullScore;
}
