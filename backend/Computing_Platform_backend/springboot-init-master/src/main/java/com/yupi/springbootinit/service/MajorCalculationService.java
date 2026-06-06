package com.yupi.springbootinit.service;

import com.yupi.springbootinit.model.dto.majorCalculation.MajorCalculationRequest;
import com.yupi.springbootinit.model.dto.majorCalculation.MajorDashboardQueryRequest;

import java.util.Map;

/**
 * 专业级达成度计算服务
 *
 * @author YU
 */
public interface MajorCalculationService {

    /**
     * 获取课程计算状态监控看板
     *
     * @param request 查询请求
     * @return 监控看板数据
     */
    Map<String, Object> getDashboardOverview(MajorDashboardQueryRequest request);

    /**
     * 执行专业级达成度计算
     *
     * @param request 计算请求
     * @return 计算结果
     */
    Map<String, Object> calculateMajorAchievement(MajorCalculationRequest request);

    /**
     * 查询专业级计算结果
     *
     * @param request 查询请求
     * @return 计算结果
     */
    Map<String, Object> getMajorCalculationResult(MajorCalculationRequest request);

    /**
     * 删除专业级计算结果
     *
     * @param request 删除请求
     * @return 是否成功
     */
    Boolean deleteMajorCalculationResult(MajorCalculationRequest request);
}
