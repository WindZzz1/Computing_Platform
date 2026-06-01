package com.yupi.springbootinit.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 宏观支撑矩阵视图对象
 *
 * @author YU
 */
@Data
public class MatrixCourseIndicatorVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 专业ID
     */
    private Long majorId;

    /**
     * 专业名称
     */
    private String majorName;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 课程代码
     */
    private String courseCode;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 指标点ID
     */
    private Long indicatorId;

    /**
     * 指标点编号
     */
    private String indicatorCode;

    /**
     * 指标点名称
     */
    private String indicatorName;

    /**
     * 宏观总支撑权重 Wc
     */
    private BigDecimal totalWeight;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}