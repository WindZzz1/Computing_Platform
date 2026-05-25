package com.yupi.springbootinit.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.springbootinit.annotation.AuthCheck;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.DeleteRequest;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.model.dto.dict.SysDictCollegeAddRequest;
import com.yupi.springbootinit.model.dto.dict.SysDictCollegeQueryRequest;
import com.yupi.springbootinit.model.dto.dict.SysDictCollegeUpdateRequest;
import com.yupi.springbootinit.model.vo.PageResultVO;
import com.yupi.springbootinit.model.vo.SysDictCollegeSimpleVO;
import com.yupi.springbootinit.model.vo.SysDictCollegeVO;
import com.yupi.springbootinit.service.SysDictCollegeService;
import com.yupi.springbootinit.constant.SysUserConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 学院字典接口
 *
 * @author YU
 */
@RestController
@RequestMapping("/dict/college")
@Slf4j
public class SysDictCollegeController {

    @Resource
    private SysDictCollegeService sysDictCollegeService;

    /**
     * 创建学院
     *
     * @param sysDictCollegeAddRequest 新增请求
     * @return 学院ID
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = SysUserConstant.ROLE_ADMIN)
    public BaseResponse<Long> addCollege(@RequestBody SysDictCollegeAddRequest sysDictCollegeAddRequest) {
        Long collegeId = sysDictCollegeService.createCollege(sysDictCollegeAddRequest);
        return ResultUtils.success(collegeId);
    }

    /**
     * 更新学院
     *
     * @param sysDictCollegeUpdateRequest 更新请求
     * @return 是否成功
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = SysUserConstant.ROLE_ADMIN)
    public BaseResponse<Boolean> updateCollege(@RequestBody SysDictCollegeUpdateRequest sysDictCollegeUpdateRequest) {
        Boolean result = sysDictCollegeService.updateCollege(sysDictCollegeUpdateRequest);
        return ResultUtils.success(result);
    }

    /**
     * 删除学院
     *
     * @param deleteRequest 删除请求
     * @return 是否成功
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = SysUserConstant.ROLE_ADMIN)
    public BaseResponse<Boolean> deleteCollege(@RequestBody DeleteRequest deleteRequest) {
        Boolean result = sysDictCollegeService.deleteCollege(deleteRequest.getId());
        return ResultUtils.success(result);
    }

    /**
     * 根据ID获取学院
     *
     * @param deleteRequest ID请求
     * @return 学院信息
     */
    @PostMapping("/get")
    @AuthCheck(mustRole = SysUserConstant.ROLE_ADMIN)
    public BaseResponse<SysDictCollegeVO> getCollegeById(@RequestBody DeleteRequest deleteRequest) {
        SysDictCollegeVO collegeVO = sysDictCollegeService.getCollegeById(deleteRequest.getId());
        return ResultUtils.success(collegeVO);
    }

    /**
     * 分页查询学院
     *
     * @param sysDictCollegeQueryRequest 查询请求
     * @return 分页结果
     */
    @PostMapping("/page")
    @AuthCheck(mustRole = SysUserConstant.ROLE_ADMIN)
    public BaseResponse<PageResultVO<SysDictCollegeVO>> pageCollege(@RequestBody SysDictCollegeQueryRequest sysDictCollegeQueryRequest) {
        Page<SysDictCollegeVO> collegePage = sysDictCollegeService.pageCollege(sysDictCollegeQueryRequest);
        return ResultUtils.success(PageResultVO.from(collegePage));
    }

    /**
     * 获取所有学院列表（简化）
     *
     * @return 学院简化列表
     */
    @PostMapping("/list")
    @AuthCheck(mustRole = SysUserConstant.ROLE_ADMIN)
    public BaseResponse<java.util.List<SysDictCollegeSimpleVO>> listCollege() {
        java.util.List<SysDictCollegeSimpleVO> list = sysDictCollegeService.listCollegeSimple();
        return ResultUtils.success(list);
    }
}