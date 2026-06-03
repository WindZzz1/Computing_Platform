package com.yupi.springbootinit.interceptor;

import com.yupi.springbootinit.annotation.AuthCheck;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.constant.SysUserConstant;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.model.entity.SysUser;
import com.yupi.springbootinit.service.SysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 系统用户 JWT 认证拦截器
 */
@Slf4j
@Component
public class SysJwtInterceptor implements HandlerInterceptor {

    @Resource
    private SysUserService sysUserService;

    private static final String TOKEN_HEADER = "Authorization";

    private static final String TOKEN_PREFIX = "Bearer ";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        AuthCheck authCheck = handlerMethod.getMethodAnnotation(AuthCheck.class);
        if (authCheck == null) {
            return true;
        }

        String token = getTokenFromRequest(request);
        if (token == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "未登录，请先登录");
        }

        SysUser user = sysUserService.getLoginUserByToken(token);
        if (user.getStatus() != null && user.getStatus() == SysUserConstant.STATUS_DISABLED) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "账号已被禁用");
        }

        validateRole(user.getRoleCode(), authCheck.mustRole());
        request.setAttribute("currentUser", user);
        return true;
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

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(TOKEN_HEADER);
        if (bearerToken != null && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        return request.getParameter("token");
    }
}
