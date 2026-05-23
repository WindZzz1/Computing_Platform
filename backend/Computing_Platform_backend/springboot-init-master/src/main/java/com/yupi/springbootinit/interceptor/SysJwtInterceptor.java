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
 * 系统用户JWT认证拦截器
 *
 * @author YU
 */
@Slf4j
@Component
public class SysJwtInterceptor implements HandlerInterceptor {

    @Resource
    private SysUserService sysUserService;

    /**
     * Token请求头名称
     */
    private static final String TOKEN_HEADER = "Authorization";

    /**
     * Token前缀
     */
    private static final String TOKEN_PREFIX = "Bearer ";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 如果不是映射到Controller方法，直接通过
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        // 获取方法上的AuthCheck注解
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        AuthCheck authCheck = handlerMethod.getMethodAnnotation(AuthCheck.class);

        // 如果没有注解，放行
        if (authCheck == null) {
            return true;
        }

        // 从请求头获取Token
        String token = getTokenFromRequest(request);
        if (token == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "未登录，请先登录");
        }

        // 验证Token并获取用户信息
        SysUser user = sysUserService.getLoginUserByToken(token);

        // 检查用户状态
        if (user.getStatus() != null && user.getStatus() == SysUserConstant.STATUS_DISABLED) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "账号已被禁用");
        }

        // 检查权限
        String mustRole = authCheck.mustRole();
        if (SysUserConstant.ROLE_ADMIN.equals(mustRole)) {
            if (!SysUserConstant.ROLE_ADMIN.equals(user.getRoleCode())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "需要管理员权限");
            }
        }

        // 将用户信息存入请求属性，供后续Controller、Service使用，只在本次请求有效
        request.setAttribute("currentUser", user);

        return true;
    }

    /**
     * 从请求中获取Token
     *
     * @param request HTTP请求
     * @return Token字符串
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(TOKEN_HEADER);
        if (bearerToken != null && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());  //去除前缀
        }
        // 也支持从参数中获取token
        return request.getParameter("token");
    }
}