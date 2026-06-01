package com.yupi.springbootinit.model.dto.matrix;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 宏观支撑矩阵保存请求
 *
 * @author YU
 */
@Data
public class MatrixCourseIndicatorSaveRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 专业ID
     */
    @NotNull(message = "专业ID不能为空")
    private Long majorId;

    /**
     * 支撑关系数据列表
     * 每个关系包含：课程ID、指标点ID、权重
     */
    private List<MatrixItem> matrixItems;

    /**
     * 矩阵项
     */
    @Data
    public static class MatrixItem implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 课程ID
         */
        @NotNull(message = "课程ID不能为空")
        private Long courseId;

        /**
         * 指标点ID
         */
        @NotNull(message = "指标点ID不能为空")
        private Long indicatorId;

        /**
         * 总支撑权重 Wc (0-1)
         */
        private BigDecimal totalWeight;
    }
}
