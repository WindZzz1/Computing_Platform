package com.yupi.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.constant.SysUserConstant;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.mapper.SysUserMapper;
import com.yupi.springbootinit.model.entity.SysUser;
import com.yupi.springbootinit.model.vo.SysUserLoginVO;
import com.yupi.springbootinit.model.vo.SysUserVO;
import com.yupi.springbootinit.service.SysUserService;
import com.yupi.springbootinit.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 系统用户服务实现
 *
 * @author YU
 */
@Service
@Slf4j
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private JwtUtil jwtUtil;

    /**
     * 盐值，混淆密码
     */
    public static final String SALT = "yupi";

    @Override
    public SysUserLoginVO loginWithToken(String username, String password) {
        // 1. 校验
        if (StringUtils.isAnyBlank(username, password)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (username.length() < 3) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号长度不能少于3位");
        }
        if (password.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度不能少于6位");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes());
        // 3. 查询用户是否存在（联合查询学院名称）
        SysUserVO userVO = sysUserMapper.selectUserWithCollege(username, encryptPassword);
        // 用户不存在
        if (userVO == null) {
            log.info("user login failed, username cannot match password");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名或密码错误");
        }
        // 检查用户状态
        if (userVO.getStatus() != null && userVO.getStatus() == SysUserConstant.STATUS_DISABLED) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "账号已被禁用");
        }
        // 4. 查询完整用户信息
        SysUser user = this.getById(userVO.getId());
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        // 5. 生成JWT Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRoleCode());
        // 6. 构建登录响应
        SysUserLoginVO loginVO = new SysUserLoginVO();
        BeanUtils.copyProperties(userVO, loginVO);
        loginVO.setToken(token);
        return loginVO;
    }

//    @Override
//    public SysUserVO login(String username, String password, HttpServletRequest request) {
//        // 1. 校验
//        if (StringUtils.isAnyBlank(username, password)) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
//        }
//        if (username.length() < 3) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号长度不能少于3位");
//        }
//        if (password.length() < 6) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度不能少于6位");
//        }
//        // 2. 加密
//        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes());
//        // 3. 查询用户是否存在（联合查询学院名称）
//        SysUserVO userVO = sysUserMapper.selectUserWithCollege(username, encryptPassword);
//        // 用户不存在
//        if (userVO == null) {
//            log.info("user login failed, username cannot match password");
//            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名或密码错误");
//        }
//        // 检查用户状态
//        if (userVO.getStatus() != null && userVO.getStatus() == SysUserConstant.STATUS_DISABLED) {
//            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "账号已被禁用");
//        }
//        // 4. 查询完整用户信息并存储到 Session
//        SysUser user = this.getById(userVO.getId());
//        request.getSession().setAttribute(SysUserConstant.SYS_USER_LOGIN_STATE, user);
//        return userVO;
//    }

//    @Override
//    public boolean logout(HttpServletRequest request) {
//        if (request.getSession().getAttribute(SysUserConstant.SYS_USER_LOGIN_STATE) == null) {
//            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
//        }
//        // 移除登录态
//        request.getSession().removeAttribute(SysUserConstant.SYS_USER_LOGIN_STATE);
//        return true;
//    }

    @Override
    public SysUser getLoginUserByToken(String token) {
        if (StringUtils.isBlank(token)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "未登录");
        }
        // 验证token
        if (!jwtUtil.validateToken(token)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "Token无效或已过期");
        }
        // 从token中获取用户ID
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "Token解析失败");
        }
        // 从数据库查询用户信息
        SysUser user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户不存在");
        }
        return user;
    }

    @Override
    public SysUser getLoginUser(HttpServletRequest request) {
        // 优先从拦截器已验证的请求属性中获取用户信息
        Object userObj = request.getAttribute("currentUser");
        if (userObj instanceof SysUser) {
            return (SysUser) userObj;
        }

        // 如果拦截器没有处理（如没有@AuthCheck注解的接口），则自行解析Token
        String token = getTokenFromRequest(request);
        return getLoginUserByToken(token);
    }

    /**
     * 从请求中获取Token
     *
     * @param request HTTP请求
     * @return Token字符串
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        // 也支持从参数中获取token
        return request.getParameter("token");
    }


    @Override
    public SysUserVO getUserVO(SysUser user) {
        if (user == null) {
            return null;
        }
        // 使用联合查询获取学院名称
        return sysUserMapper.selectUserVOById(user.getId());
    }

    @Override
    public Long createSysUser(String username, String password, String roleCode, Long collegeId, Integer status) {
        // 1. 校验
        if (StringUtils.isAnyBlank(username, password)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码不能为空");
        }
        if (username.length() < 3) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号长度不能少于3位");
        }
        if (password.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度不能少于6位");
        }
        if (StringUtils.isBlank(roleCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "角色编码不能为空");
        }
        synchronized (username.intern()) {
            // 账号不能重复
            QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("username", username);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号已存在");
            }
            // 2. 加密密码
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes());
            // 3. 插入数据
            SysUser user = new SysUser();
            user.setUsername(username);
            user.setPassword(encryptPassword);
            user.setRoleCode(roleCode);
            user.setCollegeId(collegeId);
            user.setStatus(status == null ? SysUserConstant.STATUS_NORMAL : status);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建用户失败，数据库错误");
            }
            return user.getId();
        }
    }
}
