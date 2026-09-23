#!/usr/bin/env bash
#
# Rehearse the demo database's migration, on a copy, before touching the server.
#
# ---------------------------------------------------------------------------
# WHAT THIS ANSWERS
#
# The demo database was built by Hibernate's `ddl-auto=update`, not by Flyway.
# This branch hands the schema to Flyway: twenty-five migrations, with
# `baseline-on-migrate` set. So on first start against that database Flyway
# stamps it at version 1, skips V1 — which only captures what was already
# there — and runs V2 to V25 against a schema it did not create.
#
# Whether that works depends on how far Hibernate's shape has drifted from the
# one the migrations assume, and the only way to find out is to try it. The
# only question worth asking is whether you find out here or on the server.
#
# Two things can fail, in order:
#
#   1. A migration, because the schema is not the shape it expected.
#   2. `ddl-auto=validate` afterwards, because the schema Flyway produced does
#      not match the entities. This one is quieter and comes second.
#
# The application starting is the proof that neither did.
# ---------------------------------------------------------------------------
#
# USAGE
#
#     scripts/rehearse-migration.sh de_demo
#
# It copies the database first and works only on the copy, so your import is
# left alone and you can run it as many times as you need.

set -euo pipefail

SOURCE_DB="${1:-de_demo}"
REHEARSAL_DB="${SOURCE_DB}_rehearsal"
PGUSER_="${PGUSER:-postgres}"
PGHOST_="${PGHOST:-localhost}"
PGPORT_="${PGPORT:-5432}"

psql_() { psql -h "$PGHOST_" -p "$PGPORT_" -U "$PGUSER_" "$@"; }

echo "→ Copying ${SOURCE_DB} to ${REHEARSAL_DB}"
# Dropped and recreated each run, so a second attempt starts from the same
# place as the first. A half-migrated database would answer a different
# question from the one being asked.
psql_ -d postgres -c "DROP DATABASE IF EXISTS ${REHEARSAL_DB};" >/dev/null
psql_ -d postgres -c "CREATE DATABASE ${REHEARSAL_DB} TEMPLATE ${SOURCE_DB};" >/dev/null

echo "→ What is in it now"
psql_ -d "$REHEARSAL_DB" -tAc \
  "SELECT '  tables: ' || count(*) FROM information_schema.tables WHERE table_schema = 'public';"
# Ordered by installed_rank, not by version. Flyway stores the version as text,
# so min() and max() compare it lexically and report a database that has
# reached V25 as having reached V9.
psql_ -d "$REHEARSAL_DB" -tAc \
  "SELECT '  flyway history: ' || CASE WHEN to_regclass('public.flyway_schema_history') IS NULL
     THEN 'none — Flyway will baseline at V1 and run V2 onward'
     ELSE (SELECT count(*) || ' applied, latest V'
             || (SELECT version FROM flyway_schema_history
                 WHERE version IS NOT NULL ORDER BY installed_rank DESC LIMIT 1)
           FROM flyway_schema_history)
   END;"

echo
echo "→ Starting the application against the copy, in the prod profile"
echo "  (Ctrl-C once you see it start, or read the failure.)"
echo

# The prod profile has no defaults, deliberately, so everything it checks has
# to be present. These are rehearsal values: the point is to exercise the
# migrations, not to be a working deployment.
export SPRING_PROFILES_ACTIVE=prod
export DB_URL="jdbc:postgresql://${PGHOST_}:${PGPORT_}/${REHEARSAL_DB}"
export POSTGRES_DB="$REHEARSAL_DB"
export POSTGRES_USER="$PGUSER_"
export POSTGRES_PASSWORD="${PGPASSWORD:-postgres}"
export JWT_SECRET="$(openssl rand -base64 48)"
export CORS_ALLOWED_ORIGINS="https://rehearsal.invalid"
export FRONTEND_BASE_URL="https://rehearsal.invalid"
export FRONTEND_ADMIN_URL="https://rehearsal.invalid/admin"
export GOOGLE_CLIENT_ID="rehearsal-client-id"
export GOOGLE_CLIENT_SECRET="rehearsal-client-secret"
export MAIL_USERNAME=""
export MAIL_PASSWORD=""
export SSL_ENABLED=false
export UPLOAD_DIR="/tmp/rehearsal-uploads/"
mkdir -p "$UPLOAD_DIR"

./mvnw -q spring-boot:run

# ---------------------------------------------------------------------------
# READING THE RESULT
#
#   "Started DeApplication in …"
#       Both steps passed. The server deploy will migrate cleanly.
#
#   "Migration V… failed" / "ERROR: column … does not exist"
#       A migration met a schema it did not expect. Send the migration name and
#       the SQL error — that is enough to fix the migration itself.
#
#   "Schema-validation: missing column …"
#       The migrations applied, and then Hibernate found the result does not
#       match an entity. Send the whole validation block; it lists every
#       mismatch at once rather than stopping at the first.
#
#   "Refusing to start in the 'prod' profile:"
#       Nothing to do with the database — a variable above is missing. Read the
#       list; it names each one.
# ---------------------------------------------------------------------------
