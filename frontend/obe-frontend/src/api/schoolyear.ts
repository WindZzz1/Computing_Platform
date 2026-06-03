import {
  apiPost,
  type PageResponse,
  type SysDictSchoolYearCreateRequest,
  type SysDictSchoolYearUpdateRequest,
  type SysDictSchoolYearVO
} from './backend'

export function listSchoolYears() {
  return apiPost<SysDictSchoolYearVO[]>('/dict/schoolyear/list')
}

export function pageSchoolYears(payload: { current?: number; pageSize?: number; yearName?: string; semesterName?: string }) {
  return apiPost<PageResponse<SysDictSchoolYearVO>>('/dict/schoolyear/page', payload)
}

export function createSchoolYear(payload: SysDictSchoolYearCreateRequest) {
  return apiPost<number>('/dict/schoolyear/add', payload)
}

export function updateSchoolYear(payload: SysDictSchoolYearUpdateRequest) {
  return apiPost<boolean>('/dict/schoolyear/update', payload)
}

export function deleteSchoolYear(id: number) {
  return apiPost<boolean>('/dict/schoolyear/delete', { id })
}
