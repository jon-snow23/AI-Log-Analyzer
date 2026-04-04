# AI Log Analyzer for Production Debugging

A full-stack monorepo for uploading or pasting application logs, parsing them into structured entries, detecting recurring failures, flagging anomalies, and presenting likely root-cause summaries in a dashboard.

## Features

- Upload `.log` or `.txt` files for analysis
- Paste raw logs directly into the UI
- Parse common timestamped log formats with fallback handling for unstructured lines
- Count INFO, WARN, and ERROR entries
- Surface top recurring errors and top impacted services
- Detect rule-based anomalies such as repeated timeouts, connection failures, and elevated error ratios
- Produce a deterministic root-cause summary with confidence scoring and recommended next steps
- Browse parsed log entries with filters for level, service, and keyword text
- Export the analysis summary and issue findings as JSON
- Run the entire stack locally with Docker Compose

## Architecture

```text
+----------------------+        +-----------------------+        +------------------+
| React + Vite UI      | -----> | Spring Boot API       | -----> | PostgreSQL       |
| Upload / Dashboard   |        | Parser + Analyzer     |        | Analysis storage |
| Recharts / Filters   | <----- | JPA / REST / Export   | <----- | Log entries      |
+----------------------+        +-----------------------+        +------------------+
```

Backend layers:

- `controller`: REST endpoints
- `service`: orchestration for upload, parsing, persistence, and retrieval
- `repository`: Spring Data JPA repositories
- `model/entity`: persisted domain objects
- `dto`: API payloads
- `parser`: regex-based parsing and normalization
- `analyzer`: counting, pattern grouping, anomaly detection, and summary generation
- `rules`: maintainable keyword-based root-cause rules
- `mapper`: entity to response mapping
- `exception`: API error handling

## Tech Stack

- Backend: Java 17, Spring Boot 3, Maven, JPA, Validation, Lombok, OpenAPI
- Frontend: React 18, JavaScript, Vite, Recharts
- Database: PostgreSQL 16
- Testing: JUnit 5, Mockito, Spring MVC Test, Vitest, Testing Library
- Containerization: Docker, Docker Compose

## Project Structure

```text
.
├── backend
├── frontend
├── sample-logs
├── docker-compose.yml
└── README.md
```

## Local Setup

### Docker

Run the full stack:

```bash
docker compose up --build
```

To enable OpenRouter-powered finding recommendations with Docker Compose, set these variables before starting:

```bash
export AI_ENABLED=true
export AI_PROVIDER=openrouter
export OPENROUTER_API_KEY="your_api_key_here"
docker compose up --build
```

Open:

- Frontend: [http://localhost:5173](http://localhost:5173)
- Backend API: [http://localhost:8080/api](http://localhost:8080/api)
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

### Without Docker

Backend:

```bash
cd backend
mvn spring-boot:run
```

Frontend:

```bash
cd frontend
npm install
npm run dev
```

PostgreSQL defaults:

- DB: `loganalyzer`
- User: `loganalyzer`
- Password: `loganalyzer`

The backend reads these environment variables:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SERVER_PORT`
- `AI_ENABLED`
- `AI_PROVIDER`
- `OPENROUTER_API_KEY`
- `OPENROUTER_MODEL`

## API Endpoints

- `POST /api/logs/upload`
- `POST /api/logs/analyze-text`
- `GET /api/logs/{analysisId}/summary`
- `GET /api/logs/{analysisId}/issues`
- `GET /api/logs/{analysisId}/entries?level=&service=&search=&page=0&size=25`
- `GET /api/logs/{analysisId}/export`
- `GET /api/health`

Example analyze-text request:

```bash
curl -X POST http://localhost:8080/api/logs/analyze-text \
  -H "Content-Type: application/json" \
  -d '{"rawLogs":"2026-04-04 10:01:15 ERROR PaymentService - Timeout while connecting to payment provider"}'
```

Example upload request:

```bash
curl -X POST http://localhost:8080/api/logs/upload \
  -F "file=@sample-logs/payment-timeout.log"
```

## Sample Incidents

Bundled sample logs:

- `sample-logs/payment-timeout.log`
- `sample-logs/db-connection-failure.log`
- `sample-logs/mixed-microservices-failure.log`

The frontend includes quick-load buttons for these incidents.

## How Root Cause Detection Works

The analyzer is deterministic and rule-based.

1. Each line is parsed into a structured entry where possible.
2. Messages are normalized by removing volatile numbers and request identifiers to improve grouping.
3. The analyzer counts log levels, top services, and recurring normalized error messages.
4. Root-cause rules scan for incident signatures such as `timeout`, `connection refused`, `nullpointerexception`, `outofmemoryerror`, `too many connections`, `401`, `403`, `access denied`, and `broken pipe`.
5. Confidence increases when the same pattern appears repeatedly.
6. Additional anomaly rules raise issues for elevated overall error ratios and repeated identical failures.
7. The highest-confidence candidate becomes the primary root-cause summary, while all candidates become findings with severity and recommendations.

## Dashboard Walkthrough

1. Open the frontend and load a bundled sample log or upload a file.
2. The summary cards show total logs plus INFO, WARN, and ERROR counts.
3. The root cause card highlights the most likely incident driver and confidence.
4. The findings panel lists triggered issues, severity, evidence, and next steps.
5. Charts show recurring error patterns and impacted services.
6. The explorer table supports level, service, and keyword filtering with pagination.
7. Use Export JSON to download the backend export payload.

## Testing

Backend:

```bash
cd backend
mvn test
```

Frontend:

```bash
cd frontend
npm install
npm test
```

## Screenshots

Placeholder screenshots can be added later:

- `docs/screenshots/upload-dashboard.png`
- `docs/screenshots/findings-panel.png`

## Assumptions and Limitations

- The MVP is optimized for plain-text logs, not multiline stack trace reconstruction.
- JSON logs are not parsed specially yet.
- Root-cause detection is deterministic and explainable, not LLM-based.
- Unstructured lines are still stored and searchable, but some fields may remain null or inferred.
- Current sample files are focused on common production incidents rather than exhaustive formats.
- Authentication is intentionally omitted for local MVP simplicity.

## Future Improvements

- JSON log parsing and multiline stack trace stitching
- CSV export and richer downloadable incident reports
- Timeline charts for error volume over time
- Historical trend comparisons across uploads
- Deployment marker ingestion for change correlation
- Optional LLM-generated narrative incident explanation built on top of the rule-based output
