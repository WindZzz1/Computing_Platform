package com.yupi.springbootinit.model.dto.weight;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

// 课程目标-指标点内部权重校验请求

@Data
public class WeightObjectiveIndicatorCheckRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 内部权重明细列表
     */
    private List<Item> weightList;

    /**
     * 内部权重明细项
     */
    @Data
    public static class Item implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 课程目标ID
         */
        private Long objectiveId;

        /**
         * 指标点ID
         */
        private Long indicatorId;

        /**
         * 内部贡献权重 wjk
         */
        private BigDecimal innerWeight;
    }
}
