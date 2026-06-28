import { apiPost, type PageResponse, type StudentAddRequest, type StudentPageQuery, type StudentUpdateRequest, type StudentVO } from './backend'

export function pageStudents(payload: StudentPageQuery) {
  return apiPost<PageResponse<StudentVO>>('/student/page', payload)
}

export function createStudent(payload: StudentAddRequest) {
  return apiPost<number>('/student/add', payload)
}

export function updateStudent(payload: StudentUpdateRequest) {
  return apiPost<boolean>('/student/update', payload)
}

export function deleteStudent(id: number) {
  return apiPost<boolean>('/student/delete', { id })
}

export function importStudents(payload: {
  students: Array<{
    studentNo: string
    studentName: string
    majorCode?: string
    grade?: string
    className?: string
  }>
}) {
  return apiPost<{
    total?: number
    totalCount?: number
    successCount: number
    failCount: number
    failDetails?: Array<Record<string, string>>
  }>('/student/import', payload)
}
