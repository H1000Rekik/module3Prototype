# 🏟️ CodeArena — Module 3: Code Submission, Execution & Judging

A full-stack prototype implementing **Module 3** of the CodeArena platform — a competitive programming judge system. Users can write Python code, run it against public test cases, or submit it for full evaluation using the [Judge0](https://judge0.com/) execution engine.

---

## 📚 Table of Contents

- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Project Structure](#project-structure)
- [Setup & Execution](#setup--execution)
  - [1. Clone the Repository](#1-clone-the-repository)
  - [2. Backend — Spring Boot](#2-backend--spring-boot)
  - [3. Frontend — Angular](#3-frontend--angular)
- [API Reference](#api-reference)
- [Using the App](#using-the-app)
- [Database Console (H2)](#database-console-h2)
- [How Judge0 Integration Works](#how-judge0-integration-works)
- [Known Issues & Fixes Applied](#known-issues--fixes-applied)
- [Extending for Production](#extending-for-production)

---

## Architecture

```
Angular (port 4200)  ──POST /api/submissions──►  Spring Boot (port 8087)
                     ◄──GET  /api/submissions/{id}──       │
                                                            ▼
                                                  Judge0 CE Public API
                                                  (async polling every 1s)
```

**Submission Flow:**
1. User writes `def solve(nums, target)` in the browser editor
2. Clicks **Run** (public tests only) or **Submit** (all tests including hidden)
3. Spring Boot saves submission as `PENDING` and fires an async job
4. Async job wraps user code with a stdin runner and sends it to Judge0
5. Spring Boot polls Judge0 every 1 second until execution completes
6. Angular polls `/api/submissions/{id}` every 1.5 seconds until status is `DONE`
7. Verdict + test output is displayed to the user

---

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | Angular 17 (standalone components) |
| Backend | Spring Boot 3.2.3 (Java 17) |
| Database | H2 In-Memory (prototype) |
| Code Execution | Judge0 CE (public instance) |
| ORM | Spring Data JPA + Hibernate |

---

## Prerequisites

Make sure you have the following installed before running the project:

| Tool | Minimum Version | Check Command |
|---|---|---|
| Java JDK | 17+ | `java -version` |
| Maven | 3.6+ | `mvn -version` (or use IDE) |
| Node.js | 18+ | `node -v` |
| Angular CLI | 17+ | `ng version` |

> **Tip:** If Maven is not installed globally, use **IntelliJ IDEA** — it bundles Maven automatically when you open a `pom.xml` project.

---

## Project Structure

```
codearena-module3/
├── backend/                          # Spring Boot application
│   ├── src/main/java/com/codearena/
│   │   ├── CodeArenaApplication.java       # Entry point
│   │   ├── controller/
│   │   │   └── SubmissionController.java   # REST endpoints
│   │   ├── model/
│   │   │   └── Submission.java             # JPA entity
│   │   ├── repository/
│   │   │   └── SubmissionRepository.java   # Spring Data JPA
│   │   └── service/
│   │       ├── SubmissionService.java      # Business logic + async
│   │       └── Judge0Service.java          # Judge0 API client
│   ├── src/main/resources/
│   │   └── application.properties          # Config (port, DB, Judge0)
│   └── pom.xml                             # Maven dependencies
│
└── frontend/                         # Angular application
    ├── src/app/
    │   ├── arena/
    │   │   ├── arena.component.ts          # Main component logic
    │   │   ├── arena.component.html        # Editor + results UI
    │   │   └── arena.component.scss        # Styling
    │   └── services/
    │       └── submission.service.ts       # HTTP client service
    ├── package.json
    └── angular.json
```

---

## Setup & Execution

### 1. Clone the Repository

```bash
git clone https://github.com/H1000Rekik/codearena-module3.git
cd codearena-module3
```

---

### 2. Backend — Spring Boot

#### Option A: Using IntelliJ IDEA (Recommended)

1. Open IntelliJ IDEA
2. Click **File → Open** and select the `backend/` folder
3. IntelliJ will automatically detect `pom.xml` and import Maven dependencies
4. Wait for the **"Indexing"** to finish (bottom status bar)
5. Open `src/main/java/com/codearena/CodeArenaApplication.java`
6. Click the green **▶ Run** button next to the `main` method

#### Option B: Command Line (if Maven is installed)

```bash
cd backend
mvn spring-boot:run
```

#### ✅ Verify Backend is Running

Look for this in the console output:
```
Tomcat started on port 8087 (http) with context path ''
Started CodeArenaApplication in X seconds
```

> **Note:** The backend runs on port **8087** (not 8080). This is configured in `application.properties`.

---

### 3. Frontend — Angular

Open a **new terminal** (keep the backend running in a separate one):

```bash
# Navigate to frontend folder
cd frontend

# Install dependencies (first time only)
npm install

# Start the development server
ng serve
```

#### ✅ Verify Frontend is Running

```
✔ Compiled successfully
Application bundle generation complete.

Angular Live Development Server is listening on localhost:4200
```

Open your browser at: **[http://localhost:4200](http://localhost:4200)**

---

## API Reference

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/submissions` | Submit code for judging |
| `GET` | `/api/submissions/{id}` | Poll submission result by ID |
| `GET` | `/api/submissions/user/{username}` | Get submission history for a user |

### POST `/api/submissions` — Request Body

```json
{
  "username": "player1",
  "code": "def solve(nums, target):\n    ...",
  "mode": "run"
}
```

- `mode: "run"` — Tests against **public test cases** only
- `mode: "submit"` — Tests against **all test cases** (including hidden ones)

### Possible Verdicts

| Verdict | Meaning |
|---|---|
| `Accepted` | All test cases passed ✅ |
| `Wrong Answer` | Output did not match expected |
| `Time Limit Exceeded` | Execution took more than 5 seconds |
| `Compilation Error` | Python syntax error |
| `Runtime Error` | Exception thrown during execution |
| `Memory Limit Exceeded` | Exceeded 128 MB memory |

---

## Using the App

1. Go to **[http://localhost:4200](http://localhost:4200)**
2. Enter a **username** in the "Player" field (e.g., `hamza`)
3. Write your Python solution in the code editor. The problem is **Two Sum**:

```python
def solve(nums, target):
    prev_map = {}
    for i, n in enumerate(nums):
        diff = target - n
        if diff in prev_map:
            return [prev_map[diff], i]
        prev_map[n] = i
    return []
```

4. Click **▶ Run** to test against sample cases
5. Click **Submit** to run all test cases (including hidden ones)
6. Results appear in the **Result** tab below the editor
7. View your submission history in the **History** tab

---

## Database Console (H2)

The prototype uses an **H2 in-memory database**. You can browse it while the backend is running:

1. Open: **[http://localhost:8087/h2-console](http://localhost:8087/h2-console)**
2. Fill in the login form **exactly** as follows:

| Field | Value |
|---|---|
| JDBC URL | `jdbc:h2:mem:codearena` |
| User Name | `sa` |
| Password | *(leave empty)* |

3. Click **Connect**

### Useful SQL Queries

```sql
-- View all submissions
SELECT * FROM SUBMISSION;

-- View accepted only
SELECT * FROM SUBMISSION WHERE VERDICT = 'Accepted';

-- Count by verdict
SELECT VERDICT, COUNT(*) FROM SUBMISSION GROUP BY VERDICT;
```

> ⚠️ **Data is reset every time the backend restarts.** This is by design for a prototype.

---

## How Judge0 Integration Works

The backend uses the **Judge0 CE public API** (`https://ce.judge0.com`) to execute Python code in a sandboxed environment.

**Code wrapping:** Since users only write the function body, the backend automatically injects a stdin runner around it:

```python
# User writes only this:
def solve(nums, target):
    ...

# Backend wraps it with this runner automatically:
lines = """2 7 11 15\n9""".strip().split('\n')
nums = list(map(int, lines[0].split()))
target = int(lines[1])
result = solve(nums, target)
print(' '.join(map(str, result)))
```

**Execution limits:**
- CPU time: **5 seconds**
- Memory: **128 MB**

---

## Known Issues & Fixes Applied

### Fix 1: `@PathVariable` parameter name reflection error

**Error:** `Name for argument of type [java.lang.Long] not specified`

**Cause:** Spring Boot 3.2+ no longer reads parameter names from bytecode by default.

**Fix:** Added explicit parameter names in `SubmissionController.java`:
```java
// Before (broken):
public ResponseEntity<?> getSubmission(@PathVariable Long id)

// After (fixed):
public ResponseEntity<?> getSubmission(@PathVariable("id") Long id)
```

And added the Maven compiler plugin in `pom.xml`:
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <parameters>true</parameters>
    </configuration>
</plugin>
```

### Fix 2: Backend port

The backend runs on **port 8087**, not 8080. Make sure your Angular service points to `http://localhost:8087`.

---

## Extending for Production

| Feature | What to do |
|---|---|
| Real database | Replace H2 with PostgreSQL or MySQL in `application.properties` |
| Auth integration | Add JWT tokens from Module 1 |
| More problems | Add a `Problem` table and load test cases from DB |
| More languages | Map additional language IDs in `Judge0Service.java` |
| Message queue | Replace `@Async` with RabbitMQ or Redis for scalability |
| Rate limiting | Add Spring rate limiter on the submit endpoint |

---

## 📄 License

This project is a prototype developed as part of the **CodeArena** academic platform project (ESPRIT).
