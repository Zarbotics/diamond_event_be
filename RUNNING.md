# Running the backend

## Locally, from a clean checkout

You need Java 21 and a PostgreSQL you can connect to. Nothing else.

```bash
createdb diamond_ev
./mvnw spring-boot:run
```

That is the whole setup. Every setting has a development default:
`localhost:5432/diamond_ev` as `postgres`/`postgres`, TLS off, CORS open to
`localhost:5173` and `localhost:3000`, and a development JWT signing secret.

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

## Sign-in

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
./mvnw test
```

Unit tests run anywhere. `StartupAndSecurityIT` boots the whole application
against a real database and skips itself if there is none:

```bash
createdb diamond_ev_test
./mvnw test -Dtest=StartupAndSecurityIT
```

Override with `TEST_DB_URL`, `TEST_DB_USERNAME`, `TEST_DB_PASSWORD`.
