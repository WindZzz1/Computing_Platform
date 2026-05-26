import { defineStore } from 'pinia'
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
    token: localStorage.getItem('token') || ''
  }),
  getters: {
    roleName: (state) => roleNames[state.role]
  },
  actions: {
    login(role: Role) {
      this.role = role
      this.name = role === 'teacher' ? '张老师' : role === 'leader' ? '专业负责人' : 'admin'
      this.token = 'mock-token'
      localStorage.setItem('role', this.role)
      localStorage.setItem('name', this.name)
      localStorage.setItem('token', this.token)
    },
    logout() {
      this.token = ''
      localStorage.removeItem('token')
    }
  }
})
