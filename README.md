# Global Class Offering Booking System

Spring Boot backend for course offerings, sessions, and parent bookings across timezones.

## Domain Model

- `Course`: global class/course catalog entry, such as Python Coding or Art Drawing Class.
- `Offering`: schedulable section of a course, such as Saturday Batch.
- `Session`: individual meeting time for an offering.
- `Booking`: parent booking for the full offering.
- `Teacher` and `ParentAccount`: lightweight actors used by the workflow.

## Tech Stack

- Java 17
- Spring Boot 4
- Spring Web MVC
- Spring Data JPA
- YugabyteDB via PostgreSQL-compatible YSQL
- PostgreSQL JDBC driver
- H2 in local/test mode

## Environment Variables

| Variable | Default | Description |
| --- | --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5433/yugabyte` | YugabyteDB/PostgreSQL JDBC URL |
| `DB_USERNAME` | `yugabyte` | Database username |
| `DB_PASSWORD` | `yugabyte` | Database password |
| `DDL_AUTO` | `update` | Hibernate schema update mode |

## Run Locally

The default configuration connects to local YugabyteDB YSQL:

```bash
./gradlew bootRun
```

Default database settings:

```text
DB_URL=jdbc:postgresql://localhost:5433/yugabyte
DB_USERNAME=yugabyte
DB_PASSWORD=yugabyte
```

Override them if your database uses different values:

```bash
DB_URL=jdbc:postgresql://localhost:5433/your_db_name \
DB_USERNAME=your_user \
DB_PASSWORD=your_password \
./gradlew bootRun
```

The test suite uses in-memory H2 from `src/test/resources/application.properties`.

For standard PostgreSQL:

```bash
DB_URL=jdbc:postgresql://localhost:5432/global_class_booking \
DB_USERNAME=postgres \
DB_PASSWORD=postgres \
DB_DRIVER=org.postgresql.Driver \
DDL_AUTO=update \
./gradlew bootRun
```

## API Documentation

OpenAPI documentation is available at:

```text
docs/openapi.yaml
```

Postman collection is available at:

```text
docs/postman_collection.json
```

## API Summary

All session times are stored as UTC instants. Teachers submit local times with an IANA timezone, such as `Asia/Kolkata`. Parents pass their display timezone with `?timeZone=America/New_York`.

### Create Offering

If a course with the same normalized name already exists, the offering is linked to that course. Otherwise, the course is created first.

```http
POST /api/teacher/offerings
Content-Type: application/json

{
  "teacherId": 101,
  "courseName": "Python Coding",
  "title": "Saturday Batch",
  "teacherTimeZone": "Asia/Kolkata"
}
```

### Add Session

```http
POST /api/teacher/offerings/1/sessions
Content-Type: application/json

{
  "teacherId": 101,
  "startLocalDateTime": "2026-06-06T18:00:00",
  "endLocalDateTime": "2026-06-06T19:00:00"
}
```

### Teacher Upcoming Offerings

```http
GET /api/teacher/101/offerings?timeZone=Asia/Kolkata
```

### Parent Available Offerings

```http
GET /api/parent/offerings?timeZone=America/New_York
```

### Book Offering

```http
POST /api/parent/bookings?timeZone=America/New_York
Content-Type: application/json

{
  "parentId": 501,
  "offeringId": 1
}
```

### Parent Bookings

```http
GET /api/parent/501/bookings?timeZone=America/New_York
```

## Booking And Concurrency Rules

- Parents book the full offering, not individual sessions.
- A booking locks all sessions in that offering for that parent.
- Conflict detection uses interval overlap logic: `existing.start < requested.end AND existing.end > requested.start`.
- Booking runs inside one transaction.
- The parent row is acquired with a pessimistic write lock before conflict checks, which serializes simultaneous booking attempts for the same parent.
- A unique database constraint prevents duplicate bookings for the same parent and offering.
- Multiple parents can book the same offering concurrently because there is no capacity rule in the assignment.
- Adding sessions is blocked after any booking exists for an offering, so later teacher edits cannot invalidate existing parent locks.

## Database Schema Overview

- `courses`: Stores course/class catalog records. Course names are normalized to avoid duplicate names with different spacing/case.
- `teachers`: Stores lightweight teacher records.
- `offerings`: Stores schedulable sections linked to `courses` and `teachers`.
- `sessions`: Stores meeting times linked to `offerings`; start and end times are persisted as UTC instants.
- `parent_accounts`: Stores lightweight parent records.
- `bookings`: Stores parent bookings at offering level. It has a unique constraint on `(parent_id, offering_id)`.

Schema compatibility/migration SQL is included in:

```text
src/main/resources/schema.sql
```

## Timezone Handling

- Teachers submit session date-times as local date-times with an IANA timezone, for example `Asia/Kolkata`.
- The service converts those local date-times to UTC `Instant` values before persistence.
- Parent and teacher read APIs accept `timeZone` as a query parameter.
- Response session times are formatted in the requested timezone, for example `America/New_York`.

## Concurrency Handling

- Booking is executed inside a database transaction.
- The parent row is locked with `PESSIMISTIC_WRITE` before checking existing bookings.
- This serializes simultaneous booking attempts for the same parent.
- Conflict detection checks every requested offering session against every already booked session for that parent.
- Duplicate booking of the same offering is prevented by a database unique constraint.

## Assumptions

- There is no offering capacity limit in the assignment, so multiple parents may book the same offering.
- Authentication/authorization is outside the assignment scope; `teacherId` and `parentId` are passed directly.
- Parent bookings are for the full offering, never individual sessions.
- Sessions cannot be added after an offering has bookings, because that could change the schedule already locked for parents.
- Course creation is automatic when an offering is created with a new course name.

## Tests

```bash
./gradlew test
```

Covered scenarios:

- Parent cannot book an offering when any session overlaps an existing booking.
- Session times render correctly in a parent timezone.
- Concurrent overlapping booking attempts for the same parent cannot both succeed.
