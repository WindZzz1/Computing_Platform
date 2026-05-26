package com.yupi.springbootinit.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

// 课程目标-指标点内部权重视图对象

@Data
public class WeightObjectiveIndicatorVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 内部权重ID
     */
    private Long id;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 课程目标ID
     */
    private Long objectiveId;

    /**
     * 课程目标编号
     */
    private String objCode;

    /**
     * 课程目标名称
     */
    private String objName;

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
     * 内部贡献权重 wjk
     */
    private BigDecimal innerWeight;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
