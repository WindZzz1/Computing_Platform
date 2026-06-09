import { defineStore } from 'pinia'
import { getLoginUser, loginWithToken } from '@/api/auth'
import type { SysUserLoginVO } from '@/api/backend'
import type { Role } from '@/types'

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
    token: localStorage.getItem('token') || '',
    collegeName: localStorage.getItem('collegeName') || ''
  }),
  getters: {
    roleName: (state) => roleNames[state.role]
  },
  actions: {
    applyUser(result: SysUserLoginVO) {
      this.name = result.username
      this.role = result.roleCode as Role
      this.collegeName = result.collegeName || ''
      if (result.token) {
        this.token = result.token
      }
      localStorage.setItem('role', this.role)
      localStorage.setItem('name', this.name)
      localStorage.setItem('token', this.token)
      localStorage.setItem('collegeName', this.collegeName)
    },
    async login(username: string, password: string) {
      const result: SysUserLoginVO = await loginWithToken({ username, password })
      this.applyUser(result)
    },
    async syncCurrentUser() {
      if (!this.token) return
      const result: SysUserLoginVO = await getLoginUser()
      this.applyUser({
        ...result,
        token: this.token
      })
    },
    logout() {
      this.token = ''
      this.name = 'admin'
      this.role = 'admin'
      this.collegeName = ''
      localStorage.removeItem('token')
      localStorage.removeItem('name')
      localStorage.removeItem('role')
      localStorage.removeItem('collegeName')
    }
  }
})
