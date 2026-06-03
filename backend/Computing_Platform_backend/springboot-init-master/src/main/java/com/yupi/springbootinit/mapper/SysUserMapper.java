package com.yupi.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.springbootinit.model.entity.SysUser;
import com.yupi.springbootinit.model.vo.SysUserVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author YU
 * @description 针对表【sys_user(用户表)】的数据库操作Mapper
 * @createDate 2026-05-21 18:39:44
 * @Entity com.yupi.springbootinit.model.entity.SysUser
 */
@Mapper
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

    /**
     * 按角色查询用户列表
     *
     * @param roleCode 角色编码
     * @return 用户列表
     */
    List<SysUserVO> selectUsersByRole(@Param("roleCode") String roleCode);
}
