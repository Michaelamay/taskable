# Taskable

Taskable is a Spring Boot Java 17 personal task manager with a simple web UI, CRUD task management, and AI-powered task helpers for suggestion, summarization, and breakdown.

## Overview

Taskable provides:

- Task CRUD endpoints backed by Spring Data JPA
- An in-memory H2 database for local development
- A vanilla JavaScript frontend served by Spring Boot
- AI endpoints powered by Anthropic Claude for:
  - suggesting structured tasks from plain English
  - summarizing an existing task
  - breaking an existing task into subtasks

## Prerequisites

- Java 17
- Maven

You can use the included Maven wrapper instead of a system Maven install if preferred.

## Configuration

Base application settings live in:

- [`src/main/resources/application.properties`](/Users/michaeljonathanamay/Desktop/taskable/src/main/resources/application.properties)

Local secrets should be stored in:

- [`src/main/resources/application-local.properties`](/Users/michaeljonathanamay/Desktop/taskable/src/main/resources/application-local.properties)

Set your Anthropic API key in `application-local.properties` like this:

```properties
anthropic.api.key=your-real-anthropic-api-key
```

The application already expects these Anthropic settings:

```properties
anthropic.api.url=https://api.anthropic.com/v1/messages
anthropic.api.version=2023-06-01
```

## Running The App

Start the application with:

```bash
./mvnw spring-boot:run
```

Once it is running:

- UI: `http://localhost:8080`
- H2 Console: `http://localhost:8080/h2-console`

H2 connection settings:

- JDBC URL: `jdbc:h2:mem:taskabledb`
- Username: `sa`
- Password: leave blank

## Running Tests

Run the full test suite with:

```bash
./mvnw test
```

## AI Endpoints

### `POST /tasks/suggest`

Takes a plain-English task description and returns a structured task suggestion. This does not save anything to the database.

Example request:

```http
POST /tasks/suggest
Content-Type: application/json
```

```json
{
  "description": "Prepare a high priority project update for next Monday and start working on it now."
}
```

Example response:

```json
{
  "title": "Prepare project update",
  "description": "Create and organize the content for the next Monday project update.",
  "dueDate": "2026-08-03",
  "priority": "HIGH",
  "status": "IN_PROGRESS"
}
```

### `POST /tasks/{id}/summarize`

Fetches an existing task by id and returns a 2-3 sentence conversational summary. This does not update or save the task.

Example request:

```http
POST /tasks/1/summarize
```

Example response:

```text
This task is currently marked as IN_PROGRESS and has HIGH priority, so it should stay near the top of your list. It is due on 2026-08-03, which gives you a clear target for finishing it.
```

### `POST /tasks/{id}/breakdown`

Fetches an existing task by id and returns a list of subtasks. This does not save anything to the database.

Example request:

```http
POST /tasks/1/breakdown
```

Example response:

```json
{
  "subtasks": [
    "Review the task requirements",
    "Draft the first version",
    "Revise and finalize the deliverable"
  ]
}
```

## Main App Endpoints

Core CRUD endpoints:

- `POST /tasks`
- `GET /tasks`
- `GET /tasks/{id}`
- `PUT /tasks/{id}`
- `DELETE /tasks/{id}`

## Frontend

The frontend lives at:

- [`src/main/resources/static/index.html`](/Users/michaeljonathanamay/Desktop/taskable/src/main/resources/static/index.html)

It uses plain vanilla JavaScript and talks directly to `http://localhost:8080` for:

- task listing
- task creation
- task deletion
- AI suggestion
- AI summarization
- AI breakdown
