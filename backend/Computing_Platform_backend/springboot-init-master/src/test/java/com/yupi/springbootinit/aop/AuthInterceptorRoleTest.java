package com.yupi.springbootinit.aop;

import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.constant.SysUserConstant;
import com.yupi.springbootinit.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link AuthInterceptor#validateRole} 角色校验逻辑单元测试
 * <p>
 * 重点覆盖修复点：原先 {@code @AuthCheck(anyRole = ...)} 在 AOP 中被完全忽略
 * （只读取 mustRole），导致仅声明 anyRole 的接口对任意登录用户放行。
 */
class AuthInterceptorRoleTest {

    private final AuthInterceptor authInterceptor = new AuthInterceptor();

    /**
     * 未声明任何权限要求（mustRole 与 anyRole 均空）→ 放行
     */
    @Test
    void noRestriction_shouldPass() {
        assertDoesNotThrow(() -> authInterceptor.validateRole(SysUserConstant.ROLE_TEACHER, "", ""));
        assertDoesNotThrow(() -> authInterceptor.validateRole(SysUserConstant.ROLE_TEACHER, null, null));
    }

    /**
     * admin 视为超级管理员，放行 mustRole / anyRole 声明的任意接口
     */
    @Test
    void admin_shouldAlwaysPass() {
        assertDoesNotThrow(() -> authInterceptor.validateRole(
                SysUserConstant.ROLE_ADMIN, SysUserConstant.ROLE_TEACHER, ""));
        assertDoesNotThrow(() -> authInterceptor.validateRole(
                SysUserConstant.ROLE_ADMIN, "", SysUserConstant.ROLE_LEADER + "," + SysUserConstant.ROLE_EDU));
    }

    // ---------------- mustRole ----------------

    @Test
    void mustRole_match_shouldPass() {
        assertDoesNotThrow(() -> authInterceptor.validateRole(
                SysUserConstant.ROLE_TEACHER, SysUserConstant.ROLE_TEACHER, ""));
    }

    @Test
    void mustRole_mismatch_shouldReject() {
        BusinessException ex = assertThrows(BusinessException.class, () -> authInterceptor.validateRole(
                SysUserConstant.ROLE_LEADER, SysUserConstant.ROLE_TEACHER, ""));
        assertEquals(ErrorCode.NO_AUTH_ERROR.getCode(), ex.getCode());
    }

    /**
     * 修复前的安全隐患：mustRole 为未知值时原实现不会抛异常而放行。
     * 现在未知 mustRole 且不匹配时也应拒绝。
     */
    @Test
    void mustRole_unknownValue_shouldReject() {
        BusinessException ex = assertThrows(BusinessException.class, () -> authInterceptor.validateRole(
                SysUserConstant.ROLE_TEACHER, "unknown_role", ""));
        assertEquals(ErrorCode.NO_AUTH_ERROR.getCode(), ex.getCode());
    }

    // ---------------- anyRole（修复核心） ----------------

    @Test
    void anyRole_match_shouldPass() {
        assertDoesNotThrow(() -> authInterceptor.validateRole(
                SysUserConstant.ROLE_LEADER, "", SysUserConstant.ROLE_LEADER + "," + SysUserConstant.ROLE_EDU));
        assertDoesNotThrow(() -> authInterceptor.validateRole(
                SysUserConstant.ROLE_EDU, "", SysUserConstant.ROLE_LEADER + "," + SysUserConstant.ROLE_EDU));
    }

    /**
     * 【修复点】anyRole 声明了 leader/edu，teacher 不在列表中应被拒绝。
     * 修复前该用例会通过（bug）。
     */
    @Test
    void anyRole_mismatch_shouldReject() {
        BusinessException ex = assertThrows(BusinessException.class, () -> authInterceptor.validateRole(
                SysUserConstant.ROLE_TEACHER, "", SysUserConstant.ROLE_LEADER + "," + SysUserConstant.ROLE_EDU));
        assertEquals(ErrorCode.NO_AUTH_ERROR.getCode(), ex.getCode());
    }

    /**
     * anyRole 列表允许包含空格，应正确 trim 后匹配
     */
    @Test
    void anyRole_withSpaces_shouldTrimAndMatch() {
        assertDoesNotThrow(() -> authInterceptor.validateRole(
                SysUserConstant.ROLE_EDU, "", "leader, edu"));
    }

    /**
     * anyRole 列表为空字符串时等价于未声明 anyRole，应放行（不误杀）
     */
    @Test
    void anyRole_emptyString_shouldNotReject() {
        assertDoesNotThrow(() -> authInterceptor.validateRole(
                SysUserConstant.ROLE_TEACHER, "", ""));
    }
}