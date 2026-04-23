# Smart Campus Sensor & Room Management API

**Module:** 5COSC022W — Client-Server Architectures
**Student:** Manupa Ranaweera
**Technology:** JAX-RS 2.x (Jersey 2.41) · Apache Tomcat 9 · Maven · In-Memory Storage (ConcurrentHashMap)

---

## API Overview

This project implements a fully RESTful web service for the University's "Smart Campus" initiative. It provides a comprehensive API to manage **Rooms** and **Sensors** across campus buildings, including a complete historical log of sensor readings per sensor.

### Architecture

- **Framework:** JAX-RS 2.x via Jersey 2.41, deployed as a WAR on Apache Tomcat 9
- **Entry Point:** `SmartCampusApplication` extends `ResourceConfig` with `@ApplicationPath("/api/v1")`, auto-scanning `resources`, `exception`, and `filters` packages
- **Storage:** Thread-safe singleton `DataStore` using `ConcurrentHashMap` for rooms, sensors, and readings — no database
- **Resource Hierarchy:** `/api/v1` → `/api/v1/rooms` → `/api/v1/sensors` → `/api/v1/sensors/{id}/readings`

### Key Features

| Feature | Detail |
|---|---|
| Room CRUD | `GET`, `POST`, `DELETE` on `/api/v1/rooms` |
| Safety Deletion Guard | `DELETE /rooms/{id}` blocked with 409 if sensors are assigned |
| Sensor CRUD + Status Update | `POST`, `GET`, `DELETE`, `PATCH /status` on `/api/v1/sensors` |
| Room Existence Validation | `POST /sensors` returns 422 if the `roomId` does not exist |
| Type Filtering | `GET /sensors?type=CO2` via `@QueryParam` |
| Sub-Resource Locator | `/sensors/{id}/readings` delegates to `SensorReadingResource` |
| Historical Readings | `GET` and `POST` on `/sensors/{id}/readings` |
| Side-Effect Consistency | Posting a reading automatically updates the parent sensor's `currentValue` |
| Custom Exception Mappers | 409, 422, 403, and 500 — all return structured JSON bodies |
| Global Safety Net | `GlobalExceptionMapper<Throwable>` prevents any raw stack trace leaking |
| API Logging Filter | `ContainerRequestFilter` + `ContainerResponseFilter` in one class |
| 500 Demo Endpoint | `GET /api/v1/chaos/trigger-500` demonstrates the global mapper |

---

## Project Structure

```
SmartCampusAPI/
├── .gitignore
├── nb-configuration.xml
├── pom.xml
├── README.md
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── smartcampus/
        │           ├── SmartCampusApplication.java         ← JAX-RS Entry Point
        │           ├── exception/                          ← Custom Exceptions & Mappers
        │           │   ├── GlobalExceptionMapper.java
        │           │   ├── LinkedResourceNotFoundException.java
        │           │   ├── LinkedResourceNotFoundExceptionMapper.java
        │           │   ├── RoomNotEmptyException.java
        │           │   ├── RoomNotEmptyExceptionMapper.java
        │           │   ├── SensorUnavailableException.java
        │           │   └── SensorUnavailableExceptionMapper.java
        │           ├── filters/
        │           │   └── LoggingFilter.java              ← Request/Response Logging
        │           ├── models/                             ← POJO Data Models
        │           │   ├── Room.java
        │           │   ├── Sensor.java
        │           │   └── SensorReading.java
        │           ├── resources/                          ← API Endpoints (Controllers)
        │           │   ├── ChaosResource.java
        │           │   ├── DiscoveryResource.java
        │           │   ├── SensorReadingResource.java
        │           │   ├── SensorResource.java
        │           │   └── SensorRoomResource.java
        │           └── service/
        │               └── DataStore.java                  ← In-Memory Logic (Singleton)
        ├── resources/
        │   └── META-INF/
        │       └── persistence.xml
        └── webapp/                                         ← Web Configuration
            ├── index.html
            ├── META-INF/
            │   └── context.xml
            └── WEB-INF/
                ├── beans.xml
                └── web.xml                                 ← Servlet Mapping
```


---

## How to Build and Run

