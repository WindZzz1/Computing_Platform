import {
  apiPost,
  type PageResponse,
  type SysDictMajorCreateRequest,
  type SysDictMajorSimpleVO,
  type SysDictMajorUpdateRequest,
  type SysDictMajorVO
} from './backend'

export function listMajors() {
  return apiPost<SysDictMajorSimpleVO[]>('/dict/major/list')
}

export function getMajor(id: number) {
  return apiPost<SysDictMajorVO>('/dict/major/get', { id })
}

export function pageMajors(payload: { current?: number; pageSize?: number; majorCode?: string; majorName?: string; collegeId?: number | null }) {
  return apiPost<PageResponse<SysDictMajorVO>>('/dict/major/page', payload)
}

export function createMajor(payload: SysDictMajorCreateRequest) {
  return apiPost<number>('/dict/major/add', payload)
}

export function updateMajor(payload: SysDictMajorUpdateRequest) {
  return apiPost<boolean>('/dict/major/update', payload)
}

export function deleteMajor(id: number) {
  return apiPost<boolean>('/dict/major/delete', { id })
}
