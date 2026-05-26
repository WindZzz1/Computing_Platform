export type Role = 'admin' | 'edu' | 'leader' | 'teacher'

export type CourseStatus = '未配置' | '待计算' | '已锁定' | '未提交'

export interface Course {
  id: number
  code: string
  name: string
  teacher: string
  credit: number
  term: string
  studentCount: number
  status: CourseStatus
}

export interface Indicator {
  id: number
  code: string
  name: string
  requirement: string
  achievement: number
}

export interface Objective {
  id: number
  code: string
  content: string
  achievement: number
}

export interface Assessment {
  id: number
  name: string
  score: number
  objectiveId: number
  method: string
}
