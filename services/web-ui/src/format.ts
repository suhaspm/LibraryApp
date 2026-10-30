import type { MediaType } from './api/types'

export const TYPE_LABELS: Record<MediaType, string> = {
  BOOK: 'Book',
  EBOOK: 'eBook',
  AUDIOBOOK: 'Audiobook',
  DVD: 'DVD',
  BLURAY: 'Blu-ray',
  MUSIC_CD: 'Music CD',
  VIDEO_GAME: 'Video game',
  BOARD_GAME: 'Board game',
}

const euro = new Intl.NumberFormat('de-DE', { style: 'currency', currency: 'EUR' })

export const formatPrice = (value: number | null | undefined) => (value == null ? '–' : euro.format(value))

export const rentalLabel = (fee: number) => (fee === 0 ? 'Free to borrow' : `${euro.format(fee)} per loan`)

/** "runtimeMinutes" -> "Runtime minutes" */
export const humanize = (key: string) =>
  key.replace(/([a-z])([A-Z])/g, '$1 $2').replace(/^./, (c) => c.toUpperCase()).replace(/ ([A-Z])/g, (_, c) => ` ${c.toLowerCase()}`)
