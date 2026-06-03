package com.yupi.springbootinit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.springbootinit.model.entity.SysUser;
import com.yupi.springbootinit.model.vo.SysUserLoginVO;
import com.yupi.springbootinit.model.vo.SysUserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 系统用户服务
 *
 * @author YU
 */
public interface SysUserService extends IService<SysUser> {

    /**
     * 用户登录并返回 token
     *
     * @param username 用户账号
     * @param password 用户密码
     * @return 包含 token 的登录响应
     */
    SysUserLoginVO loginWithToken(String username, String password);

    /**
     * 根据 token 获取登录用户
     *
     * @param token JWT Token
     * @return 登录用户
     */
    SysUser getLoginUserByToken(String token);

    /**
     * 获取当前登录用户
     *
     * @param request HTTP 请求
     * @return 登录用户
     */
    SysUser getLoginUser(HttpServletRequest request);

    /**
     * 获取脱敏用户信息
     *
     * @param user 用户实体
     * @return 用户视图对象
     */
    SysUserVO getUserVO(SysUser user);

    /**
     * 按角色获取用户列表
     *
     * @param roleCode 角色编码
     * @return 用户列表
     */
    List<SysUserVO> listUsersByRole(String roleCode);

    /**
     * 创建用户
     *
     * @param username 账号
     * @param password 密码
     * @param roleCode 角色编码
     * @param collegeId 学院 ID
     * @param status 状态
     * @return 新用户 id
     */
    Long createSysUser(String username, String password, String roleCode, Long collegeId, Integer status);
}
