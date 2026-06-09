package com.yupi.springbootinit.service;

import com.yupi.springbootinit.model.dto.gradeEntry.AchievementCalculationRequest;

import java.util.Map;

/**
 * 达成度计算服务
 *
 * @author YU
 */
public interface AchievementCalculationService {

    /**
     * 触发达成度计算
     *
     * @param request 计算请求
     * @return 计算结果
     */
    Map<String, Object> calculateAchievement(AchievementCalculationRequest request);

    /**
     * 查询计算状态
     *
     * @param classId 教学班级ID
     * @return 计算状态和统计信息
     */
    Map<String, Object> getCalculationStatus(Long classId);

    /**
     * 查询课程级计算明细
     *
     * @param classId 教学班级ID
     * @return 一级、二级达成度明细
     */
    Map<String, Object> getCalculationDetail(Long classId);
}
