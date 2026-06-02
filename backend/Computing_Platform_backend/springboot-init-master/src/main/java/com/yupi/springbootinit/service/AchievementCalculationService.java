package com.yupi.springbootinit.service;

import com.yupi.springbootinit.model.vo.AchievementCalculationResultVO;

/**
 * 达成度计算服务
 *
 * @author YU
 */
public interface AchievementCalculationService {

    /**
     * 一键计算课程达成度
     * 包括：一级达成度（学生课程目标达成度）和二级达成度（课程指标点达成度）
     * 计算完成后自动锁定成绩
     *
     * @param classId 教学班级ID
     * @return 计算结果
     */
    AchievementCalculationResultVO calculateAchievements(Long classId);

    /**
     * 锁定成绩
     * 锁定后无法修改成绩和课程配置
     *
     * @param classId 教学班级ID
     * @return 是否成功
     */
    Boolean lockScores(Long classId);

    /**
     * 解锁成绩
     * 只有教务管理员可以解锁
     *
     * @param classId 教学班级ID
     * @return 是否成功
     */
    Boolean unlockScores(Long classId);

    /**
     * 获取计算状态
     *
     * @param classId 教学班级ID
     * @return 计算结果（如果已计算）
     */
    AchievementCalculationResultVO getCalculationResult(Long classId);

    /**
     * 获取一级达成度（学生课程目标达成度）
     *
     * @param classId 教学班级ID
     * @return 达成度计算结果
     */
    AchievementCalculationResultVO getFirstLevelAchievements(Long classId);

    /**
     * 获取二级达成度（课程指标点达成度）
     *
     * @param classId 教学班级ID
     * @return 达成度计算结果
     */
    AchievementCalculationResultVO getSecondLevelAchievements(Long classId);
}
