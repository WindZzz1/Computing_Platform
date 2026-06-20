package com.yupi.springbootinit.manager;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.constant.SysUserConstant;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.mapper.TeachingClassMapper;
import com.yupi.springbootinit.model.entity.SysUser;
import com.yupi.springbootinit.model.entity.TeachingClass;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 数据级归属校验（横向越权防护）。
 * <p>
 * 角色级鉴权（{@code @AuthCheck}）只保证"是 teacher"，不保证"是这个班/这门课的教师"。
 * 本 helper 在写操作前校验：当前登录用户是否为该教学班的主讲教师 / 是否讲授该课程，
 * admin 放行。修复任意 teacher 改/删他人课程与成绩的横向越权。
 *
 * @author YU
 */
@Component
public class OwnershipHelper {

    @Resource
    private TeachingClassMapper teachingClassMapper;

    /**
     * 取当前登录用户（由 SysJwtInterceptor 写入 request 的 currentUser 属性）。
     */
    public SysUser getCurrentUser() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes) {
            HttpServletRequest request = ((ServletRequestAttributes) attrs).getRequest();
            Object user = request.getAttribute("currentUser");
            if (user instanceof SysUser) {
                return (SysUser) user;
            }
        }
        return null;
    }

    /**
     * 校验当前登录用户拥有该教学班（是其主讲教师）；admin 放行。
     * 非本人且非 admin 抛 NO_AUTH。
     */
    public void checkClassOwnership(Long classId) {
        checkClassOwnership(getCurrentUser(), classId);
    }

    /**
     * 校验指定用户拥有该教学班。包级可见以便单测直接注入用户（绕过 RequestContextHolder）。
     */
    void checkClassOwnership(SysUser user, Long classId) {
        if (user == null || user.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        if (isAdmin(user)) {
            return;
        }
        TeachingClass teachingClass = teachingClassMapper.selectById(classId);
        if (teachingClass == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "教学班级不存在");
        }
        if (!user.getId().equals(teachingClass.getTeacherId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权操作该教学班级（仅本班主讲教师可操作）");
        }
    }

    /**
     * 校验当前登录用户讲授该课程（存在 course_id + teacher_id 命中的教学班）；admin 放行。
     * 用于课程级配置（课程目标/考核点/内部权重）的归属校验。
     */
    public void checkCourseOwnership(Long courseId) {
        checkCourseOwnership(getCurrentUser(), courseId);
    }

    /**
     * 校验指定用户讲授该课程。包级可见以便单测。
     */
    void checkCourseOwnership(SysUser user, Long courseId) {
        if (user == null || user.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        if (isAdmin(user)) {
            return;
        }
        QueryWrapper<TeachingClass> query = new QueryWrapper<>();
        query.eq("course_id", courseId).eq("teacher_id", user.getId());
        Long count = teachingClassMapper.selectCount(query);
        if (count == null || count == 0) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权操作该课程（你不是其主讲教师）");
        }
    }

    private boolean isAdmin(SysUser user) {
        return user != null && SysUserConstant.ROLE_ADMIN.equals(user.getRoleCode());
    }
}