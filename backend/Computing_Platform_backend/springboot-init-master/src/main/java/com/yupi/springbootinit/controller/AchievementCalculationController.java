package com.yupi.springbootinit.controller;

import com.yupi.springbootinit.annotation.AuthCheck;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.constant.SysUserConstant;
import com.yupi.springbootinit.model.dto.gradeEntry.AchievementCalculationRequest;
import com.yupi.springbootinit.service.AchievementCalculationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

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
    public BaseResponse<Map<String, Object>> calculateAchievement(@RequestBody AchievementCalculationRequest request) {
        Map<String, Object> result = achievementCalculationService.calculateAchievement(request);
        return ResultUtils.success(result);
    }

    /**
     * 查询计算状态
     *
     * @param request 查询请求
     * @return 计算状态
     */
    @PostMapping("/status")
    @AuthCheck(anyRole = SysUserConstant.ROLE_TEACHER + "," + SysUserConstant.ROLE_ADMIN)
    public BaseResponse<Map<String, Object>> getCalculationStatus(@RequestBody AchievementCalculationRequest request) {
        if (request == null || request.getClassId() == null) {
            throw new RuntimeException("教学班级ID不能为空");
        }
        Map<String, Object> result = achievementCalculationService.getCalculationStatus(request.getClassId());
        return ResultUtils.success(result);
    }

    /**
     * 查询课程级计算明细
     *
     * @param request 查询请求
     * @return 一级、二级达成度明细
     */
    @PostMapping("/detail")
    @AuthCheck(anyRole = SysUserConstant.ROLE_TEACHER + "," + SysUserConstant.ROLE_ADMIN)
    public BaseResponse<Map<String, Object>> getCalculationDetail(@RequestBody AchievementCalculationRequest request) {
        if (request == null || request.getClassId() == null) {
            throw new RuntimeException("教学班级ID不能为空");
        }
        Map<String, Object> result = achievementCalculationService.getCalculationDetail(request.getClassId());
        return ResultUtils.success(result);
    }
}
