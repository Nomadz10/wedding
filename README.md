# Wedding Website

A Spring Boot + Thymeleaf site with two halves:

1. **Public site** — a home page and an events timeline (photos, descriptions, times, locations).
2. **RSVP system** — guests log in with their **first + last name** (matched against a
   pre-loaded invite list), RSVP yes/no, add a plus-one, and — once you've booked the
   hotel — see their room assignment and a personalised event schedule.

Plus an **admin panel** to manage everything and watch the live headcount.

---

## Running it

### In IntelliJ
1. Open this folder. IntelliJ detects `pom.xml` — click **Load Maven Project** if prompted.
2. Run `WeddingApplication` (green ▶ next to the class in
   `src/main/java/com/wedding/WeddingApplication.java`).
3. Open http://localhost:8080

### From the command line
```
mvn spring-boot:run
```
Then open http://localhost:8080

The app uses an embedded **H2 file database** stored in `./data/` — no database to install.
On first run it seeds a few sample events and 3 sample guests so nothing is empty.

---

## Key pages

| URL | What it is |
|-----|-----------|
| `/` | Public home page with the schedule |
| `/events` | Full events timeline |
| `/rsvp` | Guest login → RSVP form |
| `/rsvp/my-info` | Guest's room + personalised schedule (after you assign a room) |
| `/admin` | Admin panel (password protected) |
| `/h2-console` | Database console (JDBC URL `jdbc:h2:file:./data/weddingdb`, user `sa`, no password) |

---

## Admin panel

Go to `/admin` and sign in. The **default password is `changeme`** — change it in
`src/main/resources/application.properties`:

```properties
wedding.admin.password=changeme
```

From the admin panel you can:
- **Guests & RSVPs** — add guests one at a time or bulk-paste your invite list
  (one "First Last" per line). See the live headcount for hotel/catering planning.
- **Events** — add/edit/delete events, upload a photo for each.
- **Per guest** — after the hotel is booked, open a guest to set their **room assignment**
  and tick which events they're invited to (their personalised schedule). Leave all events
  unticked to invite them to everything.

### How the headcount works
"Total headcount" = everyone who said **yes**, plus their plus-ones. That's the number to
book rooms/meals for.

---

## Customising

- **Couple's names / date / venue** live in the templates
  (`src/main/resources/templates/index.html` and `fragments/layout.html` — the `J & A`
  brand). Search for `Jane` / `Alex` / `Rosewood` to replace.
- **Colours & fonts** are in `src/main/resources/static/css/style.css` (top of the file).
- **Sample data** is seeded by `src/main/java/com/wedding/config/DataSeeder.java` and only
  runs when the tables are empty. To wipe everything and start fresh, stop the app and
  delete the `./data` folder.

---

## Notes & production checklist

- Guest "login" is name-only by design (no password) — anyone who knows a guest's name can
  see that guest's info. That's fine for a wedding, but don't put anything truly sensitive
  in the notes/room fields.
## Deploying

See **[DEPLOY.md](DEPLOY.md)** for step-by-step hosting on Railway with a custom domain.

The app is already production-ready: it uses H2 locally but **PostgreSQL in production**
(driver included), reads all secrets from **environment variables**, and ships a
**Dockerfile** the host builds in the cloud. Note that GitHub Pages *cannot* host this —
it only serves static files, and the RSVP system needs a live server + database.
