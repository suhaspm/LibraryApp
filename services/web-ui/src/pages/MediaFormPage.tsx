import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState, type FormEvent } from 'react'
import { Link, useNavigate, useParams } from 'react-router'
import { catalogApi } from '../api/client'
import { LOGIN_URL } from '../api/client'
import { MEDIA_TYPES, type Media, type MediaRequest, type MediaType } from '../api/types'
import { useMe } from '../auth'
import { ErrorMessage } from '../components/ErrorMessage'
import { TYPE_LABELS } from '../format'

/** Create when there is no :id in the URL, edit otherwise. */
export function MediaFormPage() {
  const { id } = useParams()
  const { me, isLibrarian } = useMe()
  const existing = useQuery({ queryKey: ['media', id], queryFn: () => catalogApi.get(id!), enabled: !!id })

  if (!isLibrarian) {
    return (
      <section className="coming-soon">
        <h1>Librarians only</h1>
        {me.authenticated ? (
          <p>Your account can browse and borrow, but only librarians can change the catalog.</p>
        ) : (
          <p>
            <a href={LOGIN_URL}>Sign in</a> with a librarian account to add or edit media.
          </p>
        )}
      </section>
    )
  }
  if (id && existing.isPending) return <p className="muted">Loading…</p>
  if (id && existing.error) return <ErrorMessage error={existing.error} />
  // key: remount the form when the loaded item changes, so its initial state is correct.
  return <MediaForm key={existing.data?.id ?? 'new'} initial={existing.data} />
}

type Row = { key: string; value: string }

const list = (text: string) => text.split(',').map((s) => s.trim()).filter(Boolean)
const optionalNumber = (text: string) => (text.trim() === '' ? undefined : Number(text))
// "288" becomes a number, "Nintendo Switch" stays a string.
const parseValue = (text: string) => (text.trim() !== '' && !Number.isNaN(Number(text)) ? Number(text) : text)

function MediaForm({ initial }: { initial?: Media }) {
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  const [type, setType] = useState<MediaType>(initial?.type ?? 'BOOK')
  const [title, setTitle] = useState(initial?.title ?? '')
  const [creators, setCreators] = useState(initial?.creators.join(', ') ?? '')
  const [identifier, setIdentifier] = useState(initial?.identifier ?? '')
  const [language, setLanguage] = useState(initial?.language ?? '')
  const [year, setYear] = useState(initial?.publicationYear?.toString() ?? '')
  const [genres, setGenres] = useState(initial?.genres.join(', ') ?? '')
  const [description, setDescription] = useState(initial?.description ?? '')
  const [rentalFee, setRentalFee] = useState(initial?.rentalFee?.toString() ?? '0')
  const [purchasePrice, setPurchasePrice] = useState(initial?.purchasePrice?.toString() ?? '')
  const [rows, setRows] = useState<Row[]>(
    Object.entries(initial?.attributes ?? {}).map(([key, value]) => ({ key, value: String(value) })),
  )

  const save = useMutation({
    mutationFn: (body: MediaRequest) => (initial ? catalogApi.update(initial.id, body) : catalogApi.create(body)),
    onSuccess: (saved) => {
      queryClient.invalidateQueries({ queryKey: ['media'] })
      navigate(`/media/${saved.id}`)
    },
  })

  function submit(e: FormEvent) {
    e.preventDefault()
    save.mutate({
      type,
      title: title.trim(),
      creators: list(creators),
      identifier: identifier.trim() || undefined,
      language: language.trim().toLowerCase() || undefined,
      publicationYear: optionalNumber(year),
      genres: list(genres),
      description: description.trim() || undefined,
      rentalFee: optionalNumber(rentalFee),
      purchasePrice: optionalNumber(purchasePrice),
      attributes: Object.fromEntries(rows.filter((r) => r.key.trim()).map((r) => [r.key.trim(), parseValue(r.value)])),
    })
  }

  const setRow = (i: number, change: Partial<Row>) => setRows(rows.map((r, j) => (j === i ? { ...r, ...change } : r)))

  return (
    <section>
      <Link to={initial ? `/media/${initial.id}` : '/'} className="back">← Cancel</Link>
      <h1>{initial ? `Edit "${initial.title}"` : 'Add media'}</h1>
      <form className="card form" onSubmit={submit}>
        <label>
          Type
          <select value={type} onChange={(e) => setType(e.target.value as MediaType)}>
            {MEDIA_TYPES.map((t) => (
              <option key={t} value={t}>{TYPE_LABELS[t]}</option>
            ))}
          </select>
        </label>
        <label>
          Title *
          <input value={title} onChange={(e) => setTitle(e.target.value)} required maxLength={300} />
        </label>
        <label>
          <span>Creators * <span className="hint">comma-separated: authors, directors, studios…</span></span>
          <input value={creators} onChange={(e) => setCreators(e.target.value)} required />
        </label>
        <div className="row">
          <label>
            ISBN / EAN
            <input value={identifier} onChange={(e) => setIdentifier(e.target.value)} maxLength={32} />
          </label>
          <label>
            <span>Language <span className="hint">2 letters</span></span>
            <input value={language} onChange={(e) => setLanguage(e.target.value)} maxLength={2} placeholder="de" />
          </label>
          <label>
            Year
            <input value={year} onChange={(e) => setYear(e.target.value)} inputMode="numeric" />
          </label>
        </div>
        <label>
          <span>Genres <span className="hint">comma-separated</span></span>
          <input value={genres} onChange={(e) => setGenres(e.target.value)} />
        </label>
        <label>
          Description
          <textarea value={description} onChange={(e) => setDescription(e.target.value)} rows={3} maxLength={5000} />
        </label>
        <div className="row">
          <label>
            Rental fee (€)
            <input value={rentalFee} onChange={(e) => setRentalFee(e.target.value)} inputMode="decimal" />
          </label>
          <label>
            Purchase price (€)
            <input value={purchasePrice} onChange={(e) => setPurchasePrice(e.target.value)} inputMode="decimal" />
          </label>
        </div>

        <fieldset>
          <legend>Type-specific details <span className="hint">e.g. pages, runtimeMinutes, platform</span></legend>
          {rows.map((row, i) => (
            <div className="row attribute" key={i}>
              <input aria-label="Detail name" placeholder="name" value={row.key} onChange={(e) => setRow(i, { key: e.target.value })} />
              <input aria-label="Detail value" placeholder="value" value={row.value} onChange={(e) => setRow(i, { value: e.target.value })} />
              <button type="button" className="button secondary small" onClick={() => setRows(rows.filter((_, j) => j !== i))}>
                Remove
              </button>
            </div>
          ))}
          <button type="button" className="button secondary small" onClick={() => setRows([...rows, { key: '', value: '' }])}>
            Add detail
          </button>
        </fieldset>

        {save.error && <ErrorMessage error={save.error} />}
        <button className="button" type="submit" disabled={save.isPending}>
          {save.isPending ? 'Saving…' : initial ? 'Save changes' : 'Add to catalog'}
        </button>
      </form>
    </section>
  )
}
