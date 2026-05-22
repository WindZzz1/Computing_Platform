package com.yupi.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.springbootinit.model.vo.SysUserVO;
import generator.domain.SysUser;
import org.apache.ibatis.annotations.Param;

/**
 * @author YU
 * @description 针对表【sys_user(用户表)】的数据库操作Mapper
 * @createDate 2026-05-21 18:39:44
 * @Entity generator.domain.SysUser
 */
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 根据用户名和密码查询用户（带学院名称）
     *
     * @param username 用户名
     * @param password 加密后的密码
     * @return 用户VO（含学院名称）
     */
    SysUserVO selectUserWithCollege(@Param("username") String username, @Param("password") String password);

    /**
     * 根据ID查询用户（带学院名称）
     *
     * @param userId 用户ID
     * @return 用户VO（含学院名称）
     */
    SysUserVO selectUserVOById(@Param("userId") Long userId);
}