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
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private SysUserService sysUserService;

    /**
     * 执行拦截
     *
     * @param joinPoint
     * @param authCheck
     * @return
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();

        // 优先检查JWT拦截器验证过的用户（从request attribute中获取）
        Object currentUserObj = request.getAttribute("currentUser");
        if (currentUserObj instanceof SysUser) {
            // JWT验证通过的系统用户
            SysUser sysUser = (SysUser) currentUserObj;
            // 检查管理员权限
            if (SysUserConstant.ROLE_ADMIN.equals(mustRole)) {
                if (!SysUserConstant.ROLE_ADMIN.equals(sysUser.getRoleCode())) {
                    throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "需要管理员权限");
                }
            }
            return joinPoint.proceed();
        }

        // 检查Session中的系统用户登录状态
        Object sysUserObj = request.getSession().getAttribute(SysUserConstant.SYS_USER_LOGIN_STATE);
        if (sysUserObj != null) {
            // Session登录的系统用户
            SysUser sysUser = (SysUser) sysUserObj;
            if (sysUser == null || sysUser.getId() == null) {
                throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
            }
            // 从数据库查询最新用户信息
            sysUser = sysUserService.getById(sysUser.getId());
            if (sysUser == null) {
                throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
            }
            // 检查管理员权限
            if (SysUserConstant.ROLE_ADMIN.equals(mustRole)) {
                if (!SysUserConstant.ROLE_ADMIN.equals(sysUser.getRoleCode())) {
                    throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "需要管理员权限");
                }
            }
            return joinPoint.proceed();
        }

        // 未登录
        throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
    }
}
