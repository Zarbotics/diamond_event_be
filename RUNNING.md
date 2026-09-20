# Running the backend

## Locally, from a clean checkout

You need Java 21 and a PostgreSQL you can connect to. Nothing else.

```bash
createdb diamond_ev
./mvnw spring-boot:run
```

That serves the API at **`http://localhost:8080/diamond`** — port and context
path together are what the frontends and the Google OAuth redirect URI are
configured against, so changing either means changing them too. The three
places that need to agree are `server.port` and
`server.servlet.context-path` here, `VITE_API_BASE_URL` in each frontend, and
the authorised redirect URI on the Google client
(`http://localhost:8080/diamond/login/oauth2/code/google`).

That is the whole setup otherwise. Every setting has a development default:
`localhost:5432/diamond_ev` as `postgres`/`postgres`, TLS off, CORS open to
`localhost:5173`–`5175` and `localhost:3000`, and a development JWT signing
secret.

The application starts without Google credentials, without mail credentials
and without Apple. Those features do not work until configured — which is the
right behaviour on a machine that has no credentials for them — but nothing
prevents the application from running.

To point at a different database:

```bash
DB_URL=jdbc:postgresql://localhost:5432/other DB_USERNAME=me DB_PASSWORD=secret ./mvnw spring-boot:run
```

## The database

The schema is still created by Hibernate (`ddl-auto=update`). Flyway runs
first and handles the things Hibernate cannot:

| Migration | What it does |
|---|---|
| `V2` | Backfills NULL soft-delete and extras/services flags; adds the indexes the real queries need. Every statement is guarded on the table existing, so it is safe on an empty database. |
| `V3` | Creates `sso_handoff_code` for one-time sign-in codes. |
| `V4` | `CREATE EXTENSION ltree` — **required**. `MenuItem.txtPath` is an `ltree` column, and without the extension Hibernate cannot create `menu_item` at all. The symptom is `relation "menu_item" does not exist`, which points nowhere near the cause. |

There is no `V1`. The production schema was built by `ddl-auto=update` over a
long period and has never been captured as a migration, so there is nothing
that can recreate it from scratch. Capturing a baseline from a production dump
is the next step; only then can `DDL_AUTO=validate` be turned on.

## Development data

A clean database gives every catalogue endpoint an empty array, so the booking
journey renders as a sequence of blank screens and there is no way to tell a
broken query from an empty table. Load the seed:

```bash
psql -h localhost -U postgres -d diamond_ev -f src/main/resources/db/seed/dev-seed.sql
```

It is idempotent — run it as often as you like — and it lives outside
`db/migration` deliberately, so Flyway will never apply it to production.

It creates venues, event types, the catalogue of decor, extras, services and
suppliers, and the three-level `menu_item` tree the Food Menu step reads
(which is a different structure from `menu_food_master`). It also creates two
accounts, both with the password `DevPassword123!`:

| Email | Role | For |
|---|---|---|
| `dev.customer@example.com` | `ROLE_USER` | Walking the booking journey |
| `dev.admin@example.com` | `ROLE_ADMIN` | The admin portal |

Both are pre-verified so `POST /auth/login` works without an SMTP server. The
customer is deliberately `ROLE_USER`: testing the journey as an administrator
would mean never exercising the authorisation rules a real customer hits.

### Making somebody an administrator

There is deliberately no way to do this from inside the application. Signing up
always produces `ROLE_USER` — it used to read `setTxtRole("ROLE_ADMIN")`, on a
public endpoint, so anybody who posted an email and a password became an
administrator of the whole system. Promotion is an operation for somebody with
the database password, performed on a named person:

```bash
psql -d diamond_ev -v email="'someone@example.com'" \
     -f src/main/resources/db/seed/make-admin.sql
```

**The person signs up first.** There is no password to set in that script,
because the column holds a bcrypt hash and hand-making one is how an
installation ends up with a shared password nobody can rotate. So they register
through the portal's own screen, or `POST /auth/signup`, or "Continue with
Google" — any of which gives them a properly hashed credential — and the script
promotes the account they already have. It reports which happened rather than
printing `UPDATE 0` and leaving you to guess whether the address was wrong.

