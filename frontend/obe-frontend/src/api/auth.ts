import { apiGet, apiPost, type CreateUserRequest, type SysUserLoginRequest, type SysUserLoginVO, type SysUserVO } from './backend'

export function loginWithToken(payload: SysUserLoginRequest) {
  return apiPost<SysUserLoginVO>('/sysuser/login/token', payload)
}

export function createSysUser(payload: CreateUserRequest) {
  return apiPost<number>('/sysuser/add', payload)
}

export function getLoginUser() {
  return apiGet<SysUserLoginVO>('/sysuser/get/login')
}

export function listUsersByRole(roleCode: string) {
  return apiGet<SysUserVO[]>('/sysuser/list/by-role', {
    params: {
      roleCode
    }
  })
}
