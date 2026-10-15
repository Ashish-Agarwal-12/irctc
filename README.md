# IRCTC Ticket Booking (CLI)

A simple **Java console application** that simulates IRCTC-style train ticket booking. Users can sign up, log in, search trains, book seats, view bookings, and cancel tickets. Data is stored in local JSON files (no external database).

---

## Features

- Sign up with BCrypt-hashed passwords
- Login with session handling
- Search trains by source and destination
- View seat map and book an available seat
- View personal bookings
- Cancel a booking (persists to JSON and frees the seat)

---

## Tech stack

| Item | Detail |
|------|--------|
| Language | Java 21 |
| Build | Gradle (Wrapper included) |
| JSON | Jackson Databind |
| Passwords | jBCrypt |
| Storage | JSON files under `app/src/main/resources/localDb/` |

---

## Project structure

```
irctc/
├── app/
│   ├── build.gradle                 # Dependencies, main class, run stdin
│   └── src/
│       ├── main/
│       │   ├── java/ticket/booking/
│       │   │   ├── App.java                 # CLI menu entry point
│       │   │   ├── entities/                # User, Train, Ticket
│       │   │   ├── services/                # Booking & train logic
│       │   │   └── util/                    # BCrypt + DB path helpers
│       │   └── resources/localDb/
│       │       ├── users.json               # User + ticket data
│       │       └── trains.json              # Train + seat data
│       └── test/java/.../AppTest.java
├── docs/
│   ├── ARCHITECTURE.md              # How the system works
│   └── USAGE.md                     # How to use the CLI
├── gradle/                          # Wrapper + version catalog
├── settings.gradle
└── README.md
```

---

## Prerequisites

- **JDK 21** (or newer that supports toolchain 21)
- Internet access on first build (Gradle downloads dependencies)

---

## How to run

From the project root (`irctc/`):

```powershell
.\gradlew.bat :app:run
```

On macOS / Linux:

```bash
./gradlew :app:run
```

Other useful commands:

```powershell
.\gradlew.bat :app:build    # compile + test
.\gradlew.bat :app:test     # tests only
```

> **Note:** Run interactively in a real terminal so you can type menu choices. Gradle is configured to pass `System.in` into the app.

---

## Sample accounts

| Username | Password |
|----------|----------|
| `Ashish Agarwal` | `12345` |
| `rahul` | `hhbb11` |

Sample stations (lowercase): `bangalore`, `jaipur`, `delhi`

---

## Quick demo flow

1. Choose **2** → login with a sample account  
2. Choose **4** → search `bangalore` → `delhi`  
3. Choose **5** → book a seat (pick train, row, column)  
4. Choose **3** → view bookings  
5. Choose **6** → cancel by ticket id  
6. Choose **7** → exit  

More detail: [docs/USAGE.md](docs/USAGE.md)

---

## How it works (summary)

1. `App` shows a menu and calls `UserBookingService` / `TrainService`.
2. Services read/write `users.json` and `trains.json` via Jackson.
3. Passwords are stored hashed; login verifies with BCrypt.
4. Booking marks a seat as taken, creates a `Ticket`, and saves both JSON files.
5. Cancel removes the ticket and frees the seat.

Full explanation: [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)

---

## Documentation

| Document | Purpose |
|----------|---------|
| [README.md](README.md) | Overview, setup, run |
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | Design, data flow, components |
| [docs/USAGE.md](docs/USAGE.md) | CLI walkthrough |
| [docs/GITHUB_CHECKLIST.md](docs/GITHUB_CHECKLIST.md) | What to ignore / remove before push |

---

## License

Personal / learning project. Use and modify freely for educational purposes.
