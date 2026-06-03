package com.yupi.springbootinit.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.springbootinit.annotation.AuthCheck;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.DeleteRequest;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.constant.SysUserConstant;
import com.yupi.springbootinit.model.dto.dict.SysDictSchoolYearAddRequest;
import com.yupi.springbootinit.model.dto.dict.SysDictSchoolYearQueryRequest;
import com.yupi.springbootinit.model.dto.dict.SysDictSchoolYearUpdateRequest;
import com.yupi.springbootinit.model.vo.PageResultVO;
import com.yupi.springbootinit.model.vo.SysDictSchoolYearVO;
import com.yupi.springbootinit.service.SysDictSchoolYearService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

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

    @PostMapping("/add")
    @AuthCheck(mustRole = SysUserConstant.ROLE_ADMIN)
    public BaseResponse<Long> addSchoolYear(@RequestBody SysDictSchoolYearAddRequest request) {
        return ResultUtils.success(sysDictSchoolYearService.createSchoolYear(request));
    }

    @PostMapping("/update")
    @AuthCheck(mustRole = SysUserConstant.ROLE_ADMIN)
    public BaseResponse<Boolean> updateSchoolYear(@RequestBody SysDictSchoolYearUpdateRequest request) {
        return ResultUtils.success(sysDictSchoolYearService.updateSchoolYear(request));
    }

    @PostMapping("/delete")
    @AuthCheck(mustRole = SysUserConstant.ROLE_ADMIN)
    public BaseResponse<Boolean> deleteSchoolYear(@RequestBody DeleteRequest deleteRequest) {
        return ResultUtils.success(sysDictSchoolYearService.deleteSchoolYear(deleteRequest.getId()));
    }

    @PostMapping("/get")
    @AuthCheck(mustRole = SysUserConstant.ROLE_ADMIN)
    public BaseResponse<SysDictSchoolYearVO> getSchoolYearById(@RequestBody DeleteRequest deleteRequest) {
        return ResultUtils.success(sysDictSchoolYearService.getSchoolYearById(deleteRequest.getId()));
    }

    @PostMapping("/page")
    @AuthCheck(mustRole = SysUserConstant.ROLE_ADMIN)
    public BaseResponse<PageResultVO<SysDictSchoolYearVO>> pageSchoolYear(@RequestBody SysDictSchoolYearQueryRequest request) {
        Page<SysDictSchoolYearVO> schoolYearPage = sysDictSchoolYearService.pageSchoolYear(request);
        return ResultUtils.success(PageResultVO.from(schoolYearPage));
    }

    @PostMapping("/list")
    @AuthCheck(mustRole = SysUserConstant.ROLE_EDU)
    public BaseResponse<List<SysDictSchoolYearVO>> listSchoolYear() {
        SysDictSchoolYearQueryRequest request = new SysDictSchoolYearQueryRequest();
        request.setCurrent(1);
        request.setPageSize(1000);
        Page<SysDictSchoolYearVO> schoolYearPage = sysDictSchoolYearService.pageSchoolYear(request);
        return ResultUtils.success(schoolYearPage.getRecords());
    }
}
