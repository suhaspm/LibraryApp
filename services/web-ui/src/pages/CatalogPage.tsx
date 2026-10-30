import { keepPreviousData, useQuery } from '@tanstack/react-query'
import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router'
import { catalogApi } from '../api/client'
import { MEDIA_TYPES, type MediaType } from '../api/types'
import { ErrorMessage } from '../components/ErrorMessage'
import { MediaCard } from '../components/MediaCard'
import { TYPE_LABELS } from '../format'

export function CatalogPage() {
  // Search state lives in the URL, so results can be bookmarked and the back button works.
  const [params, setParams] = useSearchParams()
  const q = params.get('q') ?? ''
  const type = (params.get('type') as MediaType | null) ?? undefined
  const page = Number(params.get('page') ?? 0)

  const [input, setInput] = useState(q)
  useEffect(() => setInput(q), [q])

  // Wait until typing pauses before querying, instead of one request per keystroke.
  useEffect(() => {
    if (input === q) return
    const timer = setTimeout(() => update({ q: input || undefined, page: undefined }), 300)
    return () => clearTimeout(timer)
  })

  const { data, error, isPending, isFetching } = useQuery({
    queryKey: ['media', { q, type, page }],
    queryFn: () => catalogApi.search({ q, type, page }),
    placeholderData: keepPreviousData,
  })

  function update(changes: Record<string, string | undefined>) {
    const next = new URLSearchParams(params)
    for (const [key, value] of Object.entries(changes)) {
      if (value === undefined) next.delete(key)
      else next.set(key, value)
    }
    setParams(next)
  }

  return (
    <section>
      <h1>Catalog</h1>
      <div className="search-bar">
        <input
          type="search"
          placeholder="Search titles, authors, descriptions…"
          aria-label="Search the catalog"
          value={input}
          onChange={(e) => setInput(e.target.value)}
        />
      </div>
      <div className="chips" role="group" aria-label="Filter by type">
        <button className={type ? 'chip' : 'chip active'} onClick={() => update({ type: undefined, page: undefined })}>
          All
        </button>
        {MEDIA_TYPES.map((t) => (
          <button
            key={t}
            className={type === t ? 'chip active' : 'chip'}
            aria-pressed={type === t}
            onClick={() => update({ type: type === t ? undefined : t, page: undefined })}
          >
            {TYPE_LABELS[t]}
          </button>
        ))}
      </div>

      {error && <ErrorMessage error={error} />}
      {isPending && <p className="muted">Loading…</p>}

      {data && (
        <>
          <p className="muted result-count" aria-live="polite">
            {data.totalItems} {data.totalItems === 1 ? 'title' : 'titles'}
            {isFetching ? ' · updating…' : ''}
          </p>
          {data.items.length === 0 ? (
            <p className="empty">Nothing matches. Try another search or type.</p>
          ) : (
            <ul className="grid">
              {data.items.map((m) => (
                <MediaCard key={m.id} media={m} />
              ))}
            </ul>
          )}
          {data.totalPages > 1 && (
            <div className="pager">
              <button className="button secondary" disabled={page === 0} onClick={() => update({ page: String(page - 1) })}>
                Previous
              </button>
              <span className="muted">
                Page {page + 1} of {data.totalPages}
              </span>
              <button
                className="button secondary"
                disabled={page + 1 >= data.totalPages}
                onClick={() => update({ page: String(page + 1) })}
              >
                Next
              </button>
            </div>
          )}
        </>
      )}
    </section>
  )
}
