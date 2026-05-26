package com.yupi.springbootinit.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.springbootinit.annotation.AuthCheck;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.DeleteRequest;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.constant.SysUserConstant;
import com.yupi.springbootinit.model.dto.indicator.IndicatorPointAddRequest;
import com.yupi.springbootinit.model.dto.indicator.IndicatorPointQueryRequest;
import com.yupi.springbootinit.model.dto.indicator.IndicatorPointUpdateRequest;
import com.yupi.springbootinit.model.vo.IndicatorPointVO;
import com.yupi.springbootinit.model.vo.PageResultVO;
import com.yupi.springbootinit.service.IndicatorPointService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 二级指标点接口
 *
 * @author YU
 */
@RestController
@RequestMapping("/requirement/indicator")
@Slf4j
public class IndicatorPointController {

    @Resource
    private IndicatorPointService indicatorPointService;

    /**
     * 创建二级指标点
     *
     * @param indicatorPointAddRequest 新增请求
     * @return 二级指标点ID
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = SysUserConstant.ROLE_LEADER)
    public BaseResponse<Long> addIndicatorPoint(@RequestBody IndicatorPointAddRequest indicatorPointAddRequest) {
        Long indicatorPointId = indicatorPointService.createIndicatorPoint(indicatorPointAddRequest);
        return ResultUtils.success(indicatorPointId);
    }

    /**
     * 更新二级指标点
     *
     * @param indicatorPointUpdateRequest 更新请求
     * @return 是否成功
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = SysUserConstant.ROLE_LEADER)
    public BaseResponse<Boolean> updateIndicatorPoint(@RequestBody IndicatorPointUpdateRequest indicatorPointUpdateRequest) {
        Boolean result = indicatorPointService.updateIndicatorPoint(indicatorPointUpdateRequest);
        return ResultUtils.success(result);
    }

    /**
     * 删除二级指标点
     *
     * @param deleteRequest 删除请求
     * @return 是否成功
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = SysUserConstant.ROLE_LEADER)
    public BaseResponse<Boolean> deleteIndicatorPoint(@RequestBody DeleteRequest deleteRequest) {
        Boolean result = indicatorPointService.deleteIndicatorPoint(deleteRequest.getId());
        return ResultUtils.success(result);
    }

    /**
     * 根据ID获取二级指标点
     *
     * @param deleteRequest ID请求
     * @return 二级指标点信息
     */
    @PostMapping("/get")
    @AuthCheck(mustRole = SysUserConstant.ROLE_LEADER)
    public BaseResponse<IndicatorPointVO> getIndicatorPointById(@RequestBody DeleteRequest deleteRequest) {
        IndicatorPointVO indicatorPointVO = indicatorPointService.getIndicatorPointById(deleteRequest.getId());
        return ResultUtils.success(indicatorPointVO);
    }

    /**
     * 分页查询二级指标点
     *
     * @param indicatorPointQueryRequest 查询请求
     * @return 分页结果
     */
    @PostMapping("/page")
    @AuthCheck(mustRole = SysUserConstant.ROLE_LEADER)
    public BaseResponse<PageResultVO<IndicatorPointVO>> pageIndicatorPoint(@RequestBody IndicatorPointQueryRequest indicatorPointQueryRequest) {
        Page<IndicatorPointVO> indicatorPointPage = indicatorPointService.pageIndicatorPoint(indicatorPointQueryRequest);
        return ResultUtils.success(PageResultVO.from(indicatorPointPage));
    }
}