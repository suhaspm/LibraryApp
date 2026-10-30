import { ApiError } from '../api/client'

export function ErrorMessage({ error }: { error: unknown }) {
  const message =
    error instanceof ApiError
      ? `${error.message} (HTTP ${error.status})`
      : 'Could not reach the catalog service. Is it running (and the port-forward open)?'
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
