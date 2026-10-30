import type { Media, MediaRequest, MediaType, Page, Problem } from './types'

export class ApiError extends Error {
  readonly status: number
  readonly problem: Problem

  constructor(problem: Problem) {
    super(problem.detail ?? problem.title ?? `Request failed with status ${problem.status}`)
    this.status = problem.status
    this.problem = problem
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(path, {
    ...init,
    headers: { Accept: 'application/json', 'Content-Type': 'application/json', ...init?.headers },
  })
  if (!response.ok) {
    const problem = await response.json().catch(() => ({ status: response.status }))
    throw new ApiError({ ...problem, status: response.status })
  }
  return response.status === 204 ? (undefined as T) : response.json()
}

export interface SearchParams {
  q?: string
  type?: MediaType
  page?: number
  size?: number
}

export const catalogApi = {
  search({ q, type, page = 0, size = 12 }: SearchParams): Promise<Page<Media>> {
    const params = new URLSearchParams({ page: String(page), size: String(size) })
    if (q) params.set('q', q)
    if (type) params.set('type', type)
    return request(`/api/v1/media?${params}`)
  },
  get: (id: string) => request<Media>(`/api/v1/media/${encodeURIComponent(id)}`),
  create: (body: MediaRequest) => request<Media>('/api/v1/media', { method: 'POST', body: JSON.stringify(body) }),
  update: (id: string, body: MediaRequest) =>
    request<Media>(`/api/v1/media/${encodeURIComponent(id)}`, { method: 'PUT', body: JSON.stringify(body) }),
  remove: (id: string) => request<void>(`/api/v1/media/${encodeURIComponent(id)}`, { method: 'DELETE' }),
}
