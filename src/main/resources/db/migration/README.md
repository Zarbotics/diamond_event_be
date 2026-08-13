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

`V1` is not a file. The existing production schema *is* V1: on first run against
a non-empty database, `spring.flyway.baseline-on-migrate=true` records it as
baseline version 1 without executing anything. Migrations therefore start at
`V2`.

## Before the first deploy of this branch

`ddl-auto=validate` is stricter than `update` was. Validate against a **copy** of
production before deploying:

```bash
# restore a production dump into a scratch database, then:
DB_URL=jdbc:postgresql://localhost:5432/scratch_diamond_ev ./mvnw spring-boot:run
```

If startup fails, the message names the table and column that disagree. Fix by
adding a migration, not by reverting to `update`.

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
