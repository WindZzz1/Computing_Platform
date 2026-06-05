package com.yupi.springbootinit.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.springbootinit.annotation.AuthCheck;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.constant.SysUserConstant;
import com.yupi.springbootinit.model.dto.majorCalculation.MajorCalculationRequest;
import com.yupi.springbootinit.model.dto.majorCalculation.MajorDashboardQueryRequest;
import com.yupi.springbootinit.model.vo.majorCalculation.CourseCalculationStatusVO;
import com.yupi.springbootinit.model.vo.majorCalculation.MajorCalculationResultVO;
import com.yupi.springbootinit.service.MajorCalculationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 专业级达成度计算接口
 *
 * @author YU
 */
@RestController
@RequestMapping("/major-calculation")
@Slf4j
public class MajorCalculationController {

    @Resource
    private MajorCalculationService majorCalculationService;

    /**
     * 获取课程计算状态监控看板
     *
     * @param request 查询请求
     * @return 监控看板数据
     */
    @PostMapping("/dashboard")
    @AuthCheck(anyRole = SysUserConstant.ROLE_LEADER + "," + SysUserConstant.ROLE_EDU)
    public BaseResponse<MajorCalculationResultVO> getDashboardOverview(@RequestBody MajorDashboardQueryRequest request) {
        MajorCalculationResultVO result = majorCalculationService.getDashboardOverview(request);
        return ResultUtils.success(result);
    }

    /**
     * 分页查询课程计算状态
     *
     * @param request 查询请求
     * @return 课程计算状态分页
     */
    @PostMapping("/course-status")
    @AuthCheck(anyRole = SysUserConstant.ROLE_LEADER + "," + SysUserConstant.ROLE_EDU)
    public BaseResponse<Page<CourseCalculationStatusVO>> getCourseCalculationStatus(@RequestBody MajorDashboardQueryRequest request) {
        Page<CourseCalculationStatusVO> page = majorCalculationService.getCourseCalculationStatus(request);
        return ResultUtils.success(page);
    }

    /**
     * 执行专业级达成度计算
     *
     * @param request 计算请求
     * @return 计算结果
     */
    @PostMapping("/calculate")
    @AuthCheck(anyRole = SysUserConstant.ROLE_LEADER + "," + SysUserConstant.ROLE_EDU)
    public BaseResponse<MajorCalculationResultVO> calculateMajorAchievement(@RequestBody MajorCalculationRequest request) {
        MajorCalculationResultVO result = majorCalculationService.calculateMajorAchievement(request);
        return ResultUtils.success(result);
    }

    /**
     * 查询专业级计算结果
     *
     * @param request 查询请求
     * @return 计算结果
     */
    @PostMapping("/result")
    @AuthCheck(anyRole = SysUserConstant.ROLE_LEADER + "," + SysUserConstant.ROLE_EDU)
    public BaseResponse<MajorCalculationResultVO> getMajorCalculationResult(@RequestBody MajorCalculationRequest request) {
        MajorCalculationResultVO result = majorCalculationService.getMajorCalculationResult(request);
        return ResultUtils.success(result);
    }

    /**
     * 删除专业级计算结果
     *
     * @param request 删除请求
     * @return 是否成功
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = SysUserConstant.ROLE_ADMIN)
    public BaseResponse<Boolean> deleteMajorCalculationResult(@RequestBody MajorCalculationRequest request) {
        Boolean result = majorCalculationService.deleteMajorCalculationResult(request);
        return ResultUtils.success(result);
    }
}