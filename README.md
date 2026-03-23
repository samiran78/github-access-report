``` Here's the complete README — copy everything below and paste into GitHub:
markdown# GitHub Organization Access Report Service

A Spring Boot REST service that connects to the GitHub API and generates a structured JSON report showing **which users have access to which repositories** within a given GitHub organization.

Built as a technical assignment for CloudEagle.

---

## What This Project Does

Organizations using GitHub often need visibility into who has access to what. This service automates that — you give it an organization name, it talks to GitHub, fetches all repositories and their collaborators in parallel, and returns a clean JSON report mapping each user to the repositories they can access along with their permission level.

---

## Tech Stack

- Java 17
- Spring Boot 3.5.12
- RestTemplate (HTTP client for GitHub API)
- CompletableFuture (parallel API calls)
- ConcurrentHashMap (thread-safe aggregation)
- SpringDoc OpenAPI / Swagger UI
- Lombok
- Maven

---

## How to Run the Project

### Prerequisites
- Java 17 or higher
- Maven
- A GitHub Personal Access Token with `repo` and `read:org` scopes

### Step 1 — Create a GitHub Token

1. Go to GitHub → Settings → Developer Settings → Personal Access Tokens → Tokens (classic)
2. Click **Generate new token (classic)**
3. Give it a name like `access-report-token`
4. Select these scopes:
   - ✅ `repo`
   - ✅ `read:org`
5. Click **Generate token**
6. Copy it immediately — GitHub shows it only once

### Step 2 — Set the token as environment variable

**Windows PowerShell:**
```powershell
$env:GITHUB_TOKEN="ghp_your_token_here"
```

**Mac/Linux:**
```bash
export GITHUB_TOKEN=ghp_your_token_here
```

> Important: Start the application from the same terminal window where you set this variable.

### Step 3 — Clone and run
```bash
git clone https://github.com/samiran78/github-access-report.git
cd github-access-report
.\mvnw spring-boot:run
```

Server starts on `http://localhost:8080`

---

## How to Call the API

### Endpoint
```
GET http://localhost:8080/api/access-report?organization={orgName}
```

### Example
```
GET http://localhost:8080/api/access-report?organization=spring-projects
```

### Sample Response
```json
{
  "organization": "spring-projects",
  "totalRepositories": 80,
  "totalUsers": 5,
  "generatedAt": "2026-03-23T14:09:02Z",
  "userAccessMap": {
    "samiran78": [
      {
        "repoName": "spring-boot",
        "repoFullName": "spring-projects/spring-boot",
        "repoUrl": "https://github.com/spring-projects/spring-boot",
        "privateRepo": false,
        "permission": "admin"
      },
      {
        "repoName": "spring-framework",
        "repoFullName": "spring-projects/spring-framework",
        "repoUrl": "https://github.com/spring-projects/spring-framework",
        "privateRepo": false,
        "permission": "write"
      }
    ],
    "john_dev": [
      {
        "repoName": "spring-boot",
        "repoFullName": "spring-projects/spring-boot",
        "repoUrl": "https://github.com/spring-projects/spring-boot",
        "privateRepo": false,
        "permission": "read"
      }
    ]
  }
}
```

### Swagger UI
```
http://localhost:8080/swagger-ui.html
```

---

## How Authentication is Configured

The application uses a **GitHub Personal Access Token (PAT)** for authentication.

The token is never hardcoded in source code. It is read from the `GITHUB_TOKEN` environment variable at startup via `application.properties`:
```properties
github.token=${GITHUB_TOKEN:}
```

Spring injects this value into `GitHubConfig.java` using `@Value("${github.token}")`.

A **RestTemplate interceptor** automatically attaches the token as a Bearer header to every outgoing GitHub API request:
```
Authorization: Bearer ghp_your_token
Accept: application/vnd.github+json
X-GitHub-Api-Version: 2022-11-28
```

No manual header setup is needed per request — the interceptor handles it globally.

---

## Design Decisions and Assumptions

### 1. Parallel API calls with CompletableFuture

The core scale challenge: GitHub requires one separate API call per repository to fetch its collaborators. For 100 repos, sequential calls would take 100 × ~500ms = 50+ seconds.

Solution: for each repository, I launch a background task using `CompletableFuture.runAsync()` with a fixed thread pool of 20 threads. All calls run simultaneously:
```
Sequential:  100 repos × 500ms = ~50 seconds
Parallel:    100 repos ÷ 20 threads × 500ms = ~2.5 seconds
```

This directly addresses the scale requirement of 100+ repositories.

### 2. ConcurrentHashMap for thread safety

Since 20 threads write to the same user-access map simultaneously, a regular `HashMap` would cause data corruption — two threads can overwrite each other. `ConcurrentHashMap` handles concurrent writes safely without manual synchronization.

### 3. Pagination support

GitHub returns a maximum of 100 results per page. The `fetchAllPages()` method in `GitHubApiClient.java` automatically fetches all pages until GitHub returns an empty response. This supports organizations with 100+ repositories and 1000+ users with no changes needed.

### 4. Graceful degradation

If fetching collaborators for one specific repository fails — due to permission errors or network issues — that repository is skipped with a warning log. The rest of the report completes successfully. One broken repo never crashes the entire report.

### 5. Token security

The GitHub token is always read from an environment variable and never hardcoded anywhere in the source code. This ensures the token cannot be accidentally exposed when the repository is public.

### 6. Permission resolution

GitHub's collaborator API returns 5 boolean flags per user: `admin`, `maintain`, `push`, `triage`, `pull`. The `highestRole()` method in `GitHubUser.java` converts these into one readable string by checking from highest to lowest privilege, making the report output clean and easy to understand.

---

## Project Structure
```
src/main/java/com/samiran/github_access_report/
├── controller/
│   └── AccessReportController.java     → single GET endpoint
├── service/
│   └── AccessReportService.java        → parallel aggregation logic
├── client/
│   └── GitHubApiClient.java            → GitHub API calls + pagination
├── model/
│   ├── GitHubRepo.java                 → maps GitHub repo JSON
│   ├── GitHubUser.java                 → maps GitHub user JSON
│   └── AccessReport.java              → final response structure
├── config/
│   └── GitHubConfig.java              → RestTemplate + thread pool
└── exception/
    ├── GitHubApiException.java         → custom exception
    └── GlobalExceptionHandler.java     → global error handling
```

---

## Error Handling

| Scenario | HTTP Status | Message |
|---|---|---|
| Organization not found | 404 | Organization not found: xyz |
| Token missing or invalid | 401 | Token missing or invalid. Set GITHUB_TOKEN. |
| Token lacks permissions | 403 | Token lacks permissions. Need: repo, read:org |
| GitHub rate limit hit | 429 | GitHub rate limit hit. Wait and retry. |
| Empty org name | 400 | Organization name must not be empty |

---

## Author

**Samiran Roy Bhowmik**
- GitHub: [samiran78](https://github.com/samiran78)
- LinkedIn: [samiran-roy-413209249](https://www.linkedin.com/in/samiran-roy-413209249)
- Email: sroy21191@gmail.com ```
