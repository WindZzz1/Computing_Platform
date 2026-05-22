package com.yupi.springbootinit.constant;

/**
 * 系统用户常量
 *
 * @author YU
 */
public interface SysUserConstant {

    /**
     * 用户登录态键
     */
    String SYS_USER_LOGIN_STATE = "sys_user_login";

    /**
     * 角色编码 - 管理员
     */
    String ROLE_ADMIN = "admin";

    /**
     * 角色编码 - 教师
     */
    String ROLE_TEACHER = "teacher";

    /**
     * 角色编码 - 领导
     */
    String ROLE_LEADER = "leader";

    /**
     * 角色编码 - 教务
     */
    String ROLE_EDU = "edu";

    /**
     * 状态 - 正常
     */
    int STATUS_NORMAL = 1;

    /**
     * 状态 - 禁用
     */
    int STATUS_DISABLED = 0;
}