It takes effect on their next request, with no new sign-in:
`JwtAuthenticationFilter` reads the role from the database on every call rather
than trusting the one inside the token. That is mostly so a **demotion** cannot
be outlived by a token minted before it.

To undo it, the same `UPDATE` with `'ROLE_USER'`. Nothing else records the
role.

## Sign-in

### Configuring Google

Pick either. Both do the same thing; the file is easier on Windows, where
setting a variable for one command is awkward.

**A file on your machine, not in the repository:**

```bash
cp src/main/resources/application-local.properties.example \
   src/main/resources/application-local.properties
# fill in the id and secret, then:
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

`application-local.properties` is gitignored.

**Or environment variables:**

```bash
GOOGLE_CLIENT_ID=...apps.googleusercontent.com \
GOOGLE_CLIENT_SECRET=... \
./mvnw spring-boot:run
```

```powershell
# PowerShell
$env:GOOGLE_CLIENT_ID="...apps.googleusercontent.com"
$env:GOOGLE_CLIENT_SECRET="..."
.\mvnw spring-boot:run
```

The **Authorised redirect URI** on the OAuth client must be exactly:

```
http://localhost:8080/diamond/login/oauth2/code/google
```

That comes from the port and context path — change either and this changes with
it, and the Google console has to be updated to match or sign-in fails with
`redirect_uri_mismatch`.

Both settings have development placeholders so the application starts without
them, which is the right behaviour on a machine that has no Google project.
But an unconfigured backend used to send you to Google anyway, with a client id
of `local-dev-google-client-id`, and Google answered:

```
The OAuth client was not found.
Error 401: invalid_client
```

Correct, and no use — nothing in it points at an unset variable on your own
backend, so it reads as a broken application. The backend now refuses the
sign-in itself and says which variables are missing and what the redirect URI
should be.

### The credentials that were committed

These used to be written into `application.properties` directly:

```
spring.security.oauth2.client.registration.google.client-id=910542917624-....apps.googleusercontent.com
spring.security.oauth2.client.registration.google.client-secret=GOCSPX-...
```

That is why sign-in worked from a clean checkout with no setup, and it is why
it stopped when they were replaced with environment variables.

**The secret is still in this repository's git history.** Removing it from the
current file does not remove it from the commits that carried it, and anyone
who can read the repository can read those. Rewriting history would not help
either: every existing clone still has it.

The only thing that actually closes it is **rotating the secret in the Google
console** — create a new client secret, put the new one in your local file or
environment, and delete the old one. Until that is done, treat the committed
secret as compromised.

### How the handoff works

After Google or Apple sign-in the backend redirects to the frontend with a
single-use `?code=`, which the frontend exchanges over `POST /auth/exchange`
for the real tokens. It used to put the tokens directly in the URL, where they
were captured by browser history, access logs and the `Referer` header.

The code lives for two minutes and can be redeemed exactly once.

## Apple

Apple sign-in is **off by default**. There is one Apple developer account and
one signing key, so it exists in production only — which matches how the
development branches are set up, with Google alone.

Turning it on requires the `apple` profile and all four Apple variables,
including a readable `.p8`:

```bash
SPRING_PROFILES_ACTIVE=prod,apple
```

Without the profile no Apple client is registered at all, so no developer
needs a copy of the signing key and Apple sign-in never appears to be
available where it cannot work.

## Connecting a calendar to the consultation diary

**This is optional and off by default.** With nothing set, consultations work
normally — the diary, the availability rules, booking, moving and cancelling
all behave exactly as they do with it on. What you lose is the part that
reaches outside this system: a host's own meetings do not block slots, nothing
this system books appears in anybody's calendar, and no video link is created.
The admin portal says so, under Consultations → Calendars, rather than
offering a button that fails.

Three things are needed, and the first is needed whichever provider you use.

### 1. A key for the stored access

```bash
openssl rand -base64 32
```

```properties
app.calendar.token-key=<the output>
```

A refresh token is a standing grant to read and write somebody's calendar
until they revoke it, so it is never stored in the clear. With no key set,
connecting is **refused** rather than falling back to plaintext — which is why
this comes first, and why a correct Google registration on its own still will
not connect.

### 2. The callback URL, wherever this backend actually is

```properties
app.calendar.redirect-uri=https://api.example.com/diamond/calendar/oauth/callback
```

Locally the default is already right and you can leave it alone:
`http://localhost:8080/diamond/calendar/oauth/callback`.

