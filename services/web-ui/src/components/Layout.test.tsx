import { screen } from '@testing-library/react'
import { jsonResponse, mockFetch, renderAt } from '../test-utils'

afterEach(() => vi.unstubAllGlobals())

const emptyPage = () => jsonResponse({ items: [], page: 0, size: 12, totalItems: 0, totalPages: 0 })

test('anonymous visitors see a sign-in link and no librarian tools', async () => {
  mockFetch(emptyPage)
  renderAt('/')
  expect(await screen.findByRole('link', { name: 'Sign in' })).toHaveAttribute('href', '/oauth2/authorization/keycloak')
  expect(screen.queryByRole('link', { name: 'Add media' })).not.toBeInTheDocument()
})

test('members see their name but not librarian tools', async () => {
  mockFetch(emptyPage, 'member')
  renderAt('/')
  expect(await screen.findByText('Mia Member')).toBeInTheDocument()
  expect(screen.getByRole('button', { name: 'Sign out' })).toBeInTheDocument()
  expect(screen.queryByRole('link', { name: 'Add media' })).not.toBeInTheDocument()
})

test('librarians get the Add media tool', async () => {
  mockFetch(emptyPage, 'librarian')
  renderAt('/')
  expect(await screen.findByRole('link', { name: 'Add media' })).toBeInTheDocument()
  expect(screen.getByText('Librarian')).toBeInTheDocument()
})
