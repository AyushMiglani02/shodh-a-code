````markdown
# Shodh-a-Code — Lightweight Real-Time Coding Contest Platform

Spring Boot + Postgres backend with a Dockerized live judge, and a Next.js frontend. One `docker-compose.yml` runs everything locally.

Goal: Students join a contest by code, submit code, and watch a live leaderboard.

---

## Table of Contents
- [Architecture Overview](#architecture-overview)
- [Tech Stack](#tech-stack)
- [Setup Instructions](#setup-instructions)
- [Runbook](#runbook)
- [API Design](#api-design)
  - [Contest](#contest)
  - [Submissions](#submissions)
  - [Leaderboard](#leaderboard)
  - [Error Format](#error-format)
- [Data Model](#data-model)
- [Design Choices & Justification](#design-choices--justification)
- [Configuration & Env Vars](#configuration--env-vars)
- [Security Notes](#security-notes)
- [Troubleshooting](#troubleshooting)
- [Future Work](#future-work)
- [Repository Layout](#repository-layout)

---

## Architecture Overview

**Flow**
1. User opens the frontend, enters a Contest Code and a Username.
2. Frontend fetches contest + problems and shows a simple editor.
3. On submit, frontend calls `POST /api/submissions` to create a submission.
4. Backend enqueues asynchronous judging, compiles & runs inside a **short-lived Docker container**, enforces time/memory limits, compares stdout to expected output, and updates status.
5. Frontend polls submission status every **2–3s** and the leaderboard every **15s**.

**Components**
- **Postgres** stores users, contests, problems, test cases, submissions.
- **Spring Boot** exposes REST APIs and orchestrates Docker runs with `ProcessBuilder`.
- **Judge image** uses Alpine + Temurin JDK to compile/run user code (Java in MVP).
- **Next.js** provides join page, problem view, submission panel, leaderboard.

---

## Tech Stack

- **Backend:** Java 21, Spring Boot 3 (Web, Data JPA, Validation, Actuator), Maven  
- **Database:** Postgres 16  
- **Judge runtime:** `eclipse-temurin:21-jdk-alpine`  
- **Frontend:** Next.js 14 (React 18) + Tailwind CSS  
- **Orchestration:** Docker Compose

---

## Setup Instructions

**Requirements**
- Docker Desktop / Engine (Linux containers)
- ~4 GB free RAM
- Ports **8080** and **3000** available
- Internet access to pull images

**Steps**
```bash
# from the repo root
docker compose build
docker compose up
````

**Open**

* Frontend: [http://localhost:3000](http://localhost:3000)
* Backend health: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

**Try it**

* Contest Code: `ABC123`
* Any username (e.g., `alice`)
* Select “Sum A+B”, submit the default Java snippet, watch status change, and see the leaderboard update.

**Stop**

```bash
Ctrl + C        # in the same terminal
docker compose down
```

---

## Runbook

1. Start the stack: `docker compose up`.
2. Submit a correct solution → expect **Accepted**.
3. Submit an incorrect solution → expect **Wrong Answer** with result details.
4. Check `GET /api/contests/ABC123/leaderboard` or the UI leaderboard for scores.

> No local Java/Maven/Node required — images handle builds.

---

## API Design

Base URL in development: `http://localhost:8080`

### Contest

#### `GET /api/contests/{contestCode}`

Fetch contest details & problems.

**200 OK**

```json
{
  "id": 1,
  "code": "ABC123",
  "title": "Sample Coding Contest",
  "problems": [
    { "id": 1, "title": "Sum A+B", "statement": "Read two integers and print their sum.", "points": 100 },
    { "id": 2, "title": "Echo Lines", "statement": "Read lines until EOF and echo them without change.", "points": 100 },
    { "id": 3, "title": "Factorial N", "statement": "Read integer N and print N! for 0<=N<=10.", "points": 100 }
  ]
}
```

**404 Not Found**

```json
{ "error": "Not Found", "message": "Contest not found" }
```

---

### Submissions

#### `POST /api/submissions`

Create a submission and enqueue async judging.

**Request**

```json
{
  "username": "alice",
  "contestCode": "ABC123",
  "problemId": 1,
  "code": "/* Java code with class Main */",
  "language": "java"
}
```

**200 OK**

```json
{
  "submissionId": 42,
  "status": "Pending",
  "resultText": null
}
```

> Client should poll `GET /api/submissions/{submissionId}` every 2–3 seconds.

#### `GET /api/submissions/{submissionId}`

Fetch current status and the latest result text.

**200 OK**

```json
{
  "submissionId": 42,
  "status": "Accepted",
  "resultText": "All test cases passed"
}
```

**Statuses:** `Pending`, `Running`, `Accepted`, `Wrong Answer`, `RE` (Runtime Error), `TLE` (Time Limit Exceeded)

---

### Leaderboard

#### `GET /api/contests/{contestCode}/leaderboard`

Returns live leaderboard for the contest.

**200 OK**

```json
[
  { "username": "alice", "score": 2 },
  { "username": "bob",   "score": 1 }
]
```

> MVP scoring = count of **Accepted** submissions per user.

---

### Error Format

**400 Bad Request (validation)**

```json
{
  "error": "Bad Request",
  "message": "Method argument not valid",
  "details": { "username": "must not be blank" }
}
```

**404 Not Found**

```json
{ "error": "Not Found", "message": "Contest not found" }
```

---

## Data Model

**users**: `id`, `username (unique)`
**contests**: `id`, `code (unique)`, `title`
**problems**: `id`, `contest_id`, `title`, `statement`, `points`
**test_cases**: `id`, `problem_id`, `input_text`, `expected_output`
**submissions**:
`id`, `user_id`, `contest_id`, `problem_id`, `code`, `language`,
`status` (`Pending|Running|Accepted|Wrong Answer|TLE|RE`), `result_text`,
`created_at`, `updated_at`

Seed data includes one contest (`ABC123`) with 3 problems and several test cases.

---

## Design Choices & Justification

### Backend structure

* **Controllers** handle HTTP and map responses.
* **Services** implement business logic and state transitions.
* **Repos** encapsulate persistence; **Entities** map 1:1 to tables.
* Submissions persisted as **Pending** → **Running** → terminal state; enables safe polling and restarts.

**Trade-off:** Simple **thread pool** for async work — for scale, move to a durable queue (RabbitMQ/Kafka) with stateless judge workers.

### Judge orchestration

* **One Docker container per submission** for isolation and clean teardown.
* Resource limits via `--cpus`, `-m`, and a shell `timeout` wrapper.
* **Host Docker socket** mount is pragmatic for a prototype and local testing.

**Trade-off:** Powerful socket mount — production would prefer isolated workers / remote Docker API with least privilege, plus rootless containers and seccomp/AppArmor.

### Frontend state

* **Next.js + local state + polling**: minimal complexity for MVP.
* Poll status (2–3s) and leaderboard (15s) for a “live enough” feel.

**Trade-off:** Polling is less efficient; upgrade to **SSE/WebSockets** for instant updates.

---

## Configuration & Env Vars

Set via `docker-compose.yml` and `application.yml`.

| Variable                     | Scope    | Purpose                       | Default                                |
| ---------------------------- | -------- | ----------------------------- | -------------------------------------- |
| `SPRING_DATASOURCE_URL`      | backend  | DB URL                        | `jdbc:postgresql://db:5432/shodhacode` |
| `SPRING_DATASOURCE_USERNAME` | backend  | DB user                       | `shodh`                                |
| `SPRING_DATASOURCE_PASSWORD` | backend  | DB password                   | `shodhpass`                            |
| `JUDGE_WORKDIR`              | backend  | Temp staging dir for source   | `/work`                                |
| `JUDGE_IMAGE`                | backend  | Runtime image to execute code | `shodhacode/judge:latest`              |
| `JUDGE_TIME_LIMIT_SECONDS`   | backend  | Per-run time limit            | `3`                                    |
| `JUDGE_MEMORY`               | backend  | Memory limit per run          | `256m`                                 |
| `JUDGE_CPUS`                 | backend  | CPU share per run             | `0.5`                                  |
| `NEXT_PUBLIC_API_BASE`       | frontend | API base URL in browser       | `http://localhost:8080`                |

---

## Security Notes

* User code runs in ephemeral containers with CPU/memory caps and short timeouts.
* Backend mounting the Docker socket is acceptable here for local prototyping only.
* No external secrets; DB creds are local to Compose.

---

## Troubleshooting

* **Ports busy:** change `8080:8080` or `3000:3000` in `docker-compose.yml`.
* **Image pulls fail:** verify network/DNS; try `docker system prune -af` and rebuild.
* **TLE for simple code:** ensure the solution reads exactly the provided input and prints newlines as expected.
* **IDE package warnings:** Java files must be under `backend/src/main/java/com/shodhacode/...` and `package` lines must match folders.

---

## Future Work

* Add Python, C++, JavaScript with separate judge images and language switch.
* Realtime push with **SSE** or **WebSockets**.
* ICPC-style scoring: penalties, earliest acceptance tiebreakers.
* Scale out with a message queue and multiple judge workers.
* Harden isolation (rootless containers, seccomp/AppArmor, remote Docker API).
* Authentication & roles for hosts/participants.

---

## Repository Layout

```
shodh-a-code/
├─ README.md
├─ docker-compose.yml
├─ backend/
│  ├─ Dockerfile
│  ├─ pom.xml
│  ├─ src/main/java/com/shodhacode/contest/...
│  └─ src/main/resources/{application.yml,schema.sql,data.sql}
├─ judge/
│  └─ Dockerfile
└─ frontend/
   ├─ Dockerfile
   ├─ package.json
   ├─ next.config.mjs
   ├─ postcss.config.mjs
   ├─ tailwind.config.js
   └─ src/{app,lib}
```

```
```
