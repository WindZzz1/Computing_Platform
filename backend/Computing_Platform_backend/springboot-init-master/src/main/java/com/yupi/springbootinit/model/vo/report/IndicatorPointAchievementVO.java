package com.yupi.springbootinit.model.vo.report;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 指标点达成度VO
 *
 * @author YU
 */
@Data
public class IndicatorPointAchievementVO implements Serializable {

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
     * 达成度值
     */
    private BigDecimal achievement;

    /**
     * 毕业要求ID
     */
    private Long requirementId;

    /**
     * 毕业要求编号
     */
    private String requirementCode;

    /**
     * 毕业要求名称
     */
    private String requirementName;
}