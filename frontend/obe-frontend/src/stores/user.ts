import { defineStore } from 'pinia'
import type { Role } from '@/types'
import { getLoginUser, loginWithToken } from '@/api/auth'

const roleNames: Record<Role, string> = {
  admin: '系统管理员',
  edu: '教务管理员',
  leader: '专业负责人',
  teacher: '课程教师'
}

export const useUserStore = defineStore('user', {
  state: () => ({
    name: localStorage.getItem('name') || 'admin',
    role: (localStorage.getItem('role') as Role) || 'admin',
    token: localStorage.getItem('token') || ''
  }),
  getters: {
    roleName: (state) => roleNames[state.role]
  },
  actions: {
    setLoginState(payload: { username: string; roleCode: Role; token: string }) {
      this.role = payload.roleCode
      this.name = payload.username
      this.token = payload.token
      localStorage.setItem('role', this.role)
      localStorage.setItem('name', this.name)
      localStorage.setItem('token', this.token)
    },
    async login(username: string, password: string) {
      const user = await loginWithToken({ username, password })
      this.setLoginState({
        username: user.username,
        roleCode: user.roleCode,
        token: user.token
      })
      return user
    },
    async refreshLoginUser() {
      if (!this.token) return null
      const user = await getLoginUser()
      this.role = user.roleCode
      this.name = user.username
      localStorage.setItem('role', this.role)
      localStorage.setItem('name', this.name)
      return user
    },
    logout() {
      this.name = 'admin'
      this.role = 'admin'
      this.token = ''
      localStorage.removeItem('token')
      localStorage.removeItem('role')
      localStorage.removeItem('name')
    }
  }
})