### Prerequisites

- **JDK 11** or higher
- **Apache Maven 3.8+**
- **Apache Tomcat 9.x**
- **NetBeans IDE** (recommended) or IntelliJ IDEA / VS Code with Maven support

### Step 1 — Clone the Repository

```bash
git clone https://github.com/YOUR_USERNAME/SmartCampusAPI.git
cd SmartCampusAPI/SmartCampusAPI
```

### Step 2 — Build with Maven

```bash
mvn clean package
```

This compiles all sources and produces `target/smart-campus-api.war`.

### Step 3A — Run via NetBeans (Recommended)

1. Open NetBeans → **File → Open Project** → select the `SmartCampusAPI` folder
2. Right-click the project → **Clean and Build**
3. Right-click the project → **Run**
4. NetBeans deploys the WAR to its registered Tomcat instance automatically
5. The API is live at `http://localhost:8080/api/v1`

### Step 3B — Deploy to Standalone Tomcat 9

```bash
# Copy the WAR into Tomcat's webapps directory as ROOT.war (empty context path)
cp target/smart-campus-api.war /path/to/tomcat9/webapps/ROOT.war

# Start Tomcat
/path/to/tomcat9/bin/startup.sh        # Linux / macOS
\path\to\tomcat9\bin\startup.bat       # Windows
```

### Step 4 — Verify the Server is Running

```bash
curl http://localhost:8080/api/v1
```

Expected: `200 OK` with a JSON discovery payload containing version, description, and resource links.

> **Pre-seeded Data:** On startup, `DataStore` seeds one room (`LIB-301 — Library Quiet Study`) and one active sensor (`TEMP-001 — Temperature`) so the API is never empty on first run.

---

## API Endpoints Reference

| Method | Path | Description | Success | Error |
|--------|------|-------------|---------|-------|
| `GET` | `/api/v1` | Discovery — version, contact, resource links | 200 | — |
| `GET` | `/api/v1/rooms` | List all rooms | 200 | — |
| `POST` | `/api/v1/rooms` | Create a new room | 201 + Location header | 400 |
| `GET` | `/api/v1/rooms/{roomId}` | Get a specific room by ID | 200 | 404 |
| `DELETE` | `/api/v1/rooms/{roomId}` | Delete a room (blocked if sensors exist) | 200 | 404, 409 |
| `GET` | `/api/v1/sensors` | List all sensors (optional `?type=` filter) | 200 | — |
| `POST` | `/api/v1/sensors` | Register a sensor (validates `roomId` exists) | 201 + Location header | 400, 422 |
| `GET` | `/api/v1/sensors/{sensorId}` | Get a specific sensor by ID | 200 | 404 |
| `DELETE` | `/api/v1/sensors/{sensorId}` | Delete a sensor and unlink from room | 200 | 404 |
| `PATCH` | `/api/v1/sensors/{sensorId}/status` | Update sensor status field only | 200 | 404 |
| `GET` | `/api/v1/sensors/{sensorId}/readings` | Get full reading history for a sensor | 200 | 404 |
| `POST` | `/api/v1/sensors/{sensorId}/readings` | Add a reading (updates parent `currentValue`) | 201 | 403, 404 |
| `GET` | `/api/v1/chaos/trigger-500` | Trigger a deliberate 500 to demo global mapper | — | 500 |

---

## Sample curl Commands

### 1. Discovery Endpoint — HATEOAS metadata

```bash
curl -X GET http://localhost:8080/api/v1
```

Expected response:
```json
{
  "version": "1.0.0",
  "description": "Smart Campus Sensor & Room Management API",
  "admin_contact": "admin@smartcampus.ac.uk",
  "_links": {
    "rooms": "http://localhost:8080/api/v1/rooms",
    "sensors": "http://localhost:8080/api/v1/sensors"
  }
}
```

---

### 2. Create a Room

```bash
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"ENG-101","name":"Engineering Lab A","capacity":40}'
```

Expected: `201 Created` with `Location: http://localhost:8080/api/v1/rooms/ENG-101` header and the room object in the body.

---

### 3. Register a Sensor (roomId must exist)

