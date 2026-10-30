import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Link, useNavigate, useParams } from 'react-router'
import { catalogApi } from '../api/client'
import { ErrorMessage } from '../components/ErrorMessage'
import { TYPE_LABELS, formatPrice, humanize, rentalLabel } from '../format'

export function MediaDetailPage() {
  const { id = '' } = useParams()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const { data: media, error, isPending } = useQuery({ queryKey: ['media', id], queryFn: () => catalogApi.get(id) })

  const remove = useMutation({
    mutationFn: () => catalogApi.remove(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['media'] })
      navigate('/')
    },
  })

  if (isPending) return <p className="muted">Loading…</p>
  if (error) return <ErrorMessage error={error} />

  const attributes = Object.entries(media.attributes ?? {})
  return (
    <article className="detail">
      <Link to="/" className="back">← Back to catalog</Link>
      <span className={`badge type-${media.type.toLowerCase()}`}>{TYPE_LABELS[media.type]}</span>
      <h1>{media.title}</h1>
      <p className="lead">
        {media.creators.join(', ')}
        {media.publicationYear ? ` · ${media.publicationYear}` : ''}
      </p>
      {media.description && <p>{media.description}</p>}

      <div className="detail-grid">
        <dl className="card facts">
          <dt>Identifier</dt>
          <dd>{media.identifier ?? '–'}</dd>
          <dt>Language</dt>
          <dd>{media.language?.toUpperCase() ?? '–'}</dd>
          <dt>Genres</dt>
          <dd>{media.genres.length ? media.genres.join(', ') : '–'}</dd>
          {attributes.map(([key, value]) => (
            <div key={key} className="fact-row">
              <dt>{humanize(key)}</dt>
              <dd>{String(value)}</dd>
            </div>
          ))}
        </dl>

        <aside className="card actions">
          <p className="fee">{rentalLabel(media.rentalFee)}</p>
          <button className="button" disabled title="Rentals arrive in week 5">
            Rent
          </button>
          <p className="muted small">Buy for {formatPrice(media.purchasePrice)}</p>
          <button className="button secondary" disabled title="Purchases arrive in week 12">
            Buy
          </button>
          <p className="muted small">Renting and buying come with the rental and purchase services.</p>
          <hr />
          <p className="muted small">Librarian actions</p>
          <Link to={`/media/${media.id}/edit`} className="button secondary">
            Edit
          </Link>
          <button
            className="button danger"
            disabled={remove.isPending}
            onClick={() => window.confirm(`Delete "${media.title}" from the catalog?`) && remove.mutate()}
          >
            Delete
          </button>
          {remove.error && <ErrorMessage error={remove.error} />}
        </aside>
      </div>
      <p className="muted small">
        Added {new Date(media.createdAt).toLocaleString()} · updated {new Date(media.updatedAt).toLocaleString()}
      </p>
    </article>
  )
}
