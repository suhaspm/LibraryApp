import { ApiError, LOGIN_URL } from '../api/client'

export function ErrorMessage({ error }: { error: unknown }) {
  // 401: we don't know who you are. 403: we know, and you may not do this.
  if (error instanceof ApiError && error.status === 401) {
    return (
      <div role="alert" className="alert">
        <p>
          You need to be signed in to do this. <a href={LOGIN_URL}>Sign in</a>
        </p>
      </div>
    )
  }
  if (error instanceof ApiError && error.status === 403) {
    return (
      <div role="alert" className="alert">
        <p>Your account is not allowed to do this. Only librarians can change the catalog.</p>
      </div>
    )
  }
  const message =
    error instanceof ApiError
      ? `${error.message} (HTTP ${error.status})`
      : 'Could not reach MediaHub. Is the backend running?'
  const fieldErrors = error instanceof ApiError ? (error.problem.errors ?? []) : []
  return (
    <div role="alert" className="alert">
      <p>{message}</p>
      {fieldErrors.length > 0 && (
        <ul>
          {fieldErrors.map((e, i) => (
            <li key={i}>
              <strong>{e.field}</strong>: {e.defaultMessage}
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
