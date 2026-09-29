# Hosting VolunteerHub Online (Free)

Vercel cannot run Java/Spring Boot servers, so we use:
- **Render** (free) to run the Java app, using the `Dockerfile`
- **Aiven** (free) for the online MySQL database

## Step 1 - Create the free MySQL database (Aiven)
1. Sign up at https://aiven.io (no credit card needed).
2. Create a new service -> **MySQL** -> choose the **Free** plan.
3. Wait until it shows **Running**, then open the service's **Overview** page and note:
   Host, Port, User (`avnadmin`), Password, Database (`defaultdb`).

## Step 2 - Deploy the app (Render)
1. Sign up at https://render.com with your GitHub account.
2. **New +** -> **Web Service** -> pick the `VOLUNTEER-MGMT` repository.
3. Language/Runtime: **Docker**. Instance type: **Free**.
4. Add these **Environment Variables** (use the values from Aiven):

| Key | Value |
|-----|-------|
| `DB_URL` | `jdbc:mysql://HOST:PORT/defaultdb?sslMode=REQUIRED` |
| `DB_USERNAME` | `avnadmin` |
| `DB_PASSWORD` | your Aiven password |

5. Click **Deploy**. The first build takes about 5-10 minutes.
6. Open the `https://<name>.onrender.com` link. The demo data is created automatically.

## Good to know
- The free Render app **sleeps after 15 minutes** with no visitors. The first visit after that
  takes about 1 minute to wake up. Open the link a couple of minutes before your demo.
- Every `git push` to the main branch redeploys the app automatically.
- The real password lives only in Render's environment variables, never in the code.
