package com.yupi.springbootinit.manager;

import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.constant.SysUserConstant;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.mapper.TeachingClassMapper;
import com.yupi.springbootinit.model.entity.SysUser;
import com.yupi.springbootinit.model.entity.TeachingClass;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * {@link OwnershipHelper} 横向越权校验测试。
 * <p>
 * 直接调用包级 (SysUser, Long) 重载，注入用户、绕过 RequestContextHolder。
 */
@ExtendWith(MockitoExtension.class)
class OwnershipHelperTest {

    @InjectMocks
    private OwnershipHelper helper;

    @Mock
    private TeachingClassMapper teachingClassMapper;

    private SysUser user(Long id, String role) {
        SysUser u = new SysUser();
        u.setId(id);
        u.setRoleCode(role);
        return u;
    }

    @Test
    void admin_bypassesClassOwnership() {
        // admin 即使非主讲教师也放行，且不查教学班
        assertDoesNotThrow(() -> helper.checkClassOwnership(user(1L, SysUserConstant.ROLE_ADMIN), 99L));
        verifyNoInteractions(teachingClassMapper);
    }

    @Test
    void classOwner_passes() {
        TeachingClass tc = new TeachingClass();
        tc.setId(1L);
        tc.setTeacherId(5L);
        when(teachingClassMapper.selectById(1L)).thenReturn(tc);
        assertDoesNotThrow(() -> helper.checkClassOwnership(user(5L, SysUserConstant.ROLE_TEACHER), 1L));
    }

    /** 【修复点】非本班教师应被拒绝——任意 teacher 改他人班成绩/计算即越权。
     */
    @Test
    void nonOwner_rejected() {
        TeachingClass tc = new TeachingClass();
        tc.setId(1L);
        tc.setTeacherId(999L); // 别人的班
        when(teachingClassMapper.selectById(1L)).thenReturn(tc);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> helper.checkClassOwnership(user(5L, SysUserConstant.ROLE_TEACHER), 1L));
        assertEquals(ErrorCode.NO_AUTH_ERROR.getCode(), ex.getCode());
    }

    @Test
    void classNotFound_rejectedAsNotFound() {
        when(teachingClassMapper.selectById(1L)).thenReturn(null);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> helper.checkClassOwnership(user(5L, SysUserConstant.ROLE_TEACHER), 1L));
        assertEquals(ErrorCode.NOT_FOUND_ERROR.getCode(), ex.getCode());
    }

    @Test
    void notLogin_rejected() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> helper.checkClassOwnership(null, 1L));
        assertEquals(ErrorCode.NOT_LOGIN_ERROR.getCode(), ex.getCode());
    }

    @Test
    void courseOwner_passes() {
        when(teachingClassMapper.selectCount(any())).thenReturn(2L);
        assertDoesNotThrow(() -> helper.checkCourseOwnership(user(5L, SysUserConstant.ROLE_TEACHER), 10L));
    }

    @Test
    void nonCourseTeacher_rejected() {
        when(teachingClassMapper.selectCount(any())).thenReturn(0L);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> helper.checkCourseOwnership(user(5L, SysUserConstant.ROLE_TEACHER), 10L));
        assertEquals(ErrorCode.NO_AUTH_ERROR.getCode(), ex.getCode());
    }

    @Test
    void admin_bypassesCourseOwnership() {
        assertDoesNotThrow(() -> helper.checkCourseOwnership(user(1L, SysUserConstant.ROLE_ADMIN), 10L));
        verifyNoInteractions(teachingClassMapper);
    }
}