```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"CO2-001","type":"CO2","status":"ACTIVE","currentValue":415.0,"roomId":"ENG-101"}'
```

Expected: `201 Created` with the sensor object. Also try with a fake room ID to see the 422 response:

```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"ERR-001","type":"Humidity","status":"ACTIVE","currentValue":55.0,"roomId":"FAKE-999"}'
```

Expected: `422 Unprocessable Entity` with JSON error body.

---

### 4. Filter Sensors by Type

```bash
curl -X GET "http://localhost:8080/api/v1/sensors?type=CO2"
```

Returns only sensors whose `type` field matches `CO2` (case-insensitive). Omit the `?type=` parameter to retrieve all sensors.

---

### 5. Post a Reading to a Sensor (Sub-Resource)

```bash
curl -X POST http://localhost:8080/api/v1/sensors/CO2-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value": 421.3}'
```

Expected: `201 Created` with the new reading (auto-generated UUID + epoch timestamp). The parent sensor's `currentValue` is also updated to `421.3` as a side-effect.

---

### 6. Get Sensor Reading History

```bash
curl -X GET http://localhost:8080/api/v1/sensors/CO2-001/readings
```

Returns the full chronological list of readings recorded for this sensor.

---

### 7. Attempt to Delete a Room with Active Sensors (409 Conflict)

```bash
curl -X DELETE http://localhost:8080/api/v1/rooms/LIB-301
```

Expected: `409 Conflict` — room cannot be deleted while `TEMP-001` is still assigned to it.

---

### 8. Update Sensor Status to MAINTENANCE

```bash
curl -X PATCH http://localhost:8080/api/v1/sensors/CO2-001/status \
  -H "Content-Type: application/json" \
  -d '{"status":"MAINTENANCE"}'
```

---

### 9. Post Reading to a MAINTENANCE Sensor (403 Forbidden)

```bash
curl -X POST http://localhost:8080/api/v1/sensors/CO2-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value": 500.0}'
```

Expected: `403 Forbidden` — sensor is under maintenance and cannot accept new readings.

---

### 10. Trigger a Controlled 500 Error (Global Mapper Demo)

```bash
curl -X GET http://localhost:8080/api/v1/chaos/trigger-500
```

Expected: `500 Internal Server Error` with a clean JSON body — no stack trace visible.

---

## Conceptual Report — Question Answers

### Part 1.1 — JAX-RS Resource Lifecycle

> **Question:** Explain the default lifecycle of a JAX-RS Resource class. Is a new instance created per request or is it a singleton? How does this impact in-memory data management?

By default, JAX-RS creates a **new instance** of every resource class for each incoming HTTP request (per-request scope). This means instance fields on resource classes are not shared between requests — they are discarded after the response is sent.

This architectural decision has critical implications for in-memory data management: shared state cannot live on resource instances. If each request creates a new `SensorRoomResource` object, any data stored in its fields would be lost immediately after the response is sent.

To solve this, all mutable state in this application resides in the `DataStore` singleton, which uses the **synchronized singleton pattern** (`getInstance()` is `synchronized`). The DataStore uses `ConcurrentHashMap` for all three collections (rooms, sensors, readings). `ConcurrentHashMap` is thread-safe by design — it allows concurrent reads and uses segment-level locking for writes, preventing race conditions when multiple requests attempt to read or modify data simultaneously. This ensures no data is lost or corrupted under concurrent load.

---

### Part 1.2 — HATEOAS and Hypermedia

> **Question:** Why is the provision of "Hypermedia" (HATEOAS) considered a hallmark of advanced RESTful design? How does it benefit client developers?

HATEOAS (Hypermedia as the Engine of Application State) represents the highest maturity level of REST APIs (Level 3 of the Richardson Maturity Model). Instead of clients needing to hard-code URLs, the API embeds navigational links in every response. The discovery endpoint at `GET /api/v1` returns a `_links` object mapping resource names to their full URIs.

Benefits over static documentation:

1. **Self-discoverability** — A client with only the base URL can explore the entire API by following links, without any external documentation.
2. **Decoupled Evolution** — If the URL structure changes, clients following links adapt automatically; hard-coded URLs would break.
3. **Reduced Client Complexity** — The server acts as a guide, telling the client what actions are available given the current state.
4. **Reduced Integration Risk** — Teams integrating with the API do not need to maintain their own URL registries or keep up with documentation changes.

