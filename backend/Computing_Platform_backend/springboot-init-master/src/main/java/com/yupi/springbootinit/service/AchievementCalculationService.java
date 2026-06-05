package com.yupi.springbootinit.service;

import com.yupi.springbootinit.model.dto.gradeEntry.AchievementCalculationRequest;
import com.yupi.springbootinit.model.vo.gradeEntry.AchievementCalculationResultVO;

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
    AchievementCalculationResultVO calculateAchievement(AchievementCalculationRequest request);

    /**
     * 查询计算状态
     *
     * @param classId 教学班级ID
     * @return 计算状态
     */
    AchievementCalculationResultVO getCalculationStatus(Long classId);

    /**
     * 解锁成绩（仅管理员或强制情况）
     *
     * @param classId 教学班级ID
     * @param reason 解锁原因
     * @return 是否成功
     */
    Boolean unlockGrades(Long classId, String reason);
}
