package com.yupi.springbootinit.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 宏观支撑矩阵配置视图对象
 * 包含课程列表、指标点列表和矩阵数据
 *
 * @author YU
 */
@Data
public class MatrixConfigVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 专业ID
     */
    private Long majorId;

    /**
     * 专业名称
     */
    private String majorName;

    /**
     * 课程列表
     */
    private List<CourseSimpleVO> courses;

    /**
     * 指标点列表
     */
    private List<IndicatorPointSimpleVO> indicators;

    /**
     * 矩阵数据
     */
    private List<MatrixCourseIndicatorVO> matrixData;

    /**
     * 每个指标点的权重总和
     * key: 指标点ID, value: 权重总和
     */
    private java.util.Map<Long, java.math.BigDecimal> columnSums;

    /**
     * 简单课程信息
     */
    @Data
    public static class CourseSimpleVO implements Serializable {

        private static final long serialVersionUID = 1L;

        private Long id;
        private String courseCode;
        private String courseName;
        private Double credit;
    }

    /**
     * 简单指标点信息
     */
    @Data
    public static class IndicatorPointSimpleVO implements Serializable {

        private static final long serialVersionUID = 1L;

        private Long id;
        private String indicatorCode;
        private String indicatorName;
        private Long requirementId;
    }
}
