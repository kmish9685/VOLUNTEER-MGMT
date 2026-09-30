# Hosting VolunteerHub Online (Free)

Vercel cannot run Java/Spring Boot servers, so we use:
- **Render** (free) to run the Java app, using the `Dockerfile`
- **Supabase** (free) for the online PostgreSQL database

## Step 1 - Create the free PostgreSQL database (Supabase)
1. Sign up at https://supabase.com (GitHub login, no credit card needed).
2. Click **New project** → give it a name → choose a region close to you → set a strong password.
3. Wait until it shows **Ready**, then open **Project Settings → Database**.
4. Note the **Host**, **Port** (5432), **User** (`postgres`), **Password**, and **Database** (`postgres`).

## Step 2 - Deploy the app (Render)
1. Sign up at https://render.com with your GitHub account.
2. **New +** → **Web Service** → pick the `VOLUNTEER-MGMT` repository.
3. Language/Runtime: **Docker**. Instance type: **Free**.
4. Add these **Environment Variables** (use the values from Supabase):

| Key | Value |
|-----|-------|
| `DB_URL` | `jdbc:postgresql://HOST:5432/postgres?sslmode=require` |
| `DB_USERNAME` | `postgres` |
| `DB_PASSWORD` | your Supabase database password |

5. Click **Deploy**. The first build takes about 5-10 minutes.
6. Open the `https://<name>.onrender.com` link. The demo data is created automatically.

## Good to know
- The free Render app **sleeps after 15 minutes** with no visitors. The first visit after that
  takes about 1 minute to wake up. Open the link a couple of minutes before your demo.
- Every `git push` to the main branch redeploys the app automatically.
- The real password lives only in Render's environment variables, never in the code.
- `db-secret.properties` (local credentials) is in `.gitignore` and is never pushed to GitHub.
