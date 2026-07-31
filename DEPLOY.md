# Deploying the wedding site (Railway + custom domain)

Your site is a Java/Spring Boot app with a database, so it needs a host that runs
Java — **GitHub Pages cannot run it** (it only serves static files). We'll use
**Railway** (~$5/month), which builds the included `Dockerfile` and gives you a
managed PostgreSQL database.

The app is already prepped for this:
- It uses H2 locally but **PostgreSQL in production** (driver included).
- All secrets (admin password, DB credentials) come from **environment variables**.
- A `Dockerfile` builds it in the cloud — you don't need Docker installed.

---

## 0. One-time accounts
- A **GitHub** account (to hold the code).
- A **Railway** account — sign up at https://railway.app with your GitHub login.
- A **domain registrar** account if you want a custom domain (Cloudflare, Namecheap, or Porkbun).

---

## 1. Push the code to GitHub
From the project folder (`C:\Users\vijay\IdeaProjects\wedding`):

```bash
git init
git add .
git commit -m "Wedding site"
```

Then create a new **private** repo on github.com (e.g. `wedding`), and follow its
"push an existing repository" instructions, which look like:

```bash
git remote add origin https://github.com/<your-username>/wedding.git
git branch -M main
git push -u origin main
```

> `data/`, `uploads/`, and `target/` are gitignored, so your local test database
> and build files won't be uploaded — that's intentional.

---

## 2. Create the Railway project
1. On https://railway.app click **New Project → Deploy from GitHub repo**.
2. Pick your `wedding` repo. Railway detects the `Dockerfile` and starts building.
3. The first build takes a few minutes. It will start, but **crash-loop until we add
   the database and variables** in the next steps — that's expected.

---

## 3. Add a PostgreSQL database
1. In the project, click **New → Database → Add PostgreSQL**.
2. Railway creates a service named **Postgres** with its own connection variables.

---

## 4. Set the app's environment variables
Open your **app service** (not the Postgres one) → **Variables** tab → add these.
The `${{Postgres.*}}` values are live references to the database service — type them
exactly, and Railway fills in the real values:

| Variable | Value |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://${{Postgres.PGHOST}}:${{Postgres.PGPORT}}/${{Postgres.PGDATABASE}}` |
| `SPRING_DATASOURCE_USERNAME` | `${{Postgres.PGUSER}}` |
| `SPRING_DATASOURCE_PASSWORD` | `${{Postgres.PGPASSWORD}}` |
| `WEDDING_ADMIN_PASSWORD` | *a strong password of your choice* |
| `WEDDING_RSVP_DEADLINE` | `2026-08-20` |
| `WEDDING_UPLOAD_DIR` | `/data/uploads` |

Click **Deploy** / redeploy. The app should now boot and connect to Postgres.
On first boot it seeds your events and guest list into the database.

---

> Everything below lives **inside your `wedding` service card** (click that card on
> the project canvas), *not* in the project's own settings and *not* the Postgres card.
> Railway moves these around, so alternate paths are given.

## 5. Get it on the internet (domain)
1. Click the **`wedding`** service card → the **Settings** tab.
2. Scroll to the **Networking** section → under **Public Networking**, click **Generate Domain**.
3. If it asks which **port**, enter **`8080`**.
4. Railway gives you a free URL like `wedding-production-xxxx.up.railway.app`.
5. Open it — your site should be live. Test the RSVP flow and `/admin` login.

---

## 6. Keep uploaded photos (persistent volume)
Railway's disk is wiped on every redeploy, so guest + event photos need a **volume**.
Railway moved volume creation out of Settings — use whichever works:
- **Right-click the `wedding` service card** on the canvas → **Attach Volume** (or "Create Volume").
- Or press **Ctrl/Cmd + K** (command palette) → type **Volume**.
- Or the canvas **`+ Create` / `+ New`** button → **Volume**.

Set the **Mount path** to **`/data`** (matches the `/data/uploads` env var from step 4), then redeploy.

> Not blocked without it: you can skip the volume for the first deploy and add it later —
> photos just won't survive a *redeploy* until it's attached. (Or skip uploads entirely and
> put photos in `src/main/resources/static/images/` and reference them directly.)

---

## 7. Custom domain (optional)
1. **Buy a domain** at your registrar (~$10–15/year). A `.com` or `.wedding` works.
2. In Railway: **Settings → Networking → Custom Domain**, enter e.g.
   `www.yourdomain.com`. Railway shows a **CNAME target** (like `xxxx.up.railway.app`).
3. In your registrar's **DNS settings**, add a record:
   - **Type:** CNAME  **Name:** `www`  **Value:** *(the Railway target)*
4. To make the bare domain (`yourdomain.com`) work too:
   - On **Cloudflare**: add a CNAME on the root (`@`) to the Railway target —
     Cloudflare "flattens" it automatically. Also add the root domain in Railway.
   - On registrars without flattening: add a redirect from `yourdomain.com` →
     `www.yourdomain.com` (most registrars offer a free "domain forwarding" option).
5. HTTPS is issued automatically. DNS changes take a few minutes to a few hours.

---

## Day-to-day
- **Update the site:** edit code locally, `git commit`, `git push` → Railway
  auto-rebuilds and redeploys.
- **View RSVPs / headcount:** log in at `https://your-domain/admin`.
- **Your data is safe:** it lives in the Postgres service and persists across
  deploys (unlike the local H2 file).

## Rough cost
- Railway: ~$5/month (app + small Postgres) for the months you keep it live.
- Domain: ~$10–15/year.

addresses
rsvp by event separation