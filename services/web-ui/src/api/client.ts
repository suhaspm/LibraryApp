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

/**
 * The gateway sets an XSRF-TOKEN cookie; state-changing requests must echo it in a header.
 * Only JavaScript running on our own origin can read the cookie, which is what proves the
 * request came from this app and not from another site riding on the session cookie.
 */
export function csrfToken(): string | undefined {
  return document.cookie
    .split('; ')
    .find((c) => c.startsWith('XSRF-TOKEN='))
    ?.slice('XSRF-TOKEN='.length)
}

const SAFE_METHODS = ['GET', 'HEAD', 'OPTIONS']

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const method = (init?.method ?? 'GET').toUpperCase()
  const token = SAFE_METHODS.includes(method) ? undefined : csrfToken()
  const response = await fetch(path, {
    ...init,
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
      ...(token ? { 'X-XSRF-TOKEN': decodeURIComponent(token) } : {}),
      ...init?.headers,
    },
  })
  if (!response.ok) {
    const problem = await response.json().catch(() => ({ status: response.status }))
    throw new ApiError({ ...problem, status: response.status })
  }
  return response.status === 204 ? (undefined as T) : response.json()
}

export interface Me {
  authenticated: boolean
  username?: string
  name?: string
  email?: string
  roles: string[]
}

export const authApi = {
  me: () => request<Me>('/api/me'),
  /** Ends our session, then sends the browser to Keycloak to end the SSO session too. */
  async logout(): Promise<void> {
    const { logoutUrl } = await request<{ logoutUrl: string }>('/logout', { method: 'POST' })
    window.location.assign(logoutUrl)
  },
}

/** Full-page navigation: the gateway redirects to Keycloak's login page. */
export const LOGIN_URL = '/oauth2/authorization/keycloak'

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
