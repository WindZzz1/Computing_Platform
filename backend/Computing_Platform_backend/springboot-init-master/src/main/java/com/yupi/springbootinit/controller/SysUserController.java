package com.yupi.springbootinit.controller;

import com.yupi.springbootinit.annotation.AuthCheck;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.constant.SysUserConstant;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.model.dto.SysUser.SysUserAddRequest;
import com.yupi.springbootinit.model.dto.SysUser.SysUserLoginRequest;
import com.yupi.springbootinit.model.entity.SysUser;
import com.yupi.springbootinit.model.vo.SysUserLoginVO;
import com.yupi.springbootinit.model.vo.SysUserVO;
import com.yupi.springbootinit.service.SysUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

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

    @PostMapping("/login/token")
    public BaseResponse<SysUserLoginVO> loginWithToken(@RequestBody SysUserLoginRequest sysUserLoginRequest) {
        if (sysUserLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String username = sysUserLoginRequest.getUsername();
        String password = sysUserLoginRequest.getPassword();
        if (StringUtils.isAnyBlank(username, password)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名或密码不能为空");
        }
        return ResultUtils.success(sysUserService.loginWithToken(username, password));
    }

    @GetMapping("/get/login")
    public BaseResponse<SysUserVO> getLoginUser(HttpServletRequest request) {
        SysUser user = sysUserService.getLoginUser(request);
        return ResultUtils.success(sysUserService.getUserVO(user));
    }

    @GetMapping("/list/by-role")
    @AuthCheck(mustRole = SysUserConstant.ROLE_EDU)
    public BaseResponse<List<SysUserVO>> listUsersByRole(@RequestParam String roleCode) {
        return ResultUtils.success(sysUserService.listUsersByRole(roleCode));
    }

    @PostMapping("/add")
    public BaseResponse<Long> addSysUser(@RequestBody SysUserAddRequest sysUserAddRequest) {
        if (sysUserAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long userId = sysUserService.createSysUser(
            sysUserAddRequest.getUsername(),
            sysUserAddRequest.getPassword(),
            sysUserAddRequest.getRoleCode(),
            sysUserAddRequest.getCollegeId(),
            sysUserAddRequest.getStatus()
        );
        return ResultUtils.success(userId);
    }
}
