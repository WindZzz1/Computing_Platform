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

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(sysJwtInterceptor)
                .addPathPatterns(
                        "/dict/**",
                        "/requirement/**",
                        "/course/**",
                        "/student/**",
                        "/teaching-class/**",
                        "/weight/**",
                        "/assessment/**"
                )
                .excludePathPatterns(
                        "/sysuser/login",
                        "/sysuser/login/token"
                );
    }
}

