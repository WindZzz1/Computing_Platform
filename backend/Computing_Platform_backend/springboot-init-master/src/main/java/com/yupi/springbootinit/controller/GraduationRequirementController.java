package com.yupi.springbootinit.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.springbootinit.annotation.AuthCheck;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.DeleteRequest;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.constant.SysUserConstant;
import com.yupi.springbootinit.model.dto.requirement.GraduationRequirementAddRequest;
import com.yupi.springbootinit.model.dto.requirement.GraduationRequirementQueryRequest;
import com.yupi.springbootinit.model.dto.requirement.GraduationRequirementUpdateRequest;
import com.yupi.springbootinit.model.vo.GraduationRequirementVO;
import com.yupi.springbootinit.model.vo.PageResultVO;
import com.yupi.springbootinit.service.GraduationRequirementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 毕业要求接口
 *
 * @author YU
 */
@RestController
@RequestMapping("/requirement/graduation")
@Slf4j
public class GraduationRequirementController {

    @Resource
    private GraduationRequirementService graduationRequirementService;

    /**
     * 创建毕业要求
     *
     * @param graduationRequirementAddRequest 新增请求
     * @return 毕业要求ID
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = SysUserConstant.ROLE_LEADER)
    public BaseResponse<Long> addRequirement(@RequestBody GraduationRequirementAddRequest graduationRequirementAddRequest) {
        Long requirementId = graduationRequirementService.createRequirement(graduationRequirementAddRequest);
        return ResultUtils.success(requirementId);
    }

    /**
     * 更新毕业要求
     *
     * @param graduationRequirementUpdateRequest 更新请求
     * @return 是否成功
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = SysUserConstant.ROLE_LEADER)
    public BaseResponse<Boolean> updateRequirement(@RequestBody GraduationRequirementUpdateRequest graduationRequirementUpdateRequest) {
        Boolean result = graduationRequirementService.updateRequirement(graduationRequirementUpdateRequest);
        return ResultUtils.success(result);
    }

    /**
     * 删除毕业要求
     *
     * @param deleteRequest 删除请求
     * @return 是否成功
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = SysUserConstant.ROLE_LEADER)
    public BaseResponse<Boolean> deleteRequirement(@RequestBody DeleteRequest deleteRequest) {
        Boolean result = graduationRequirementService.deleteRequirement(deleteRequest.getId());
        return ResultUtils.success(result);
    }

    /**
     * 根据ID获取毕业要求
     *
     * @param deleteRequest ID请求
     * @return 毕业要求信息
     */
    @PostMapping("/get")
    @AuthCheck(mustRole = SysUserConstant.ROLE_LEADER)
    public BaseResponse<GraduationRequirementVO> getRequirementById(@RequestBody DeleteRequest deleteRequest) {
        GraduationRequirementVO requirementVO = graduationRequirementService.getRequirementById(deleteRequest.getId());
        return ResultUtils.success(requirementVO);
    }

    /**
     * 分页查询毕业要求
     *
     * @param graduationRequirementQueryRequest 查询请求
     * @return 分页结果
     */
    @PostMapping("/page")
    @AuthCheck(mustRole = SysUserConstant.ROLE_LEADER)
    public BaseResponse<PageResultVO<GraduationRequirementVO>> pageRequirement(@RequestBody GraduationRequirementQueryRequest graduationRequirementQueryRequest) {
        Page<GraduationRequirementVO> requirementPage = graduationRequirementService.pageRequirement(graduationRequirementQueryRequest);
        return ResultUtils.success(PageResultVO.from(requirementPage));
    }
}