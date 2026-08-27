# Helping Hand — Java Backend (PBL Project)

Plain Java (Servlets + JDBC, no framework) backend for the existing
`helping-hand.html` website. Covers:

1. **Booking system** — save bookings to MySQL, with slot-conflict checking
2. **Notifications** — booking confirmation email (JavaMail)
3. **Authentication** — customer register/login (SHA-256 hashed passwords) + admin login
4. **Service catalog + admin price editing**

---

## 1. Project structure

```
HelpingHandBackend/
├── pom.xml
├── database/schema.sql          <- run this in MySQL first
├── src/main/java/com/helpinghand/
│   ├── model/      User.java, ServiceItem.java, Booking.java
│   ├── util/       DBConnection.java, PasswordUtil.java, JsonUtil.java, NotificationUtil.java
│   ├── dao/        UserDAO.java, ServiceDAO.java, BookingDAO.java
│   └── servlet/    RegisterServlet, LoginServlet, AdminLoginServlet,
│                    ServiceServlet, BookingServlet, AdminBookingServlet
└── src/main/webapp/WEB-INF/web.xml
```

## 2. Quickest way to run: Docker

If you have Docker Desktop installed, you don't need to install Tomcat,
Maven, or MySQL yourself at all. Just run:

```bash
docker compose up --build
```

This will:
1. Build the WAR file (Maven stage) and package it into a Tomcat image
2. Start a MySQL container and automatically run `database/schema.sql`
   on first boot (creates tables + seeds all 34 services)
3. Start the Tomcat container, connected to MySQL over the internal
   Docker network (no manual password editing needed)

Once both containers are up, the API is live at:
```
http://localhost:8080/api/services
```
(Deployed as `ROOT.war`, so there's no `/helping-hand-backend/` prefix
in Docker — just `http://localhost:8080/api/...`)

To stop everything:
```bash
docker compose down
```
To stop AND wipe the database data:
```bash
docker compose down -v
```

If you'd rather run without Docker, follow the manual steps below instead.

## 3. Tools you need (manual/non-Docker setup)

- JDK 11+
- Apache Tomcat 9 (download from tomcat.apache.org)
- MySQL 8
- Maven (or just an IDE like Eclipse/IntelliJ that manages Maven for you)

## 4. Setup steps (manual/non-Docker)

**Step 1 — Create the database**
```bash
mysql -u root -p < database/schema.sql
```
This creates `helping_hand_db` with `users`, `services`, `bookings` tables,
and seeds all 34 services already on your site (same names/prices).

**Step 2 — Set your DB password**
Open `src/main/java/com/helpinghand/util/DBConnection.java` and change:
```java
private static final String DB_USER = "root";
private static final String DB_PASSWORD = "your_mysql_password";
```

**Step 3 (optional) — Enable real emails**
Open `NotificationUtil.java`, set `SMTP_USER` / `SMTP_APP_PASSWORD` (a Gmail
App Password), and flip `EMAIL_ENABLED = true`. Until you do this, booking
confirmations just print to the Tomcat console — the app still works fine
for demoing the project.

**Step 4 — Build and deploy**
```bash
mvn clean package
```
This creates `target/helping-hand-backend.war`. Copy it into Tomcat's
`webapps/` folder (or deploy it from your IDE), then start Tomcat.
Your API will be live at:
```
http://localhost:8080/helping-hand-backend/api/...
```

