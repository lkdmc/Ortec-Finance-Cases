# Task List — Interfaces

The application exposes the same task-management core through two interfaces: an
interactive **console** and a **REST API**. Both are backed by the same
`TaskService` and keep their data in memory.

- [Running the application](#running-the-application)
- [Console commands](#console-commands)
- [REST API](#rest-api)
- [Error responses](#error-responses)

## Running the application

The entry point picks the interface from the program arguments.

| Mode | Command | Notes |
|------|---------|-------|
| Console | `mvn spring-boot:run` | No arguments — starts the interactive console. |
| Web | `mvn spring-boot:run "-Dspring-boot.run.arguments=web"` | Any argument — starts the REST API on `localhost:8080`. |

Dates use the format `dd-MM-yyyy` everywhere (for example `25-11-2024`).

## Console commands

Type `help` at the prompt to list the commands. A task belongs to a project and
has a numeric id, a description, a done flag, and an optional deadline.

| Command | Description |
|---------|-------------|
| `show` | List every project with its tasks. |
| `add project <name>` | Create a new, empty project. |
| `add task <project> <description>` | Add a task to an existing project. |
| `check <id>` / `uncheck <id>` | Mark a task done or not done. |
| `deadline <id> <dd-MM-yyyy>` | Set a task's deadline. |
| `today` | List only the tasks due today. |
| `view-by-deadline` | List all tasks grouped by deadline, then by project. |
| `help` | Show the available commands. |
| `quit` | Exit the application. |

Invalid input (an unknown id, a malformed date, or missing arguments) is
reported with a message and the application keeps running.

### `view-by-deadline` output

Tasks are grouped by deadline in chronological order, then by project. Tasks
without a deadline appear last under `No deadline:`.

```
11-11-2021:
     secrets:
       	1: Eat more donuts.
     training:
       	4: Four Elements of Simple Design
13-11-2021:
     training:
       	3: Interaction-Driven Design
No deadline:
     training:
       	2: Refactor the codebase
```

## REST API

Base URL: `http://localhost:8080`. All request and response bodies are JSON.

### `POST /projects`

Create a project.

Request:

```json
{ "name": "secrets" }
```

Response — `201 Created`:

```json
{ "id": 1, "name": "secrets", "tasks": [] }
```

An empty or missing name returns `400 Bad Request`.

### `GET /projects`

Return all projects and their tasks.

Response — `200 OK`:

```json
[
  {
    "id": 1,
    "name": "secrets",
    "tasks": [
      { "id": 1, "description": "Eat more donuts.", "done": false, "deadline": "25-11-2024" }
    ]
  }
]
```

### `POST /projects/{projectId}/tasks`

Create a task in a project.

Request:

```json
{ "description": "Eat more donuts." }
```

Response — `201 Created`:

```json
{ "id": 1, "description": "Eat more donuts.", "done": false, "deadline": null }
```

An unknown `projectId` returns `404 Not Found`. A missing description returns
`400 Bad Request`.

### `PUT /projects/{projectId}/tasks/{taskId}?deadline=<dd-MM-yyyy>`

Update a task's deadline. The deadline is passed as a query parameter.

Example: `PUT /projects/1/tasks/1?deadline=25-11-2024`

Response — `200 OK` (the updated task):

```json
{ "id": 1, "description": "Eat more donuts.", "done": false, "deadline": "25-11-2024" }
```

A malformed date returns `400 Bad Request`. An unknown `taskId` returns
`404 Not Found`.

### `GET /projects/view_by_deadline`

Return all tasks grouped by deadline (chronological), then by project. The group
without a deadline, if any, comes last with a `null` deadline.

Response — `200 OK`:

```json
[
  {
    "deadline": "11-11-2021",
    "projects": [
      { "name": "secrets", "tasks": [ { "id": 1, "description": "Eat more donuts.", "done": false, "deadline": "11-11-2021" } ] }
    ]
  },
  {
    "deadline": null,
    "projects": [
      { "name": "secrets", "tasks": [ { "id": 2, "description": "Refactor", "done": false, "deadline": null } ] }
    ]
  }
]
```

## Error responses

Failed requests return a consistent error body:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Project name is required.",
  "path": "/projects"
}
```

| Status | When |
|--------|------|
| `400 Bad Request` | Missing project name or task description, or a malformed deadline. |
| `404 Not Found` | Unknown project id or task id. |