---

### Part 2.1 — Full Objects vs IDs in List Responses

> **Question:** What are the implications of returning only IDs versus returning full room objects in a list response?

**Returning only IDs** minimises bandwidth — the payload is tiny — but forces clients to issue N additional `GET /rooms/{id}` requests to get usable data. This is the N+1 problem and significantly increases latency for dashboard-style clients that need all room details at once.

**Returning full objects** (our implementation) requires a single request to get all information, which is better for facilities management dashboards where all room data is displayed simultaneously. The trade-off is a larger response payload, which could be a concern at scale with thousands of rooms.

Our decision is to return full room objects because the API serves operational dashboards. At scale, pagination or sparse fieldset support could mitigate the bandwidth concern without reverting to ID-only lists.

---

### Part 2.2 — DELETE Idempotency

> **Question:** Is the DELETE operation idempotent? What happens on repeated DELETE requests?

Yes, DELETE is idempotent in this implementation. Idempotency means that making the same request N times produces the same server state as making it once — regardless of the response code.

What happens step by step:

- **First DELETE** of a room with no sensors: room is removed → `200 OK` with confirmation JSON
- **Second DELETE** of the same room ID: room no longer exists → `404 Not Found` with JSON error body
- **Every subsequent DELETE**: same result as the second call → `404 Not Found`

After the first successful call, the server state is "room does not exist." Subsequent calls find that identical state. RFC 9110 explicitly states that idempotency applies to the **effect on the resource state**, not the response code. Therefore, returning `404` on repeated deletes is fully compliant with REST idempotency principles — the server state is unchanged by the repeated calls.

---

### Part 3.1 — @Consumes and Content-Type Mismatch

> **Question:** What happens if a client sends data in text/plain or application/xml instead of application/json?

The `@Consumes(MediaType.APPLICATION_JSON)` annotation on POST methods declares that these endpoints only accept `application/json` request bodies. If a client sends any other Content-Type:

1. The JAX-RS runtime checks the incoming `Content-Type` header against all registered `@Consumes` declarations for the matched path.
2. If no match is found — for example `text/plain` or `application/xml` — JAX-RS immediately returns **HTTP 415 Unsupported Media Type** before the resource method is ever invoked.
3. The client receives a 415 error, not a stack trace.
4. No deserialization is attempted, protecting the server from malformed data.

This is a built-in contract enforcement mechanism. `@Consumes` acts as a gatekeeper that validates request format declaratively, keeping resource methods clean and focused on business logic.

---

### Part 3.2 — @QueryParam vs @PathParam for Filtering

> **Question:** Why is the query parameter approach superior to path-based filtering such as /sensors/type/CO2?

**Path parameter approach** (`/sensors/type/CO2`) treats the filter as if it identifies a distinct sub-resource, implying `type/CO2` is a fixed entity in the hierarchy. This is semantically incorrect — `type` is not a resource, it is a filter criterion. Combining multiple filters becomes awkward: `/sensors/type/CO2/status/ACTIVE` requires a new route definition for every combination.

**Query parameter approach** (`/sensors?type=CO2`) — our implementation:

- Semantically correct: `/sensors` is the resource; `?type=CO2` modifies the view of that collection
- Naturally composable: `?type=CO2&status=ACTIVE&roomId=LIB-301` requires no extra routes
- Optional by design — omitting the parameter returns all sensors, which is the natural default
- Industry standard for search and filter operations across REST APIs
- A single route definition handles all possible filter combinations

Query parameters are the correct tool when the intent is "give me a filtered view of collection X," not "give me resource X/Y."

---

### Part 4.1 — Sub-Resource Locator Pattern

> **Question:** What are the architectural benefits of the Sub-Resource Locator pattern?

The sub-resource locator in `SensorResource` — the `@Path("/{sensorId}/readings")` method that returns a `SensorReadingResource` instance — provides the following benefits:

1. **Single Responsibility Principle** — `SensorResource` manages sensors; `SensorReadingResource` manages readings. Each class has exactly one reason to change.

