// Mirrors catalog-service's MediaResponse/PageResponse. Week 3 replaces these hand-written
// types with ones generated from the service's OpenAPI document, so they can't drift.

export const MEDIA_TYPES = [
  'BOOK', 'EBOOK', 'AUDIOBOOK', 'DVD', 'BLURAY', 'MUSIC_CD', 'VIDEO_GAME', 'BOARD_GAME',
] as const

export type MediaType = (typeof MEDIA_TYPES)[number]

export interface Media {
  id: string
  type: MediaType
  title: string
  creators: string[]
  identifier: string | null
  language: string | null
  publicationYear: number | null
  genres: string[]
  description: string | null
  coverUrl: string | null
  attributes: Record<string, unknown>
  purchasePrice: number | null
  rentalFee: number
  createdAt: string
  updatedAt: string
}

export interface MediaRequest {
  type: MediaType
  title: string
  creators: string[]
  identifier?: string
  language?: string
  publicationYear?: number
  genres?: string[]
  description?: string
  coverUrl?: string
  attributes?: Record<string, unknown>
  purchasePrice?: number
  rentalFee?: number
}

export interface Page<T> {
  items: T[]
  page: number
  size: number
  totalItems: number
  totalPages: number
}

/** RFC 9457 problem details, as returned by every MediaHub service on errors. */
export interface Problem {
  status: number
  title?: string
  detail?: string
  errors?: { field?: string; defaultMessage?: string }[]
}
