package com.yupi.springbootinit.config;

import com.yupi.springbootinit.interceptor.SysJwtInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * Web MVC配置
 *
 * @author YU
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private SysJwtInterceptor sysJwtInterceptor;

    /**
     * 配置拦截器
     *
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 系统用户JWT认证拦截器
        registry.addInterceptor(sysJwtInterceptor)
                .addPathPatterns("/dict/**")      // 拦截字典管理相关接口
                .addPathPatterns("/requirement/**") // 拦截毕业要求和指标点相关接口
                .excludePathPatterns(
                        "/sysuser/login",      // 登录接口
                        "/sysuser/login/token" // Token登录接口
                );
    }
}