2. **Elimination of God Classes** — Without this pattern, every nested path (`/sensors/{id}/readings`, etc.) would be crammed into a single `SensorResource` class, creating an unmanageable monolith as the API grows.

3. **Independent Testability** — `SensorReadingResource` can be instantiated and unit-tested in complete isolation, without requiring the full `SensorResource` context.

4. **Dynamic Context Injection** — The locator method passes the `sensorId` to the sub-resource constructor, establishing a precise, scoped context for all reading operations without relying on global state.

5. **Scalability** — Adding new reading-specific features such as pagination, date filtering, or data aggregations only affects `SensorReadingResource`, leaving `SensorResource` untouched.

---

### Part 5.2 — HTTP 422 vs 404 for Missing References

> **Question:** Why is HTTP 422 more semantically accurate than 404 when a payload references a non-existent resource?

**HTTP 404 Not Found** means the requested URL does not exist on this server — it describes a routing failure. The path `/api/v1/sensors` is clearly found, so 404 is factually wrong.

**HTTP 422 Unprocessable Entity** means the server understood the content type, successfully parsed the body, but cannot process the semantic instructions because they are logically invalid.

When a client POSTs `{"roomId": "FAKE-999"}`:

- The URL `/api/v1/sensors` **is found** — no routing issue
- The JSON is **syntactically valid** — no parse error
- The **semantic content** is invalid — `FAKE-999` is a dangling reference to a room that does not exist

The problem is inside a valid payload. This is precisely what 422 was designed for: the request was well-formed but semantically unprocessable. Using 404 would mislead the client into thinking the endpoint itself does not exist, causing serious confusion during integration and debugging.

---

### Part 5.4 — Cybersecurity Risks of Stack Traces

> **Question:** What risks does exposing Java stack traces to external API consumers create?

Exposing raw Java stack traces in API responses creates multiple serious attack vectors:

1. **Technology Fingerprinting** — Stack traces expose library names and exact version numbers such as `jersey-server-2.41.jar`. Attackers cross-reference these against CVE databases to find known, exploitable vulnerabilities.

2. **Internal Path Disclosure** — File paths in stack traces reveal server directory structure, OS type, and deployment layout — all valuable for planning targeted attacks.

3. **Logic Flow Exposure** — The call stack reveals the exact sequence of method calls, exposing business logic, data access patterns, and architectural decisions that can be reverse-engineered to find injection points.

4. **Class and Package Enumeration** — Fully qualified class names expose the package structure, making it straightforward to identify entry points for reflection-based or injection attacks.

5. **Error Message Data Leaks** — Exception messages often contain internal state values, object IDs, or configuration strings that must never leave the server.

Our mitigation is the `GlobalExceptionMapper<Throwable>` which intercepts every unhandled exception, logs the full stack trace server-side only (in Tomcat logs), and returns a safe, generic JSON body to the client with no internal details.

---

### Part 5.5 — JAX-RS Filters vs Manual Logging

> **Question:** Why use JAX-RS filters for logging instead of inserting Logger.info() inside every resource method?

**Problems with manual per-method logging:**

- Code duplication — the same log statement must be copied into every resource method
- Human error — developers forget to add logging when writing new endpoints
- Inconsistency — log format varies across methods and across team members
- Violates the DRY (Don't Repeat Yourself) principle and Single Responsibility Principle
- Difficult to disable or modify — changing the format requires editing every resource class

**JAX-RS Filter advantages (our implementation in `LoggingFilter`):**

- Cross-cutting concern isolation — all logging logic lives in exactly one class
- Guaranteed coverage — every request and response is logged automatically, with no exceptions regardless of which resource handles it
- Consistent format — one definition applied uniformly across the entire API
- Easy maintenance — changing the log format requires editing a single file
- Clean resource classes — resource methods focus purely on business logic with no logging boilerplate
- Instantly disableable — removing the `@Provider` annotation or deregistering the filter disables all logging in one step

This is the foundational reason why cross-cutting concerns such as security, logging, CORS handling, and authentication are always implemented as filters and interceptors in mature web frameworks.
