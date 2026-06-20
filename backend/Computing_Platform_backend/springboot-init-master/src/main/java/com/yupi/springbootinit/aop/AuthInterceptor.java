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
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

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
        String anyRole = authCheck.anyRole();
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();

        Object currentUserObj = request.getAttribute("currentUser");
        if (currentUserObj instanceof SysUser) {
            SysUser sysUser = (SysUser) currentUserObj;
            validateRole(sysUser.getRoleCode(), mustRole, anyRole);
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
            validateRole(sysUser.getRoleCode(), mustRole, anyRole);
            return joinPoint.proceed();
        }

        throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
    }

    /**
     * 角色校验
     * <p>
     * 规则：
     * 1. mustRole 与 anyRole 均为空时放行（接口未声明权限）；
     * 2. admin 视为超级管理员，放行全部角色接口；
     * 3. mustRole 非空时，要求当前用户角色精确等于 mustRole；
     * 4. anyRole 非空时，要求当前用户角色属于 anyRole 列表（逗号分隔）中的任一角色；
     * 5. 若两者同时声明，需同时满足。
     *
     * @param userRole 当前登录用户角色
     * @param mustRole 必须的角色（精确匹配）
     * @param anyRole  允许的角色列表（逗号分隔，任一即可）
     */
    // 包级可见以便单元测试直接覆盖鉴权逻辑（AuthInterceptorRoleTest）
    void validateRole(String userRole, String mustRole, String anyRole) {
        boolean hasMustRole = mustRole != null && !mustRole.isEmpty();
        boolean hasAnyRole = anyRole != null && !anyRole.isEmpty();

        // 未声明任何权限要求，放行
        if (!hasMustRole && !hasAnyRole) {
            return;
        }

        // admin 视为超级管理员，放行全部角色接口
        if (SysUserConstant.ROLE_ADMIN.equals(userRole)) {
            return;
        }

        // mustRole：必须精确匹配
        if (hasMustRole && !mustRole.equals(userRole)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, buildMustRoleMessage(mustRole));
        }

        // anyRole：任一匹配即可
        if (hasAnyRole) {
            Set<String> allowedRoles = Arrays.stream(anyRole.split(","))
                    .map(String::trim)
                    .filter(role -> !role.isEmpty())
                    .collect(Collectors.toSet());
            if (!allowedRoles.contains(userRole)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限，所需角色：" + anyRole);
            }
        }
    }

    /**
     * 根据 mustRole 生成友好的权限提示文案
     */
    private String buildMustRoleMessage(String mustRole) {
        switch (mustRole) {
            case SysUserConstant.ROLE_ADMIN:
                return "需要管理员权限";
            case SysUserConstant.ROLE_LEADER:
                return "需要专业负责人权限";
            case SysUserConstant.ROLE_TEACHER:
                return "需要主讲教师权限";
            case SysUserConstant.ROLE_EDU:
                return "需要教务管理员权限";
            default:
                return "无权限";
        }
    }
}
