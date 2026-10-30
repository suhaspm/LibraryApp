import { Link } from 'react-router'
import type { Media } from '../api/types'
import { TYPE_LABELS, rentalLabel } from '../format'

export function MediaCard({ media }: { media: Media }) {
  return (
    <li className="card media-card">
      <Link to={`/media/${media.id}`} className="card-link">
        <span className={`badge type-${media.type.toLowerCase()}`}>{TYPE_LABELS[media.type]}</span>
        <h3>{media.title}</h3>
        <p className="muted">
          {media.creators.join(', ')}
          {media.publicationYear ? ` · ${media.publicationYear}` : ''}
        </p>
        <p className="fee">{rentalLabel(media.rentalFee)}</p>
      </Link>
    </li>
  )
}
