package com.yupi.springbootinit.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 学生成绩预览VO
 *
 * @author YU
 */
@Data
public class ScorePreviewVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 成绩记录ID
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
     * 学生姓名
     */
    private String studentName;

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
     * 满分值
     */
    private BigDecimal fullScore;

    /**
     * 实际得分
     */
    private BigDecimal actualScore;

    /**
     * 是否已提交
     */
    private Boolean submitted;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
