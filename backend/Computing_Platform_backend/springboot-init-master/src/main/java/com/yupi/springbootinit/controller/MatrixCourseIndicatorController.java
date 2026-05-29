package com.yupi.springbootinit.controller;

import com.yupi.springbootinit.annotation.AuthCheck;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.constant.SysUserConstant;
import com.yupi.springbootinit.model.dto.matrix.MatrixCourseIndicatorSaveRequest;
import com.yupi.springbootinit.model.vo.MatrixConfigVO;
import com.yupi.springbootinit.service.MatrixCourseIndicatorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 宏观支撑矩阵接口
 * 专业负责人配置课程与毕业要求指标点的支撑关系和权重
 *
 * @author YU
 */
@RestController
@RequestMapping("/matrix")
@Slf4j
public class MatrixCourseIndicatorController {

    @Resource
    private MatrixCourseIndicatorService matrixCourseIndicatorService;

    /**
     * 根据专业ID获取矩阵配置
     * 包含课程列表、指标点列表和已有的矩阵数据
     *
     * @param majorId 专业ID
     * @return 矩阵配置
     */
    @GetMapping("/config/{majorId}")
    @AuthCheck(mustRole = SysUserConstant.ROLE_EDU)
    public BaseResponse<MatrixConfigVO> getMatrixConfig(@PathVariable Long majorId) {
        MatrixConfigVO config = matrixCourseIndicatorService.getMatrixConfigByMajorId(majorId);
        return ResultUtils.success(config);
    }

    /**
     * 保存矩阵配置
     * 包含权重校验：每个指标点列的权重总和必须为1.0
     *
     * @param saveRequest 保存请求
     * @return 是否成功
     */
    @PostMapping("/save")
    @AuthCheck(mustRole = SysUserConstant.ROLE_EDU)
    public BaseResponse<Boolean> saveMatrixConfig(@RequestBody MatrixCourseIndicatorSaveRequest saveRequest) {
        log.info("保存矩阵配置，专业ID: {}, 矩阵项数量: {}",
                saveRequest.getMajorId(),
                saveRequest.getMatrixItems() != null ? saveRequest.getMatrixItems().size() : 0);

        Boolean result = matrixCourseIndicatorService.saveMatrixConfig(saveRequest);
        return ResultUtils.success(result);
    }

    /**
     * 校验矩阵权重配置
     * 用于前端实时校验，返回每个指标点的权重总和
     *
     * @param saveRequest 保存请求
     * @return 校验结果
     */
    @PostMapping("/check")
    @AuthCheck(mustRole = SysUserConstant.ROLE_EDU)
    public BaseResponse<WeightCheckResultVO> checkMatrixWeights(@RequestBody MatrixCourseIndicatorSaveRequest saveRequest) {
        MatrixCourseIndicatorService.WeightCheckResult result =
                matrixCourseIndicatorService.checkMatrixWeights(saveRequest);

        WeightCheckResultVO vo = new WeightCheckResultVO();
        vo.setValid(result.isValid());
        vo.setMessage(result.getMessage());
        vo.setColumnSums(result.getColumnSums());

        return ResultUtils.success(vo);
    }

    /**
     * 权重校验结果VO
     */
    @lombok.Data
    public static class WeightCheckResultVO {
        /**
         * 是否通过校验
         */
        private Boolean valid;

        /**
         * 校验消息
         */
        private String message;

        /**
         * 每个指标点的权重总和
         * key: 指标点ID, value: 权重总和
         */
        private java.util.Map<Long, java.math.BigDecimal> columnSums;
    }
}
