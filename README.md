# PostMind

A full-stack automation tool that fetches trending topics from HackerNews, generates tweet drafts using an LLM, and lets you approve or reject them before publishing to Twitter/X.

## How it works

```
HackerNews trends → LLM generates tweet → You review → Twitter publishes
```

1. Every hour the scheduler fetches top HackerNews stories
2. The top 5 by score are sent to Groq (Llama 3) to generate tweet drafts
3. You get a Telegram notification telling you how many drafts are ready
4. You approve or reject each one in the dashboard
5. Approved posts are published to your Twitter account within 5 minutes

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot 3.3, Spring Data JPA |
| Database | PostgreSQL + Flyway migrations |
| LLM | Groq API (Llama 3.1 — free tier) |
| Trend source | HackerNews Firebase API |
| Publishing | Twitter/X API v2 (OAuth 1.0a) |
| Notifications | Telegram Bot API |
| Frontend | React 18, React Router v6, Vite |

## Project Structure

```
postmind/
├── backend/
│   └── src/main/java/com/postmind/
│       ├── config/          # App properties, RestClient beans, CORS, Scheduling
│       ├── controller/      # REST endpoints
│       ├── service/         # Business logic
│       ├── repository/      # JPA repositories
│       ├── entity/          # JPA entities
│       ├── dto/             # Request/response objects
│       ├── enums/           # PostStatus, ApprovalDecision, TrendSource
│       └── exception/       # Global error handling
└── frontend/
    └── src/
        ├── api/             # Fetch wrappers
        ├── components/      # StatusBadge, PostCard
        └── pages/           # Dashboard, PostReview
```

## API Endpoints

```
GET  /api/trends               List all trends
GET  /api/trends/{id}          Single trend

GET  /api/posts                All posts (newest first)
GET  /api/posts?status=DRAFT   Filter by status
GET  /api/posts/{id}           Single post

POST /api/posts/{id}/approval  Approve or reject  { "decision": "APPROVED" | "REJECTED" }
GET  /api/posts/{id}/approval  Get decision for a post

POST /api/trigger/pipeline     Manually trigger fetch + generate
POST /api/trigger/publish      Manually trigger publish job
```

## Setup

### Prerequisites
- Java 21+
- PostgreSQL
- Node.js 18+

### 1. Database

```bash
psql postgres
CREATE DATABASE postmind;
\q
```

### 2. Environment variables

Create `backend/.env`:

```
DB_USERNAME=your_postgres_username
DB_PASSWORD=your_postgres_password
GROQ_API_KEY=gsk_...
TWITTER_API_KEY=...
TWITTER_API_SECRET=...
TWITTER_ACCESS_TOKEN=...
TWITTER_ACCESS_TOKEN_SECRET=...
TELEGRAM_BOT_TOKEN=...
TELEGRAM_CHAT_ID=...
```

Get your keys:
- **Groq**: console.groq.com → API Keys (free)
- **Twitter**: developer.twitter.com → Your App → Keys and Tokens
- **Telegram**: message @BotFather on Telegram → `/newbot` to get a token; send a message to your bot then call `https://api.telegram.org/bot<TOKEN>/getUpdates` to find your chat ID

### 3. Run the backend

```bash
cd backend
export $(cat .env | xargs) && mvn spring-boot:run
```

Flyway will automatically create the database tables on first run.

### 4. Run the frontend

```bash
cd frontend
npm install
npm run dev
```

Open [http://localhost:5173](http://localhost:5173)

### 5. Trigger the pipeline manually

```bash
curl -X POST http://localhost:8080/api/trigger/pipeline
```

## Architecture decisions

- **Flyway over Hibernate DDL** — schema changes are versioned and auditable
- **DTOs separate from entities** — API contract is decoupled from the database schema
- **Package-private `updateStatus()`** — only `ApprovalService` and `PublishingService` can drive post state transitions
- **Per-entry error handling in scheduler** — one failed trend never aborts the whole batch
- **Groq free tier** — swap in any OpenAI-compatible provider (OpenAI, Perplexity, Ollama) by changing the base URL and model in `application.yml`

## Post states

```
DRAFT → APPROVED → PUBLISHED
      → REJECTED
```

## Limitations & next steps

- Single-user only — multi-user would require a Twitter OAuth callback flow per user and encrypted token storage
- No authentication on the dashboard — add Spring Security for production
- HackerNews as sole trend source — pluggable architecture makes it easy to add more sources
