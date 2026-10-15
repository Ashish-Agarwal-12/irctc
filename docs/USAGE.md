# Usage guide

## Start the app

From the repository root:

```powershell
.\gradlew.bat :app:run
```

You should see:

```text
Welcome to IRCTC Ticket Booking

1. Sign up
2. Login
3. Fetch Bookings
4. Search Trains
5. Book a Seat
6. Cancel my Booking
7. Exit the App
Choose an option:
```

Type the option number and press Enter.

---

## 1. Sign up

1. Enter a unique username  
2. Enter a password  

The password is hashed with BCrypt and stored in `users.json`. After signup, use **Login**.

---

## 2. Login

Use an existing account.

**Sample accounts**

| Username | Password |
|----------|----------|
| Ashish Agarwal | 12345 |
| rahul | hhbb11 |

On success you will see a welcome message. Options 3, 5, and 6 require login.

---

## 3. Fetch bookings

Prints all tickets for the logged-in user, including ticket id, route, date, and seat `[row,col]`.

---

## 4. Search trains

1. Enter source station  
2. Enter destination station  

Stations in the sample data are **lowercase**:

- `bangalore`
- `jaipur`
- `delhi`

A train is returned only if both stations exist on its route and the source appears **before** the destination.

Example:

- `bangalore` → `delhi` → finds train `bacs`
- `delhi` → `bangalore` → finds train `defg`

---

## 5. Book a seat

1. Login first  
2. Enter source, destination, and travel date (e.g. `2026-10-20`)  
3. Pick a train by number from the list  
4. Look at the seat map (`0` = free, `1` = taken)  
5. Enter row and column indices (0-based)  

On success:

- The seat becomes `1` in `trains.json`
- A new ticket is added to your user in `users.json`

---

## 6. Cancel booking

1. Login first  
2. Review your bookings and copy a `Ticket ID`  
3. Paste that id when prompted  

On success the ticket is removed and the seat is freed (`0` again).

---

## 7. Exit

Ends the program cleanly.

---

## Troubleshooting

| Problem | Fix |
|---------|-----|
| `NoSuchElementException: No line found` | Run in an interactive terminal; ensure `app/build.gradle` sets `standardInput = System.in` for the `run` task |
| Login fails | Check username spelling/spacing; use sample passwords above |
| No trains found | Use lowercase station names that exist in `trains.json` |
| Seat booking fails | Seat already `1`, or row/column out of range |
| DB file errors | Run from repo root so `LocalDbPathUtil` can find `app/src/main/resources/localDb/` |
