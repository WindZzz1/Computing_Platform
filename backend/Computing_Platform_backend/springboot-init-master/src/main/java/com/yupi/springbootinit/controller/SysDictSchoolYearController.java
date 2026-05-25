package com.yupi.springbootinit.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.springbootinit.annotation.AuthCheck;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.DeleteRequest;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.model.dto.dict.SysDictSchoolYearAddRequest;
import com.yupi.springbootinit.model.dto.dict.SysDictSchoolYearQueryRequest;
import com.yupi.springbootinit.model.dto.dict.SysDictSchoolYearUpdateRequest;
import com.yupi.springbootinit.model.vo.PageResultVO;
import com.yupi.springbootinit.model.vo.SysDictSchoolYearVO;
import com.yupi.springbootinit.service.SysDictSchoolYearService;
import com.yupi.springbootinit.constant.SysUserConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 学年学期字典接口
 *
 * @author YU
 */
@RestController
@RequestMapping("/dict/schoolyear")
@Slf4j
public class SysDictSchoolYearController {

    @Resource
    private SysDictSchoolYearService sysDictSchoolYearService;

    /**
     * 创建学年学期
     *
     * @param sysDictSchoolYearAddRequest 新增请求
     * @return 学年学期ID
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = SysUserConstant.ROLE_ADMIN)
    public BaseResponse<Long> addSchoolYear(@RequestBody SysDictSchoolYearAddRequest sysDictSchoolYearAddRequest) {
        Long schoolYearId = sysDictSchoolYearService.createSchoolYear(sysDictSchoolYearAddRequest);
        return ResultUtils.success(schoolYearId);
    }

    /**
     * 更新年学期
     *
     * @param sysDictSchoolYearUpdateRequest 更新请求
     * @return 是否成功
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = SysUserConstant.ROLE_ADMIN)
    public BaseResponse<Boolean> updateSchoolYear(@RequestBody SysDictSchoolYearUpdateRequest sysDictSchoolYearUpdateRequest) {
        Boolean result = sysDictSchoolYearService.updateSchoolYear(sysDictSchoolYearUpdateRequest);
        return ResultUtils.success(result);
    }

    /**
     * 删除学年学期
     *
     * @param deleteRequest 删除请求
     * @return 是否成功
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = SysUserConstant.ROLE_ADMIN)
    public BaseResponse<Boolean> deleteSchoolYear(@RequestBody DeleteRequest deleteRequest) {
        Boolean result = sysDictSchoolYearService.deleteSchoolYear(deleteRequest.getId());
        return ResultUtils.success(result);
    }

    /**
     * 根据ID获取学年学期
     *
     * @param deleteRequest ID请求
     * @return 学年学期信息
     */
    @PostMapping("/get")
    @AuthCheck(mustRole = SysUserConstant.ROLE_ADMIN)
    public BaseResponse<SysDictSchoolYearVO> getSchoolYearById(@RequestBody DeleteRequest deleteRequest) {
        SysDictSchoolYearVO schoolYearVO = sysDictSchoolYearService.getSchoolYearById(deleteRequest.getId());
        return ResultUtils.success(schoolYearVO);
    }

    /**
     * 分页查询学年学期
     *
     * @param sysDictSchoolYearQueryRequest 查询请求
     * @return 分页结果
     */
    @PostMapping("/page")
    @AuthCheck(mustRole = SysUserConstant.ROLE_ADMIN)
    public BaseResponse<PageResultVO<SysDictSchoolYearVO>> pageSchoolYear(@RequestBody SysDictSchoolYearQueryRequest sysDictSchoolYearQueryRequest) {
        Page<SysDictSchoolYearVO> schoolYearPage = sysDictSchoolYearService.pageSchoolYear(sysDictSchoolYearQueryRequest);
        return ResultUtils.success(PageResultVO.from(schoolYearPage));
    }

    /**
     * 获取所有学年学期列表（不分页）
     *
     * @return 学年学期列表
     */
//    @PostMapping("/list")
//    @AuthCheck(mustRole = SysUserConstant.ROLE_ADMIN)
//    public BaseResponse<Page<SysDictSchoolYearVO>> listSchoolYear() {
//        SysDictSchoolYearQueryRequest request = new SysDictSchoolYearQueryRequest();
//        request.setCurrent(1);
//        request.setPageSize(1000);
//        Page<SysDictSchoolYearVO> schoolYearPage = sysDictSchoolYearService.pageSchoolYear(request);
//        return ResultUtils.success(schoolYearPage);
//    }
}