import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import type { Media } from '../api/types'
import { jsonResponse, mockFetch, renderAt } from '../test-utils'

afterEach(() => vi.unstubAllGlobals())

const media = (title: string, type: Media['type']): Media => ({
  id: title, type, title, creators: ['Someone'], identifier: null, language: 'de', publicationYear: 2001,
  genres: [], description: null, coverUrl: null, attributes: {}, purchasePrice: null, rentalFee: 0,
  createdAt: '2026-01-01T00:00:00Z', updatedAt: '2026-01-01T00:00:00Z',
})

test('lists media and filters by type', async () => {
  const fetch = mockFetch((url) => {
    const items = url.includes('type=DVD') ? [media('Soul Kitchen', 'DVD')] : [media('Buddenbrooks', 'BOOK'), media('Soul Kitchen', 'DVD')]
    return jsonResponse({ items, page: 0, size: 12, totalItems: items.length, totalPages: 1 })
  })
  renderAt('/')

  expect(await screen.findByText('Buddenbrooks')).toBeInTheDocument()
  expect(screen.getByText('2 titles')).toBeInTheDocument()

  await userEvent.click(screen.getByRole('button', { name: 'DVD' }))

  expect(await screen.findByText('1 title')).toBeInTheDocument()
  expect(screen.queryByText('Buddenbrooks')).not.toBeInTheDocument()
  expect(fetch.mock.calls.at(-1)![0]).toContain('type=DVD')
})

test('explains when the service is unreachable', async () => {
  vi.stubGlobal('fetch', vi.fn(() => Promise.reject(new TypeError('Failed to fetch'))))
  renderAt('/')
  expect(await screen.findByRole('alert')).toHaveTextContent('Could not reach the catalog service')
})
