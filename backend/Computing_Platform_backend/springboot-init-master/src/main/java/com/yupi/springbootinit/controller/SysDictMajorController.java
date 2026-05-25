package com.yupi.springbootinit.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.springbootinit.annotation.AuthCheck;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.DeleteRequest;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.model.dto.dict.SysDictMajorAddRequest;
import com.yupi.springbootinit.model.dto.dict.SysDictMajorQueryRequest;
import com.yupi.springbootinit.model.dto.dict.SysDictMajorUpdateRequest;
import com.yupi.springbootinit.model.vo.PageResultVO;
import com.yupi.springbootinit.model.vo.SysDictMajorSimpleVO;
import com.yupi.springbootinit.model.vo.SysDictMajorVO;
import com.yupi.springbootinit.service.SysDictMajorService;
import com.yupi.springbootinit.constant.SysUserConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 专业字典接口
 *
 * @author YU
 */
@RestController
@RequestMapping("/dict/major")
@Slf4j
public class SysDictMajorController {

    @Resource
    private SysDictMajorService sysDictMajorService;

    /**
     * 创建专业
     *
     * @param sysDictMajorAddRequest 新增请求
     * @return 专业ID
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = SysUserConstant.ROLE_ADMIN)
    public BaseResponse<Long> addMajor(@RequestBody SysDictMajorAddRequest sysDictMajorAddRequest) {
        Long majorId = sysDictMajorService.createMajor(sysDictMajorAddRequest);
        return ResultUtils.success(majorId);
    }

    /**
     * 更新专业
     *
     * @param sysDictMajorUpdateRequest 更新请求
     * @return 是否成功
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = SysUserConstant.ROLE_ADMIN)
    public BaseResponse<Boolean> updateMajor(@RequestBody SysDictMajorUpdateRequest sysDictMajorUpdateRequest) {
        Boolean result = sysDictMajorService.updateMajor(sysDictMajorUpdateRequest);
        return ResultUtils.success(result);
    }

    /**
     * 删除专业
     *
     * @param deleteRequest 删除请求
     * @return 是否成功
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = SysUserConstant.ROLE_ADMIN)
    public BaseResponse<Boolean> deleteMajor(@RequestBody DeleteRequest deleteRequest) {
        Boolean result = sysDictMajorService.deleteMajor(deleteRequest.getId());
        return ResultUtils.success(result);
    }

    /**
     * 根据ID获取专业
     *
     * @param deleteRequest ID请求
     * @return 专业信息
     */
    @PostMapping("/get")
    @AuthCheck(mustRole = SysUserConstant.ROLE_ADMIN)
    public BaseResponse<SysDictMajorVO> getMajorById(@RequestBody DeleteRequest deleteRequest) {
        SysDictMajorVO majorVO = sysDictMajorService.getMajorById(deleteRequest.getId());
        return ResultUtils.success(majorVO);
    }

    /**
     * 分页查询专业
     *
     * @param sysDictMajorQueryRequest 查询请求
     * @return 分页结果
     */
    @PostMapping("/page")
    @AuthCheck(mustRole = SysUserConstant.ROLE_ADMIN)
    public BaseResponse<PageResultVO<SysDictMajorVO>> pageMajor(@RequestBody SysDictMajorQueryRequest sysDictMajorQueryRequest) {
        Page<SysDictMajorVO> majorPage = sysDictMajorService.pageMajor(sysDictMajorQueryRequest);
        return ResultUtils.success(PageResultVO.from(majorPage));
    }

    /**
     * 获取所有专业列表（简化）
     *
     * @return 专业简化列表
     */
    @PostMapping("/list")
    @AuthCheck(mustRole = SysUserConstant.ROLE_ADMIN)
    public BaseResponse<java.util.List<SysDictMajorSimpleVO>> listMajor() {
        java.util.List<SysDictMajorSimpleVO> list = sysDictMajorService.listMajorSimple();
        return ResultUtils.success(list);
    }
}