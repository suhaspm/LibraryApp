import { NavLink, Outlet } from 'react-router'

const navClass = ({ isActive }: { isActive: boolean }) => (isActive ? 'nav-link active' : 'nav-link')

export function Layout() {
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
            <NavLink to="/media/new" className={navClass}>Add media</NavLink>
          </nav>
          <NavLink to="/login" className="button secondary small">Sign in</NavLink>
        </div>
      </header>
      <main className="container">
        <Outlet />
      </main>
      <footer className="container footer">MediaHub · a microservices learning project</footer>
    </>
  )
}
