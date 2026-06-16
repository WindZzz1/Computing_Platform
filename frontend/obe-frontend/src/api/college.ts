import {
  apiPost,
  type PageResponse,
  type SysDictCollegeCreateRequest,
  type SysDictCollegeSimpleVO,
  type SysDictCollegeUpdateRequest,
  type SysDictCollegeVO
} from './backend'

export function listColleges() {
  return apiPost<SysDictCollegeSimpleVO[]>('/dict/college/list')
}

export function getCollege(id: number) {
  return apiPost<SysDictCollegeVO>('/dict/college/get', { id })
}

export function pageColleges(payload: { current?: number; pageSize?: number; collegeName?: string }) {
  return apiPost<PageResponse<SysDictCollegeVO>>('/dict/college/page', payload)
}

export function createCollege(payload: SysDictCollegeCreateRequest) {
  return apiPost<number>('/dict/college/add', payload)
}

export function updateCollege(payload: SysDictCollegeUpdateRequest) {
  return apiPost<boolean>('/dict/college/update', payload)
}

export function deleteCollege(id: number) {
  return apiPost<boolean>('/dict/college/delete', { id })
}
