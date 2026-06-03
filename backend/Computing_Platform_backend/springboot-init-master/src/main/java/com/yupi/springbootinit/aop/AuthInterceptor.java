package com.yupi.springbootinit.aop;

import com.yupi.springbootinit.annotation.AuthCheck;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.constant.SysUserConstant;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.model.entity.SysUser;
import com.yupi.springbootinit.service.SysUserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 权限校验 AOP（系统用户）
 */
@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private SysUserService sysUserService;

    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();

        Object currentUserObj = request.getAttribute("currentUser");
        if (currentUserObj instanceof SysUser) {
            SysUser sysUser = (SysUser) currentUserObj;
            validateRole(sysUser.getRoleCode(), mustRole);
            return joinPoint.proceed();
        }

        Object sysUserObj = request.getSession().getAttribute(SysUserConstant.SYS_USER_LOGIN_STATE);
        if (sysUserObj instanceof SysUser) {
            SysUser sysUser = (SysUser) sysUserObj;
            if (sysUser.getId() == null) {
                throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
            }
            sysUser = sysUserService.getById(sysUser.getId());
            if (sysUser == null) {
                throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
            }
            validateRole(sysUser.getRoleCode(), mustRole);
            return joinPoint.proceed();
        }

        throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
    }

    private void validateRole(String userRole, String mustRole) {
        if (mustRole == null || mustRole.isEmpty()) {
            return;
        }

        // admin 视为超级管理员，放行全部角色接口
        if (SysUserConstant.ROLE_ADMIN.equals(userRole)) {
            return;
        }

        if (mustRole.equals(userRole)) {
            return;
        }

        if (SysUserConstant.ROLE_ADMIN.equals(mustRole)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "需要管理员权限");
        }
        if (SysUserConstant.ROLE_LEADER.equals(mustRole)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "需要专业负责人权限");
        }
        if (SysUserConstant.ROLE_TEACHER.equals(mustRole)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "需要主讲教师权限");
        }
        if (SysUserConstant.ROLE_EDU.equals(mustRole)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "需要教务管理员权限");
        }
    }
}
