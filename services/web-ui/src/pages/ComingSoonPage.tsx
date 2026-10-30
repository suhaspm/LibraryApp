import type { ReactNode } from 'react'

export function ComingSoonPage({ title, week, children }: { title: string; week: number; children: ReactNode }) {
  return (
    <section className="coming-soon">
      <h1>{title}</h1>
      <p className="badge">Arrives in week {week}</p>
      <p>{children}</p>
    </section>
  )
}
