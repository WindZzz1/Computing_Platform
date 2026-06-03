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
import java.util.List;

/**
 * 系统用户服务实现
 *
 * @author YU
 */
@Service
@Slf4j
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    public static final String SALT = "yupi";

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private JwtUtil jwtUtil;

    @Override
    public SysUserLoginVO loginWithToken(String username, String password) {
        if (StringUtils.isAnyBlank(username, password)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        }
        if (username.length() < 3) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号长度不能少于3位");
        }
        if (password.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度不能少于6位");
        }

        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes());
        SysUserVO userVO = sysUserMapper.selectUserWithCollege(username, encryptPassword);
        if (userVO == null) {
            log.info("user login failed, username cannot match password");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名或密码错误");
        }
        if (userVO.getStatus() != null && userVO.getStatus() == SysUserConstant.STATUS_DISABLED) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "账号已被禁用");
        }

        SysUser user = this.getById(userVO.getId());
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRoleCode());
        SysUserLoginVO loginVO = new SysUserLoginVO();
        BeanUtils.copyProperties(userVO, loginVO);
        loginVO.setToken(token);
        return loginVO;
    }

    @Override
    public SysUser getLoginUserByToken(String token) {
        if (StringUtils.isBlank(token)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "未登录");
        }
        if (!jwtUtil.validateToken(token)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "Token无效或已过期");
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "Token解析失败");
        }
        SysUser user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户不存在");
        }
        return user;
    }

    @Override
    public SysUser getLoginUser(HttpServletRequest request) {
        Object userObj = request.getAttribute("currentUser");
        if (userObj instanceof SysUser) {
            return (SysUser) userObj;
        }

        String token = getTokenFromRequest(request);
        return getLoginUserByToken(token);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return request.getParameter("token");
    }

    @Override
    public SysUserVO getUserVO(SysUser user) {
        if (user == null) {
            return null;
        }
        return sysUserMapper.selectUserVOById(user.getId());
    }

    @Override
    public List<SysUserVO> listUsersByRole(String roleCode) {
        if (StringUtils.isBlank(roleCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "角色编码不能为空");
        }
        return sysUserMapper.selectUsersByRole(roleCode);
    }

    @Override
    public Long createSysUser(String username, String password, String roleCode, Long collegeId, Integer status) {
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
            QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("username", username);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号已存在");
            }

            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes());
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
