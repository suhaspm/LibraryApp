import { createBrowserRouter } from 'react-router'
import { Layout } from './components/Layout'
import { CatalogPage } from './pages/CatalogPage'
import { ComingSoonPage } from './pages/ComingSoonPage'
import { MediaDetailPage } from './pages/MediaDetailPage'
import { MediaFormPage } from './pages/MediaFormPage'
import { NotFoundPage } from './pages/NotFoundPage'

export const routes = [
  {
    element: <Layout />,
    children: [
      { index: true, element: <CatalogPage /> },
      { path: 'media/new', element: <MediaFormPage /> },
      { path: 'media/:id', element: <MediaDetailPage /> },
      { path: 'media/:id/edit', element: <MediaFormPage /> },
      {
        path: 'rentals',
        element: (
          <ComingSoonPage title="My rentals" week={5}>
            Everything you have borrowed, with due dates and renewals. Requests for the same title are
            handled strictly in the order they arrive, through Kafka.
          </ComingSoonPage>
        ),
      },
      {
        path: 'payments',
        element: (
          <ComingSoonPage title="Payments due" week={7}>
            Overdue fines and fees, calculated every night by billing-service, and paying them.
          </ComingSoonPage>
        ),
      },
      { path: '*', element: <NotFoundPage /> },
    ],
  },
]

export const router = createBrowserRouter(routes)
