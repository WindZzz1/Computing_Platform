import { apiPost, type PageResponse, type StudentPageQuery, type StudentVO } from './backend'

export function pageStudents(payload: StudentPageQuery) {
  return apiPost<PageResponse<StudentVO>>('/student/page', payload)
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
