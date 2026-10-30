import { NavLink, Outlet } from 'react-router'
import { LOGIN_URL, authApi } from '../api/client'
import { useMe } from '../auth'

const navClass = ({ isActive }: { isActive: boolean }) => (isActive ? 'nav-link active' : 'nav-link')

export function Layout() {
  const { me, isLibrarian } = useMe()
  return (
    <>
      <header className="site-header">
        <div className="container header-inner">
          <NavLink to="/" className="brand">
            <img src="/favicon.svg" alt="" width={28} height={28} />
            <span>MediaHub</span>
          </NavLink>
          <nav aria-label="Main">
            <NavLink to="/" end className={navClass}>Catalog</NavLink>
            <NavLink to="/rentals" className={navClass}>My rentals</NavLink>
            <NavLink to="/payments" className={navClass}>Payments due</NavLink>
            {isLibrarian && <NavLink to="/media/new" className={navClass}>Add media</NavLink>}
          </nav>
          {me.authenticated ? (
            <div className="user-menu">
              <span className="user-name" title={me.email}>
                {me.name ?? me.username}
                {isLibrarian && <span className="badge role">Librarian</span>}
              </span>
              <button className="button secondary small" onClick={() => authApi.logout()}>
                Sign out
              </button>
            </div>
          ) : (
            <a href={LOGIN_URL} className="button secondary small">Sign in</a>
          )}
        </div>
      </header>
      <main className="container">
        <Outlet />
      </main>
      <footer className="container footer">MediaHub · a microservices learning project</footer>
    </>
  )
}
