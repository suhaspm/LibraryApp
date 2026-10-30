import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { jsonResponse, mockFetch, renderAt } from '../test-utils'

afterEach(() => vi.unstubAllGlobals())

test('sends a well-formed request and opens the new item', async () => {
  const fetch = mockFetch((_url, init) =>
    init?.method === 'POST'
      ? jsonResponse({ id: 'new-1', ...JSON.parse(String(init.body)) }, 201)
      : jsonResponse({ id: 'new-1', type: 'BOOK', title: 'Der Process', creators: ['Franz Kafka'], genres: [], attributes: {}, rentalFee: 0, createdAt: '2026-01-01T00:00:00Z', updatedAt: '2026-01-01T00:00:00Z' }),
  )
  const router = renderAt('/media/new')

  await userEvent.type(screen.getByLabelText(/^Title/), 'Der Process')
  await userEvent.type(screen.getByLabelText(/^Creators/), 'Franz Kafka, Max Brod')
  await userEvent.click(screen.getByRole('button', { name: 'Add detail' }))
  await userEvent.type(screen.getByLabelText('Detail name'), 'pages')
  await userEvent.type(screen.getByLabelText('Detail value'), '288')
  await userEvent.click(screen.getByRole('button', { name: 'Add to catalog' }))

  const [, init] = fetch.mock.calls.find(([, i]) => i?.method === 'POST')!
  expect(JSON.parse(String(init!.body))).toMatchObject({
    type: 'BOOK', title: 'Der Process', creators: ['Franz Kafka', 'Max Brod'], attributes: { pages: 288 }, rentalFee: 0,
  })
  expect(await screen.findByRole('heading', { name: 'Der Process' })).toBeInTheDocument()
  expect(router.state.location.pathname).toBe('/media/new-1')
})

test('shows field errors returned by the service', async () => {
  mockFetch(() =>
    jsonResponse({ title: 'Bad Request', detail: 'Invalid request content.', errors: [{ field: 'language', defaultMessage: 'size must be between 2 and 2' }] }, 400),
  )
  renderAt('/media/new')

  await userEvent.type(screen.getByLabelText(/^Title/), 'X')
  await userEvent.type(screen.getByLabelText(/^Creators/), 'Y')
  await userEvent.type(screen.getByLabelText(/^Language/), 'd')
  await userEvent.click(screen.getByRole('button', { name: 'Add to catalog' }))

  const alert = await screen.findByRole('alert')
  expect(alert).toHaveTextContent('Invalid request content. (HTTP 400)')
  expect(alert).toHaveTextContent('language: size must be between 2 and 2')
})
