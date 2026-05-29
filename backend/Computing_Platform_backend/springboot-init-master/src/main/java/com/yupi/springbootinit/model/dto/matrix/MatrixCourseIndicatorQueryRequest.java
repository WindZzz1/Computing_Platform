package com.yupi.springbootinit.model.dto.matrix;

import com.yupi.springbootinit.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 宏观支撑矩阵查询请求
 *
 * @author YU
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MatrixCourseIndicatorQueryRequest extends PageRequest {

    /**
     * 专业ID
     */
    private Long majorId;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 指标点ID
     */
    private Long indicatorId;
}
