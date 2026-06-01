package com.yupi.springbootinit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.springbootinit.model.dto.matrix.MatrixCourseIndicatorQueryRequest;
import com.yupi.springbootinit.model.dto.matrix.MatrixCourseIndicatorSaveRequest;
import com.yupi.springbootinit.model.entity.MatrixCourseIndicator;
import com.yupi.springbootinit.model.vo.MatrixConfigVO;
import com.yupi.springbootinit.model.vo.MatrixCourseIndicatorVO;

import java.math.BigDecimal;

/**
 * 宏观支撑矩阵服务
 *
 * @author YU
 */
public interface MatrixCourseIndicatorService extends IService<MatrixCourseIndicator> {

    /**
     * 根据专业ID获取矩阵配置
     *
     * @param majorId 专业ID
     * @return 矩阵配置VO
     */
    MatrixConfigVO getMatrixConfigByMajorId(Long majorId);

    /**
     * 保存矩阵配置
     * 包含权重校验：每个指标点列的权重总和必须为1.0
     *
     * @param saveRequest 保存请求
     * @return 是否成功
     */
    Boolean saveMatrixConfig(MatrixCourseIndicatorSaveRequest saveRequest);

    /**
     * 校验矩阵权重配置
     * 每个指标点列的权重总和必须为1.0
     *
     * @param saveRequest 保存请求
     * @return 校验结果
     */
    WeightCheckResult checkMatrixWeights(MatrixCourseIndicatorSaveRequest saveRequest);

    /**
     * 权重校验结果
     */
    class WeightCheckResult {
        private boolean valid;
        private String message;
        private java.util.Map<Long, BigDecimal> columnSums;

        public WeightCheckResult(boolean valid, String message, java.util.Map<Long, BigDecimal> columnSums) {
            this.valid = valid;
            this.message = message;
            this.columnSums = columnSums;
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }

        public java.util.Map<Long, BigDecimal> getColumnSums() {
            return columnSums;
        }
    }
}
