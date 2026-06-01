package com.yupi.springbootinit.controller;

import com.yupi.springbootinit.annotation.AuthCheck;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.constant.SysUserConstant;
import com.yupi.springbootinit.model.dto.weight.AvailableIndicatorRequest;
import com.yupi.springbootinit.model.dto.weight.WeightObjectiveIndicatorCheckRequest;
import com.yupi.springbootinit.model.dto.weight.WeightObjectiveIndicatorSaveRequest;
import com.yupi.springbootinit.model.vo.IndicatorPointVO;
import com.yupi.springbootinit.model.vo.WeightCheckVO;
import com.yupi.springbootinit.model.vo.WeightObjectiveIndicatorVO;
import com.yupi.springbootinit.service.WeightObjectiveIndicatorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

// 课程目标-指标点内部权重接口

@RestController
@RequestMapping("/weight/objective-indicator")
@Slf4j
public class WeightObjectiveIndicatorController {

    @Resource
    private WeightObjectiveIndicatorService weightObjectiveIndicatorService;

    /**
     * 获取指定课程可配置的指标点
     *
     * @param request 查询请求
     * @return 指标点列表
     */
    @PostMapping("/available")
    @AuthCheck(mustRole = SysUserConstant.ROLE_TEACHER)
    public BaseResponse<List<IndicatorPointVO>> listAvailableIndicators(@RequestBody AvailableIndicatorRequest request) {
        return ResultUtils.success(weightObjectiveIndicatorService.listAvailableIndicators(request.getCourseId()));
    }

    /**
     * 保存课程内部权重配置
     *
     * @param request 保存请求
     * @return 是否成功
     */
    @PostMapping("/save")
    @AuthCheck(mustRole = SysUserConstant.ROLE_TEACHER)
    public BaseResponse<Boolean> saveWeights(@RequestBody WeightObjectiveIndicatorSaveRequest request) {
        return ResultUtils.success(weightObjectiveIndicatorService.saveWeights(request));
    }

    /**
     * 获取指定课程的内部权重配置
     *
     * @param request 查询请求
     * @return 内部权重列表
     */
    @PostMapping("/list")
    @AuthCheck(mustRole = SysUserConstant.ROLE_TEACHER)
    public BaseResponse<List<WeightObjectiveIndicatorVO>> listWeights(@RequestBody AvailableIndicatorRequest request) {
        return ResultUtils.success(weightObjectiveIndicatorService.listWeights(request.getCourseId()));
    }

    /**
     * 校验内部权重合计是否为1
     *
     * @param request 校验请求
     * @return 校验结果
     */
    @PostMapping("/check")
    @AuthCheck(mustRole = SysUserConstant.ROLE_TEACHER)
    public BaseResponse<WeightCheckVO> checkWeights(@RequestBody WeightObjectiveIndicatorCheckRequest request) {
        return ResultUtils.success(weightObjectiveIndicatorService.checkWeights(request));
    }
}
