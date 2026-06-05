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
import java.util.Arrays;
import java.util.stream.Collectors;

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

        validateRole(user.getRoleCode(), authCheck.mustRole(), authCheck.anyRole());
        request.setAttribute("currentUser", user);
        return true;
    }

    private void validateRole(String userRole, String mustRole, String anyRole) {
        // admin 视为超级管理员，放行全部角色接口
        if (SysUserConstant.ROLE_ADMIN.equals(userRole)) {
            return;
        }

        // 检查 mustRole
        if (mustRole != null && !mustRole.isEmpty()) {
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

        // 检查 anyRole（支持多个角色，逗号分隔）
        if (anyRole != null && !anyRole.isEmpty()) {
            String[] allowedRoles = anyRole.split(",");
            for (String allowedRole : allowedRoles) {
                if (allowedRole.trim().equals(userRole)) {
                    return; // 匹配任一角色即可
                }
            }

            // 构建错误消息
            String roleNames = String.join("或", Arrays.stream(allowedRoles)
                    .map(role -> {
                        switch (role.trim()) {
                            case SysUserConstant.ROLE_ADMIN: return "管理员";
                            case SysUserConstant.ROLE_LEADER: return "专业负责人";
                            case SysUserConstant.ROLE_TEACHER: return "主讲教师";
                            case SysUserConstant.ROLE_EDU: return "教务管理员";
                            default: return "相关角色";
                        }
                    })
                    .collect(Collectors.toList()));

            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "需要" + roleNames + "权限");
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
