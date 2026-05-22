package com.yupi.springbootinit.model.dto.SysUser;

import lombok.Data;

import java.io.Serializable;

/**
 * 系统用户创建请求
 *
 * @author YU
 */
@Data
public class SysUserAddRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 账号
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 角色编码：admin/edu/leader/teacher
     */
    private String roleCode;

    /**
     * 所属学院ID
     */
    private Long collegeId;

    /**
     * 状态：1正常 0禁用
     */
    private Integer status;
}
