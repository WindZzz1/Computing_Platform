package com.yupi.springbootinit.controller;

import com.yupi.springbootinit.annotation.AuthCheck;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.model.dto.SysUser.SysUserAddRequest;
import com.yupi.springbootinit.model.dto.SysUser.SysUserLoginRequest;
import com.yupi.springbootinit.model.vo.SysUserVO;
import com.yupi.springbootinit.service.SysUserService;
import generator.domain.SysUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 系统用户接口
 *
 * @author YU
 */
@RestController
@RequestMapping("/sysuser")
@Slf4j
public class SysUserController {

    @Resource
    private SysUserService sysUserService;

    /**
     * 用户登录
     *
     * @param sysUserLoginRequest 登录请求
     * @param request             HTTP请求
     * @return 登录用户信息
     */
    @PostMapping("/login")
    public BaseResponse<SysUserVO> login(@RequestBody SysUserLoginRequest sysUserLoginRequest, HttpServletRequest request) {
        if (sysUserLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String username = sysUserLoginRequest.getUsername();
        String password = sysUserLoginRequest.getPassword();
        if (StringUtils.isAnyBlank(username, password)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名或密码不能为空");
        }
        SysUserVO sysUserVO = sysUserService.login(username, password, request);
        return ResultUtils.success(sysUserVO);
    }

    /**
     * 用户登出
     *
     * @param request HTTP请求
     * @return 是否成功
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> logout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = sysUserService.logout(request);
        return ResultUtils.success(result);
    }

    /**
     * 获取当前登录用户信息
     *
     * @param request HTTP请求
     * @return 登录用户信息
     */
    @GetMapping("/get/login")
    public BaseResponse<SysUserVO> getLoginUser(HttpServletRequest request) {
        SysUser user = sysUserService.getLoginUser(request);
        return ResultUtils.success(sysUserService.getUserVO(user));
    }


    /**
     * 创建用户
     *
     * @param sysUserAddRequest 创建用户请求
     * @return 新用户 id
     */
    @PostMapping("/add")
//    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addSysUser(@RequestBody SysUserAddRequest sysUserAddRequest) {
        if (sysUserAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String username = sysUserAddRequest.getUsername();
        String password = sysUserAddRequest.getPassword();
        String roleCode = sysUserAddRequest.getRoleCode();
        Long collegeId = sysUserAddRequest.getCollegeId();
        Integer status = sysUserAddRequest.getStatus();
        Long userId = sysUserService.createSysUser(username, password, roleCode, collegeId, status);
        return ResultUtils.success(userId);
    }

}