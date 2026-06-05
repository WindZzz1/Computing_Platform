package com.yupi.springbootinit.controller;

import com.yupi.springbootinit.annotation.AuthCheck;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.constant.SysUserConstant;
import com.yupi.springbootinit.model.dto.gradeEntry.AchievementCalculationRequest;
import com.yupi.springbootinit.model.vo.gradeEntry.AchievementCalculationResultVO;
import com.yupi.springbootinit.service.AchievementCalculationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 达成度计算接口
 *
 * @author YU
 */
@RestController
@RequestMapping("/achievement-calculation")
@Slf4j
public class AchievementCalculationController {

    @Resource
    private AchievementCalculationService achievementCalculationService;

    /**
     * 触发达成度计算
     *
     * @param request 计算请求
     * @return 计算结果
     */
    @PostMapping("/calculate")
    @AuthCheck(mustRole = SysUserConstant.ROLE_TEACHER)
    public BaseResponse<AchievementCalculationResultVO> calculateAchievement(@RequestBody AchievementCalculationRequest request) {
        AchievementCalculationResultVO result = achievementCalculationService.calculateAchievement(request);
        return ResultUtils.success(result);
    }

    /**
     * 查询计算状态
     *
     * @param request 查询请求
     * @return 计算状态
     */
    @PostMapping("/status")
    @AuthCheck(mustRole = SysUserConstant.ROLE_TEACHER)
    public BaseResponse<AchievementCalculationResultVO> getCalculationStatus(@RequestBody AchievementCalculationRequest request) {
        if (request == null || request.getClassId() == null) {
            throw new RuntimeException("教学班级ID不能为空");
        }
        AchievementCalculationResultVO result = achievementCalculationService.getCalculationStatus(request.getClassId());
        return ResultUtils.success(result);
    }

    /**
     * 解锁成绩（仅管理员可操作）
     *
     * @param request 解锁请求
     * @return 是否成功
     */
    @PostMapping("/unlock")
    @AuthCheck(mustRole = SysUserConstant.ROLE_ADMIN)
    public BaseResponse<Boolean> unlockGrades(@RequestBody AchievementCalculationRequest request) {
        String reason = request.getForceRecalculate() != null && request.getForceRecalculate() ?
                "管理员强制解锁" : "手动解锁";
        Boolean result = achievementCalculationService.unlockGrades(request.getClassId(), reason);
        return ResultUtils.success(result);
    }
}
