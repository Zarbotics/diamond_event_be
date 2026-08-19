# Database migrations

The schema is owned by Flyway. Hibernate is set to `ddl-auto=validate` and will
refuse to start if the entities and the database have drifted.

## Why this changed

The application previously ran `ddl-auto=update` against the live database.
That setting only ever *adds*: it never drops a column, never narrows a type,
and never adds a constraint to existing data. Every schema mistake was therefore
permanent, no deploy left a record of what it changed, and there was no way to
reproduce the schema or roll one back. Much of the duplication in
`event_master` is a direct result.

## How the baseline works

`V1__baseline_schema.sql` is the schema as it actually exists, captured from a
production `pg_dump --schema-only` on 19 August 2026.

Until then this section said "V1 is not a file" — the existing schema *was* the
baseline, adopted by `baseline-on-migrate` and never written down. That worked
for production and failed everywhere else: **9 tables existed out of the 68 the
entities need** on a database built by the migrations alone. The other 59 came
from `ddl-auto=update` at startup.

The consequence was sharper than the drift. Every statement in `V2` is guarded
on its table existing — it has to be, or an empty database cannot start — and
Flyway runs before Hibernate. On a *fresh* database `V2` therefore found
nothing, applied none of its repairs, and said nothing about it. New
environments came up without the constraints `V2` exists to add.

### What runs V1, and what does not

| Database | What happens |
|---|---|
| **Empty** | Runs `V1`, then `V2`–`V9`. Ends up matching production, constraints included. |
| **Production** | Has no `flyway_schema_history` at all, so `baseline-on-migrate` adopts the schema as version 1 and **skips `V1`** — which is right, because `V1` *is* that schema and running it would try to create tables holding live data. |
| **Created during this branch's development** | History starts at `2`, because `V1` did not exist yet. Flyway refuses to start on these: *"Detected resolved migration not applied to database: 1"*. `FlywayBaselineStamp` stamps them on startup, once, and then never again. |

All three were verified against a restored production dump before this landed,
including a full simulation of the production deploy.

### Why from production rather than from the entities

Hibernate can export what the entities describe, and that was the tempting
shortcut. But `ddl-auto=update` never drops and never narrows, so production
holds things the entities no longer mention: `decor_event_extras` and
`detail_seq` map to no entity at all, and there are four views the Java code
knows nothing about. An entity export would have silently omitted every one of
them, and the first fresh environment would have differed from production in
ways nobody would notice until something failed.

## Before the first deploy of this branch

`ddl-auto=validate` is stricter than `update` was, so this was checked against a
restored production dump before the switch: the schema validates, and `update`
had nothing left to do. Worth repeating against a current copy anyway, because
production moves:

```bash
createdb scratch_diamond_ev
pg_restore --schema-only --no-owner --no-privileges \
    -d postgresql://localhost:5432/scratch_diamond_ev production.dump
DB_URL=jdbc:postgresql://localhost:5432/scratch_diamond_ev ./mvnw spring-boot:run
```

If startup fails, the message names the table and column that disagree. Fix by
adding a migration, not by reverting to `update` — reverting hides the drift
again rather than resolving it.

`DDL_AUTO=update` still exists as an escape hatch for an emergency. Using it
means the schema is no longer described by these files, so it should be followed
by a migration that captures whatever it did.

## Naming

`V<n>__<snake_case_description>.sql`, one concern per file, never edited once
merged — Flyway checksums applied migrations and a changed file fails the next
startup. Corrections go in a new migration.

## Pending: constraints that need data cleaned first

These are deliberately **not** yet migrations, because they will fail on live
data that currently violates them. Each needs a dedup pass agreed with the
business first, since deciding which duplicate is authoritative is a business
question, not a technical one.

### `customer_master.txt_email` — unique

Duplicate customer records share an email address. This matters beyond
tidiness: email is the only link between a login (`user_master`) and a customer
record, so a duplicate means one person's bookings are split across two
customer rows. `CurrentUser.customers()` deliberately returns a list to cope
with this today.

Find them:

```sql
SELECT LOWER(txt_email), COUNT(*), ARRAY_AGG(ser_cust_id ORDER BY ser_cust_id)
FROM customer_master
WHERE bln_is_deleted = false AND txt_email IS NOT NULL
GROUP BY LOWER(txt_email)
HAVING COUNT(*) > 1
ORDER BY 2 DESC;
```

### `event_master.txt_event_master_code` — unique

Reference codes are generated with `SELECT MAX(...)` and a parse-increment,
with no lock and no constraint, so two simultaneous enquiries can be issued the
same code. The generator should move to a database sequence; the unique
constraint is what makes that safe.

Find collisions:

```sql
SELECT txt_event_master_code, COUNT(*)
FROM event_master
WHERE txt_event_master_code IS NOT NULL
GROUP BY txt_event_master_code
HAVING COUNT(*) > 1;
```

### `event_master` — one venue reference

`ser_venue_master_id` and `ser_venue_master_detail_id` are both present, and the
second already implies the first, so they can disagree. Check before dropping
the redundant column:

```sql
SELECT e.ser_event_master_id, e.ser_venue_master_id, d.ser_venue_master_id AS implied
FROM event_master e
JOIN venue_master_detail d ON d.ser_venue_master_detail_id = e.ser_venue_master_detail_id
WHERE e.ser_venue_master_id IS DISTINCT FROM d.ser_venue_master_id;
```
