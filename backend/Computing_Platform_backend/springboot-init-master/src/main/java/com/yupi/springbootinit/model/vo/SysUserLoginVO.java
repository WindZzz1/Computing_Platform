package com.yupi.springbootinit.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 系统用户登录响应VO
 *
 * @author YU
 */
@Data
public class SysUserLoginVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户 id
     */
    private Long id;

    /**
     * 账号
     */
    private String username;

    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 学院名称
     */
    private String collegeName;

    /**
     * 状态：1正常 0禁用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * JWT Token
     */
    private String token;
}
