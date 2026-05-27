package com.yupi.springbootinit.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

// 内部权重校验结果

@Data
public class WeightCheckVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 权重合计是否校验通过
     */
    private Boolean valid;

    /**
     * 指标点ID与该指标点下内部权重合计的映射
     */
    private Map<Long, BigDecimal> indicatorWeightSumMap;
}
