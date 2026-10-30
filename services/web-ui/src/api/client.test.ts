import { jsonResponse, mockFetch } from '../test-utils'
import { ApiError, catalogApi } from './client'

afterEach(() => vi.unstubAllGlobals())

test('builds the search query string', async () => {
  const fetch = mockFetch(() => jsonResponse({ items: [], page: 0, size: 12, totalItems: 0, totalPages: 0 }))
  await catalogApi.search({ q: 'kafka', type: 'BOOK', page: 2 })
  expect(fetch.mock.calls[0][0]).toBe('/api/v1/media?page=2&size=12&q=kafka&type=BOOK')
})

test('turns problem details into an ApiError', async () => {
  mockFetch(() => jsonResponse({ title: 'Conflict', detail: 'A media item with this identifier already exists' }, 409))
  const error = await catalogApi.get('x').catch((e: unknown) => e)
  expect(error).toBeInstanceOf(ApiError)
  expect(error).toMatchObject({ status: 409, message: 'A media item with this identifier already exists' })
})

test('handles empty 204 responses', async () => {
  mockFetch(() => new Response(null, { status: 204 }))
  await expect(catalogApi.remove('x')).resolves.toBeUndefined()
})

test('sends the CSRF token on writes but not on reads', async () => {
  document.cookie = 'XSRF-TOKEN=abc%2D123'
  const fetch = mockFetch(() => jsonResponse({ id: '1' }, 201))
  await catalogApi.create({ type: 'BOOK', title: 'T', creators: ['C'] })
  await catalogApi.get('1')
  const headers = (i: number) => fetch.mock.calls[i][1]!.headers as Record<string, string>
  expect(headers(0)['X-XSRF-TOKEN']).toBe('abc-123')
  expect(headers(1)['X-XSRF-TOKEN']).toBeUndefined()
  document.cookie = 'XSRF-TOKEN=; expires=Thu, 01 Jan 1970 00:00:00 GMT'
})