**Step 5 — Import the required jars if not using Maven**
If your college setup doesn't use Maven, manually download these jars into
`WEB-INF/lib` instead of relying on `pom.xml`:
- `mysql-connector-j-8.0.33.jar`
- `gson-2.10.1.jar`
- `javax.mail-1.6.2.jar`
- `javax.servlet-api-4.0.1.jar` (Tomcat already provides this — don't bundle it in the WAR)

**Step 3.5 — Chatbot (Groq) setup**
The chatbot (`/api/chatbot`) and issue-summarizer (`/api/summarize-issue`)
endpoints call Groq's free AI API. You need a free API key:
1. Go to https://console.groq.com/keys and generate a key
2. Set it as an environment variable named `GROQ_API_KEY` before starting Tomcat:
   - **Docker:** run `GROQ_API_KEY=your_key_here docker compose up --build`
     (or paste the key directly into `docker-compose.yml` under the `web` service)
   - **Windows (manual Tomcat):** `set GROQ_API_KEY=your_key_here` then start Tomcat from that same terminal
   - **Mac/Linux (manual Tomcat):** `export GROQ_API_KEY=your_key_here` then start Tomcat from that same terminal
   - **Eclipse/IntelliJ:** Run Configuration → Environment Variables → add `GROQ_API_KEY`
3. If you skip this, `/api/chatbot` will return `{"error":"Server missing GROQ_API_KEY"}` — that's expected, not a bug.

**"Class file version mismatch" error — why it happens & the fix**
This means the WAR was compiled with a newer JDK (17/21, likely what's already
on your laptop) than the JRE running Tomcat (JDK 11). It builds fine but Tomcat
refuses to load the classes at runtime. Two ways to fix it:
- **Easiest: use Docker** (`docker compose up --build`) — the build stage and
  run stage both use JDK 11 internally, so this can't happen.
- **Manual Tomcat:** make sure the `JAVA_HOME` used by `mvn clean package` is a
  JDK 11 install, OR just confirm you're running Tomcat 9 with a JDK **17**
  (Tomcat 9 works fine on 17) instead of hunting for JDK 11. `pom.xml` now
  uses `maven.compiler.release=11`, which forces Java 11-compatible bytecode
  regardless of which JDK compiles it — so a fresh `mvn clean package` should
  resolve this even without touching your JAVA_HOME.

## 5. API endpoints

| Method | URL | Purpose |
|---|---|---|
| POST | `/api/register` | Customer sign up |
| POST | `/api/login` | Customer login |
| POST | `/api/admin/login` | Admin login (username: `admin`, password: same as before) |
| GET | `/api/services` | List all services |
| POST | `/api/services/update-price` | Admin: update a service's price |
| GET | `/api/bookings?phone=...` | "My Bookings" for a phone number |
| POST | `/api/bookings` | Create a new booking |
| GET | `/api/admin/bookings` | Admin: list every booking |
| POST | `/api/admin/bookings/cancel` | Admin: cancel a booking by id |

All request/response bodies are JSON.

## 6. Connecting your existing HTML/JS to this backend

Right now your site stores everything in `localStorage` (see the
`saveBooking()`, `getBookings()`, `SERVICES` array, and the admin login
check in your HTML `<script>`). To use the real backend instead, replace
those calls with `fetch()` calls to the URLs above. Example — replacing
the booking form's local save with a real API call:

```javascript
// Instead of just calling saveBooking({...}) locally, call the backend:
const response = await fetch('http://localhost:8080/helping-hand-backend/api/bookings', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    serviceId: currentServiceId,
    name: name,
    phone: phone,
    address: address,
    date: date,
    time: time,
    issue: issue,
    email: email // optional, only if you add an email field to the form
  })
});

if (response.ok) {
  const booking = await response.json();
  // show booking-success-wrap like before
} else {
  const err = await response.json();
  errorEl.textContent = err.error; // e.g. "That slot is already booked."
}
```

Same pattern for:
- `adminLogin()` → POST to `/api/admin/login`
- `renderAdminBookings()` → GET `/api/admin/bookings`
- `saveServicePrice(id)` → POST `/api/services/update-price`
- Loading `SERVICES` at page start → GET `/api/services` instead of the hard-coded array

**Note on CORS:** if you keep the HTML file opened directly (`file://`) or
serve it from a different port than Tomcat, you'll need to add CORS headers,
or simplest for a college project: just copy your `helping-hand.html`
into `src/main/webapp/index.html` so both frontend and backend are served
by the same Tomcat app on the same origin (no CORS needed at all).

## 7. For your PBL report / viva

Good points to mention:
- **Dockerized deployment** — the app + database each run in their own
  container, wired together with `docker-compose.yml`, so it starts with
  one command and runs the same way on any machine
- **JDBC + PreparedStatement** used everywhere → prevents SQL injection
- **Servlets** map cleanly to REST-style endpoints (`@WebServlet`)
- **DAO pattern** separates database code from request-handling code
- **SHA-256 password hashing** — passwords are never stored in plain text
- **Session-based admin auth** — admin actions check `HttpSession` before allowing changes
- **Slot-conflict check** in `BookingDAO.isSlotTaken()` prevents double-booking
