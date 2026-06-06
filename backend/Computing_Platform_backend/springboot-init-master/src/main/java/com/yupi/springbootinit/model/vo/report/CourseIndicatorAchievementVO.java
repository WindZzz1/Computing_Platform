package com.yupi.springbootinit.model.vo.report;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 课程指标点达成度VO
 *
 * @author YU
 */
@Data
public class CourseIndicatorAchievementVO implements Serializable {

    private static final long serialVersionUID = 1L;

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
     * 二级达成度值
     */
    private BigDecimal achievement;

    /**
     * 计算时间
     */
    private String calculationTime;
}