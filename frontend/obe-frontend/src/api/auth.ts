import request from './request'
import type { Role } from '@/types'

export interface LoginPayload {
  username: string
  password: string
}

export interface LoginUser {
  id: number
  username: string
  roleCode: Role
  collegeName?: string
  status: number
  createTime?: string
  token: string
}

export interface CurrentUser {
  id: number
  username: string
  roleCode: Role
  collegeName?: string
  status: number
  createTime?: string
}

export const loginWithToken = (payload: LoginPayload) => {
  return request.post<LoginUser, LoginUser>('/sysuser/login/token', payload)
}

export const getLoginUser = () => {
  return request.get<CurrentUser, CurrentUser>('/sysuser/get/login')
}
