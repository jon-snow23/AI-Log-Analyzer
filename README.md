# AI Log Analyzer

A full-stack log analysis application for uploading or pasting application logs, extracting structured events, surfacing recurring failures, and generating a clear root-cause summary with recommended next steps.

The project is built as a monorepo with a React frontend, a Spring Boot backend, and PostgreSQL for persistence.

## Overview

AI Log Analyzer is designed for production-debugging workflows where raw logs are noisy, repetitive, and difficult to triage quickly. The application ingests plain-text logs, normalizes recurring messages, detects common failure patterns, and presents the result in a dashboard that highlights:

- overall log volume and severity counts
- dominant recurring errors
- impacted services
- detected issues with evidence and recommendations
- a primary root-cause candidate with confidence scoring

## Features

- Upload `.log` and `.txt` files for analysis
- Paste raw log text directly into the UI
- Parse common timestamped log formats with fallback handling for unstructured lines
- Count `INFO`, `WARN`, and `ERROR` events
- Group recurring failures by normalized error message
- Surface top failing services and most frequent errors
- Detect rule-based incident patterns such as timeouts, connection failures, auth errors, and repeated failures
- Generate a deterministic root-cause summary with confidence scoring
- Browse parsed log entries with level, service, and keyword filters
- Export the analysis summary and detected findings as JSON
- Optionally generate AI-powered recommendations for detected issues

## Architecture

```text
+----------------------+        +-----------------------+        +------------------+
| React + Vite UI      | -----> | Spring Boot API       | -----> | PostgreSQL       |
| Upload / Dashboard   |        | Parser + Analyzer     |        | Analysis storage |
| Charts / Filters     | <----- | JPA / REST / Export   | <----- | Log entries      |
+----------------------+        +-----------------------+        +------------------+
```

### Backend Responsibilities

- accept uploaded files or pasted text
- parse raw log lines into structured entries
- normalize noisy messages for grouping
- compute severity counts, service impact, and recurring failures
- evaluate rule-based root-cause candidates
- persist analyses, issues, and log entries
- expose REST endpoints for summaries, issues, entries, and exports

### Frontend Responsibilities

- collect uploaded files and pasted log text
- trigger analysis requests
- display summary metrics and charts
- show root-cause findings and recommended actions
- browse raw parsed entries with filters and pagination

## Tech Stack

### Backend

- Java 17
- Spring Boot 3
- Spring Web
- Spring Data JPA
- Bean Validation
- Maven
- Lombok
- springdoc OpenAPI

### Frontend

- React 18
- Vite
- React Router
- Recharts
- Framer Motion

### Database and Tooling

- PostgreSQL
- Docker
- Docker Compose
- JUnit 5
- Mockito
- Vitest
- Testing Library

## Repository Structure

```text
.
├── backend
│   ├── src
│   └── pom.xml
├── frontend
│   ├── src
│   ├── public
│   └── package.json
├── sample-logs
├── docker-compose.yml
└── README.md
```

## Getting Started

### Prerequisites

- Java 17+
- Node.js 18+ or 20+
- npm
- PostgreSQL 16+ if running without Docker
- Docker and Docker Compose if running the containerized setup

## Running Locally

### Option 1: Docker Compose

Start the full stack:

```bash
docker compose up --build
```

Open:

- Frontend: [http://localhost:5173](http://localhost:5173)
- Backend API: [http://localhost:8080/api](http://localhost:8080/api)
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

To enable AI-powered recommendations with OpenRouter:

```bash
export AI_ENABLED=true
export AI_PROVIDER=openrouter
export OPENROUTER_API_KEY="your_api_key_here"
docker compose up --build
```

### Option 2: Run Services Separately

#### Start PostgreSQL

Use a local PostgreSQL instance with these defaults or provide your own values:

- database: `loganalyzer`
- username: `loganalyzer`
- password: `loganalyzer`

#### Start the Backend

```bash
cd backend
mvn spring-boot:run
```

#### Start the Frontend

```bash
cd frontend
npm install
npm run dev
```

## Environment Variables

The backend reads the following environment variables:

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/loganalyzer
SPRING_DATASOURCE_USERNAME=loganalyzer
SPRING_DATASOURCE_PASSWORD=loganalyzer
SERVER_PORT=8080
AI_ENABLED=false
AI_PROVIDER=openrouter
OPENROUTER_API_KEY=
OPENROUTER_MODEL=openrouter/free
OPENROUTER_APP_NAME=AI Log Analyzer
GEMINI_API_KEY=
GEMINI_MODEL=gemini-2.0-flash
```

The frontend uses:

```env
VITE_API_BASE_URL=http://localhost:8080/api
```

## API Endpoints

### Health

- `GET /api/health`

### Analysis

- `POST /api/logs/upload`
- `POST /api/logs/analyze-text`
- `GET /api/logs/{analysisId}/summary`
- `GET /api/logs/{analysisId}/issues`
- `GET /api/logs/{analysisId}/entries?level=&service=&search=&page=0&size=25`
- `GET /api/logs/{analysisId}/export`

### Example: Analyze Raw Log Text

```bash
curl -X POST http://localhost:8080/api/logs/analyze-text \
  -H "Content-Type: application/json" \
  -d '{"rawLogs":"2026-04-04 10:01:15 ERROR PaymentService - Timeout while connecting to payment provider"}'
```

### Example: Upload a File

```bash
curl -X POST http://localhost:8080/api/logs/upload \
  -F "file=@sample-logs/payment-timeout.log"
```

## Sample Logs

The repository includes example incidents in [sample-logs](/Users/vipinnirmal/Desktop/log_reader/sample-logs):

- `payment-timeout.log`
- `db-connection-failure.log`
- `mixed-microservices-failure.log`

These are useful for quick demos, local testing, and verifying parser behavior.

## How Root-Cause Detection Works

The analyzer is deterministic and rule-based.

1. Raw log lines are parsed into structured entries where possible.
2. Messages are normalized to reduce noise from IDs and volatile values.
3. The system computes severity counts, dominant services, and recurring normalized errors.
4. Root-cause rules scan for known failure signatures such as:
   - timeout
   - connection refused
   - null pointer exceptions
   - out of memory
   - too many connections
   - 401 and 403 responses
   - access denied
   - broken pipe
5. Confidence increases when patterns repeat frequently.
6. Additional anomaly rules flag elevated error ratios and repeated identical failures.
7. The highest-confidence candidate becomes the primary root-cause summary.

## Testing

### Backend

```bash
cd backend
mvn test
```

### Frontend

```bash
cd frontend
npm install
npm test
```

## Deployment Notes

This project can be deployed in multiple ways depending on the expected traffic and upload size.

### Recommended Split

- Frontend: Cloudflare Pages or Cloudflare Workers static hosting
- Backend: Java host such as Render, Oracle Cloud VM, Railway, or another VM/container platform
- Database: PostgreSQL, such as Neon or a self-managed instance

### Important Note on Large File Uploads

For production-scale log ingestion, large uploads should be processed server-side through a proper multipart upload flow with asynchronous processing or background jobs. Free-tier platforms with cold starts can be limiting for large-file uploads.

## Limitations

- Optimized for plain-text logs rather than fully structured JSON logs
- Multiline stack traces are not reconstructed yet
- Authentication and multi-user isolation are not implemented
- Root-cause detection is rule-based and explainable, not a full incident-correlation engine
- Very large log files may require a more robust upload and processing architecture

## Roadmap

- JSON log support
- Multiline stack-trace stitching
- Better large-file upload architecture
- Async processing and job polling
- Historical comparison across analyses
- Richer exports and downloadable reports
- Stronger deployment story for large production logs

## License

Add a license file if you plan to open-source or distribute the project publicly.
