import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render } from '@testing-library/react'
import { RouterProvider, createMemoryRouter } from 'react-router'
import { routes } from './router'

export function renderAt(path: string) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  const router = createMemoryRouter(routes, { initialEntries: [path] })
  render(
    <QueryClientProvider client={client}>
      <RouterProvider router={router} />
    </QueryClientProvider>,
  )
  return router
}

export function jsonResponse(body: unknown, status = 200) {
  return new Response(JSON.stringify(body), { status, headers: { 'Content-Type': 'application/json' } })
}

export type SignedInAs = 'anonymous' | 'member' | 'librarian'

const ME = {
  anonymous: { authenticated: false, roles: [] },
  member: { authenticated: true, username: 'member1', name: 'Mia Member', roles: ['MEMBER'] },
  librarian: { authenticated: true, username: 'librarian1', name: 'Lars Librarian', roles: ['LIBRARIAN', 'MEMBER'] },
}

/**
 * Replaces fetch for one test; returns the mock so tests can inspect the requests made.
 * /api/me answers as the given user; everything else goes to the handler.
 */
export function mockFetch(handler: (url: string, init?: RequestInit) => Response, as: SignedInAs = 'anonymous') {
  const fn = vi.fn((input: RequestInfo | URL, init?: RequestInit) => {
    const url = String(input)
    return Promise.resolve(url === '/api/me' ? jsonResponse(ME[as]) : handler(url, init))
  })
  vi.stubGlobal('fetch', fn)
  return fn
}
