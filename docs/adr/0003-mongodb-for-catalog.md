# 0003: MongoDB for the catalog, Postgres for transactional services

**Status:** Accepted

## Context
Catalog entries vary by media type. A book has pages and an ISBN; a game has a platform and PEGI
rating; a DVD has a runtime and region. Reads far outnumber writes, and every read wants the
whole item. Rentals, payments and stock counts are the opposite: small rows, invariants such as
"never lend one copy twice", and money that must add up.

## Decision
- **catalog-service → MongoDB.** Shared fields are typed; type-specific fields go in an `attributes` map. A text index serves basic search. `@Version` gives optimistic locking.
- **inventory, rental, billing, purchase → PostgreSQL** (one CloudNativePG cluster, one database and user per service).
- Each service owns its data. No service reads another's database; data crosses boundaries only through APIs and events.

## Consequences
- Two database engines to operate. That is accepted on purpose, as practice in polyglot persistence.
- No cross-database joins: the "available media" view is built by inventory-service from catalog events (week 5). That is eventual consistency, and the UI must tolerate a short lag.
- Mongo's text search is basic (no fuzzy matching). OpenSearch is a stretch goal if search quality matters.
