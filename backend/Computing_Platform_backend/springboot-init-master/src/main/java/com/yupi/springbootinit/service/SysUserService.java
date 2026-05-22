package com.yupi.springbootinit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.springbootinit.model.vo.SysUserVO;
import generator.domain.SysUser;

import javax.servlet.http.HttpServletRequest;

/**
 * 系统用户服务
 *
 * @author YU
 */
public interface SysUserService extends IService<SysUser> {

    /**
     * 用户登录
     *
     * @param username  用户账号
     * @param password  用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    SysUserVO login(String username, String password, HttpServletRequest request);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    boolean logout(HttpServletRequest request);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    SysUser getLoginUser(HttpServletRequest request);

    /**
     * 获取脱敏的用户信息
     *
     * @param user
     * @return
     */
    SysUserVO getUserVO(SysUser user);

    /**
     * 创建用户
     *
     * @param username  账号
     * @param password  密码
     * @param roleCode  角色编码
     * @param collegeId 学院ID
     * @param status    状态
     * @return 新用户 id
     */
    Long createSysUser(String username, String password, String roleCode, Long collegeId, Integer status);
}