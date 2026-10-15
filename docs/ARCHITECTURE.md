# Architecture

This document explains how the IRCTC Ticket Booking CLI is structured and how data flows through the system.

---

## High-level design

```
┌─────────────┐     ┌──────────────────────┐     ┌─────────────────┐
│   App.java  │────▶│  UserBookingService  │────▶│   users.json    │
│  (CLI UI)   │     └──────────┬───────────┘     └─────────────────┘
└─────────────┘                │
                               ▼
                    ┌──────────────────────┐     ┌─────────────────┐
                    │    TrainService      │────▶│   trains.json   │
                    └──────────────────────┘     └─────────────────┘
```

- **Presentation:** `App` — menu loop and user prompts (`Scanner`)
- **Business logic:** `UserBookingService`, `TrainService`
- **Domain models:** `User`, `Train`, `Ticket`
- **Utilities:** `UserServiceUtil` (password hashing), `LocalDbPathUtil` (file paths)
- **Persistence:** JSON files (no SQL / no server)

---

## Components

### `App` (`ticket.booking.App`)

Entry point (`main`). Responsibilities:

- Print the main menu
- Collect user input
- Delegate to services
- Keep a `UserBookingService` instance that holds the logged-in session after login

Menu options:

| Option | Action |
|--------|--------|
| 1 | Sign up |
| 2 | Login |
| 3 | Fetch bookings |
| 4 | Search trains |
| 5 | Book a seat |
| 6 | Cancel booking |
| 7 | Exit |

### Entities

#### `User`

- `name`, `password` (session plain text, not relied on for storage security)
- `hashedPassword` — BCrypt hash persisted in JSON
- `ticketsBooked` — list of `Ticket`
- `userId` — unique id (UUID for new signups)

Jackson maps snake_case JSON fields (`hashed_password`, `tickets_booked`) via `@JsonNaming`.

#### `Train`

- `trainId`, `trainNo`
- `stations` — ordered list (used to validate source before destination)
- `stationTimes` — map of station → time string
- `seats` — 2D grid; `0` = available, `1` = booked

#### `Ticket`

- Route: `source`, `destination`, `dateOfTravel`
- Links: `userId`, nested `train`
- Seat coordinates: `seatRow`, `seatCol` (used when cancelling to free the seat)

### Services

#### `UserBookingService`

Owns the current session user and the in-memory `userList` loaded from `users.json`.

| Method | Behavior |
|--------|----------|
| `signUp` | Rejects duplicate usernames; appends user; saves JSON |
| `loginUser` | Matches name + BCrypt password; sets `this.user` to DB user |
| `fetchBookings` | Prints tickets for the logged-in user |
| `getTrains` | Delegates search to `TrainService` |
| `fetchSeats` | Returns seat grid for a train |
| `bookTrainSeat` | Marks seat, updates train file, creates ticket, saves user file |
| `cancelBooking` | Removes ticket, saves users, releases seat via `TrainService` |

#### `TrainService`

Loads `trains.json` and supports:

- `searchTrains(source, destination)` — station order must place source before destination (case-insensitive)
- `addTrain` / `updateTrain` — upsert by `trainId` (used after booking a seat)
- `releaseSeat` — sets seat cell back to `0` on cancel

### Utilities

#### `UserServiceUtil`

- `hashPassword(plain)` → BCrypt hash
- `checkPassword(plain, hash)` → boolean

#### `LocalDbPathUtil`

Resolves writable DB files whether you start Gradle from the **repo root** or the **`app/`** module:

1. `app/src/main/resources/localDb/<file>`
2. `src/main/resources/localDb/<file>`
3. `localDb/<file>`

---

## Data flow

### Sign up

```
App → create User(name, password, hash, [], uuid)
    → UserBookingService.signUp
    → append to userList → write users.json
```

### Login

```
App → UserBookingService(user with name+password)
    → loginUser()
    → find matching user → set session user
```

### Search trains

```
App → getTrains(source, dest)
    → TrainService.searchTrains
    → filter stations where index(source) < index(dest)
```

### Book seat

```
App → choose train, row, seat
    → bookTrainSeat(...)
    → seats[row][seat] = 1 → save trains.json
    → new Ticket(...) → user.ticketsBooked.add → save users.json
```

### Cancel booking

```
App → ticketId
    → remove ticket from user → save users.json
    → releaseSeat(trainId, row, col) → save trains.json
```

---

## JSON storage

### `users.json`

Array of users. Nested tickets include train snapshot and seat position.

### `trains.json`

Array of trains with live seat availability. Booking/cancel mutate this file.

Jackson uses snake_case property naming so Java camelCase fields map cleanly to JSON keys like `train_id`, `hashed_password`.

---

## Security notes (learning project)

- Passwords are hashed with BCrypt before storage.
- Plain `password` fields may still appear in sample JSON for demo convenience; do not use this pattern in production.
- There is no encryption at rest, no HTTPS, and no multi-user concurrency control.
- Suitable for local learning demos only.

---

## Dependencies (why they exist)

| Library | Role |
|---------|------|
| Jackson Databind | Serialize/deserialize entities ↔ JSON |
| jBCrypt | Hash and verify passwords |
| Guava | Included from Gradle init template |
| JUnit 4 | Unit tests |
