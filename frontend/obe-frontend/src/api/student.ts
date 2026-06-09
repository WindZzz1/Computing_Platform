import { apiPost, type PageResponse, type StudentPageQuery, type StudentVO } from './backend'

export function pageStudents(payload: StudentPageQuery) {
  return apiPost<PageResponse<StudentVO>>('/student/page', payload)
}
