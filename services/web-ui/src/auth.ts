import { useQuery } from '@tanstack/react-query'
import { authApi, type Me } from './api/client'

const ANONYMOUS: Me = { authenticated: false, roles: [] }

/**
 * Who is signed in, according to the gateway. Used only to shape the UI (show a name, hide
 * buttons). Hiding a button is a convenience, not security: the services still check every request.
 */
export function useMe() {
  const { data } = useQuery({ queryKey: ['me'], queryFn: authApi.me, staleTime: 60_000 })
  const me: Me = data?.authenticated ? { ...data, roles: data.roles ?? [] } : ANONYMOUS
  return { me, isLibrarian: me.roles.includes('LIBRARIAN') }
}
