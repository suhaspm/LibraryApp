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
