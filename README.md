# CommerceAI

AI-native e-commerce platform — a premium shopping experience powered by conversational AI for customers and business teams.

## Stack

| Layer    | Technology                          |
| -------- | ----------------------------------- |
| Frontend | Next.js, TypeScript, Tailwind CSS   |
| Backend  | Kotlin, Spring Boot                 |
| Database | PostgreSQL                          |
| AI       | OpenAI API (future phases)          |

## Project Structure

```
commerceAI/
├── frontend/          # Next.js customer & admin UI
├── backend/           # Spring Boot modular monolith
├── docker-compose.yml # Local PostgreSQL
├── .env.example       # Environment variable template
└── README.md
```

### Frontend (`frontend/`)

```
src/
├── app/           # Next.js App Router pages
├── components/    # Reusable UI components
├── lib/           # API clients and utilities
└── types/         # Shared TypeScript types
```

### Backend (`backend/`)

```
src/main/kotlin/com/commerceai/
├── config/        # Cross-cutting configuration
├── health/        # Health check endpoints
└── ...            # Feature modules added per phase
```

## Prerequisites

- [Node.js](https://nodejs.org/) 20+
- [Java](https://adoptium.net/) 21+
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (for PostgreSQL)

## Quick Start

### 1. Clone and configure environment

```bash
cp .env.example .env
cp frontend/.env.example frontend/.env.local
cp backend/.env.example backend/.env
```

### 2. Start PostgreSQL

```bash
docker compose up -d
```

### 3. Install dependencies

```bash
npm install          # root dev tooling (concurrently)
cd frontend && npm install
```

### 4. Run frontend and backend together

From the project root:

```bash
npm run dev
```

| Service  | URL                   |
| -------- | --------------------- |
| Frontend | http://localhost:3000 |
| Backend  | http://localhost:8080 |
| Health   | http://localhost:8080/api/health |

### Run services individually

```bash
# Frontend only
npm run dev:frontend

# Backend only (requires PostgreSQL)
npm run dev:backend
```

On macOS/Linux, run the backend with:

```bash
cd backend && ./gradlew bootRun --args='--spring.profiles.active=local'
```

## Environment Variables

| Variable                    | Description              | Default                                              |
| --------------------------- | ------------------------ | ---------------------------------------------------- |
| `POSTGRES_USER`             | Database user            | `commerceai`                                         |
| `POSTGRES_PASSWORD`         | Database password        | `commerceai`                                         |
| `POSTGRES_DB`               | Database name            | `commerceai`                                         |
| `SPRING_DATASOURCE_URL`     | JDBC connection string   | `jdbc:postgresql://localhost:5432/commerceai`        |
| `SPRING_DATASOURCE_USERNAME`| DB username for backend  | `commerceai`                                         |
| `SPRING_DATASOURCE_PASSWORD`| DB password for backend  | `commerceai`                                         |
| `SERVER_PORT`               | Backend HTTP port        | `8080`                                               |
| `NEXT_PUBLIC_API_URL`       | Backend URL for frontend | `http://localhost:8080`                              |
| `JWT_SECRET`                | JWT signing key (32+ chars) | dev default in `application.yml`                  |
| `JWT_EXPIRATION_MS`         | Token lifetime in ms     | `604800000` (7 days)                                 |

## Authentication (Phase 1)

| Endpoint | Method | Auth |
|----------|--------|------|
| `/api/auth/register` | POST | Public |
| `/api/auth/login` | POST | Public |
| `/api/auth/me` | GET | Bearer JWT |
| `/api/admin/**` | * | Admin role |

**Seeded admin** (local profile only): `admin@commerceai.com` / `admin12345`

**Frontend routes:** `/login`, `/register`, `/dashboard` (protected), `/admin` (admin only)

## Product Catalog (Phase 2)

| Endpoint | Method | Access |
|----------|--------|--------|
| `/api/categories` | GET | Public |
| `/api/categories/{slug}` | GET | Public |
| `/api/products` | GET | Public (pagination, category filter, search) |
| `/api/products/{slug}` | GET | Public |
| `/api/admin/categories` | POST/PUT/DELETE | Admin |
| `/api/admin/products` | POST/PUT/DELETE | Admin |

**Local seed:** 8 categories and 100 products (picsum.photos images) on `local` profile startup.

**Frontend routes:** `/`, `/products`, `/products/[slug]`, `/categories/[slug]`

## Development

```bash
# Backend tests
cd backend && ./gradlew test        # macOS/Linux
cd backend && .\gradlew.bat test      # Windows

# Frontend lint
npm run lint
```

## Roadmap

Phase 0 establishes the monorepo foundation. Upcoming MVP phases:

- Authentication
- Product catalog
- Shopping cart
- AI shopping assistant
- Admin dashboard
- AI business assistant

## License

Private — portfolio project.
