package com.yupi.springbootinit.model.dto.SysUser;

import lombok.Data;

import java.io.Serializable;

/**
 * 系统用户登录请求
 *
 * @author YU
 */
@Data
public class SysUserLoginRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 账号
     */
    private String username;

    /**
     * 密码
     */
    private String password;
}