It has to be the **backend**, not either frontend, and it has to be reachable
by a browser coming back from Google or Microsoft. Whatever you put here must
also be registered with the provider, character for character — a trailing
slash is a different URL to them.

### 3. An application registration with the provider

Either or both. Google is the usual one.

**Google**, at console.cloud.google.com:

1. Enable the **Google Calendar API**.
2. Create an **OAuth 2.0 Client ID**, type *Web application*.
3. Add the callback from step 2 as an authorised redirect URI.
4. Scopes: `calendar.freebusy` to read, `calendar.app.created` to write.

```properties
app.calendar.google.client-id=...
app.calendar.google.client-secret=...
```

The narrow write scope is deliberate: it creates a calendar this application
owns and can only touch what it put there. The broad `calendar` scope asks
people for everything in their diary and now attracts a heavier verification
review for no benefit here.

This may live in the same Cloud project as "Continue with Google" sign-in, but
it is a separate concern — sign-in identifies a customer, this reads a team
member's availability. Different client ids, different consent.

**Microsoft 365**, at entra.microsoft.com:

1. Register an application; add the callback as a redirect URI.
2. Create a client secret — its value is shown once.
3. Add these **delegated** permissions: `Calendars.ReadBasic`,
   `Calendars.ReadWrite`, `offline_access`.

```properties
app.calendar.microsoft.client-id=...
app.calendar.microsoft.client-secret=...
app.calendar.microsoft.tenant=common
```

Delegated means acting as the person who connected their own account, never as
the organisation; application permissions would read every mailbox in the
tenant. Without `offline_access` there is no refresh token at all, so the
connection stops working within the hour.

### Then, in the admin portal

Consultations → **Calendars**, pick whose calendars, and press Connect. The
browser goes to the provider, the person signs in as themselves and agrees,
and comes back to a listed connection. Connect as many as you like — work and
personal — and all of them block slots. One of them is marked *Use this one*,
and that is the only calendar anything is ever written into.

If a connection stops syncing, disconnect and reconnect it: a revoked or
expired grant cannot be repaired from this end.

### Why nothing breaks when it is not set up

The adapter for a provider with no client id is **not registered at all**,
rather than registered and failing on use. So a consultation books, the sync
step finds no provider, records that there was nothing to write to, and the
booking is untouched. Half-configured is the one state worth avoiding, and the
token key is what makes it impossible: no key, no connection, no half-synced
diary.

## Production

```bash
SPRING_PROFILES_ACTIVE=prod          # Google only
SPRING_PROFILES_ACTIVE=prod,apple    # Google + Apple
```

`prod` requires every value from `.env.example` and refuses to start if a
development default survived — the development signing secret, a `localhost`
CORS origin, an unrotated database password, a placeholder Google client id,
or a `ddl-auto` that would drop the schema. It reports all the problems at
once rather than one per deploy attempt.

## Tests

```bash
./mvnw verify
```

`verify`, not `test`. Surefire's default includes stop at `*Test`, so
`StartupAndSecurityIT` — the only test that boots the whole application
against a real database, and the one covering the startup, CORS and
401-vs-302 failures — was never run by the build. `maven-failsafe-plugin` is
bound to `verify` and picks it up.

`./mvnw test` still runs the 42 unit tests, which need nothing but a JDK.

The integration test skips itself when there is no database, so `verify`
succeeds on a machine without PostgreSQL — it just covers less:

```bash
createdb diamond_ev_test
./mvnw verify
```

Override the connection with `TEST_DB_URL`, `TEST_DB_USERNAME`,
`TEST_DB_PASSWORD`.

Current state: 42 unit tests, 12 integration tests, all passing.
