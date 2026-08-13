-- =====================================================================
-- V2 — data integrity repairs on top of the baselined schema
-- =====================================================================
--
-- V1 is the schema as Hibernate's ddl-auto=update left it; Flyway baselines
-- it rather than recreating it. This migration is the first deliberate,
-- reviewed change.
--
-- Everything here is additive or a backfill. Nothing drops a column or
-- narrows a type, so it is safe to run against live data and safe to leave
-- in place if the deploy is rolled back.
-- =====================================================================


-- ---------------------------------------------------------------------
-- 1. Extras / services discriminator
-- ---------------------------------------------------------------------
-- event_decor_extras_selection holds two different customer-facing concepts
-- in one table, told apart by bln_is_services. EventMaster maps them as two
-- separate collections, each filtered by @SQLRestriction on that column.
--
-- A row where the column is NULL matches neither `= false` nor `= true`, so it
-- belongs to neither collection: it is invisible to the application, is never
-- returned to the customer, and is never cleaned up when selections change.
-- The column was added to an existing table by ddl-auto, so historical rows
-- have exactly that problem.
--
-- Backfill to false (extras), which is what a row written before services
-- existed must have been.
UPDATE event_decor_extras_selection
SET bln_is_services = false
WHERE bln_is_services IS NULL;

ALTER TABLE event_decor_extras_selection
    ALTER COLUMN bln_is_services SET DEFAULT false;

ALTER TABLE event_decor_extras_selection
    ALTER COLUMN bln_is_services SET NOT NULL;


-- ---------------------------------------------------------------------
-- 2. Soft-delete flags
-- ---------------------------------------------------------------------
-- Repositories filter on `bln_is_deleted = false`. A NULL there is not false
-- in SQL, so any row with a NULL flag silently disappears from every query
-- that filters on it — while still being visible to the queries that do not.
-- Same root cause: the column was added to existing tables by ddl-auto.
DO $$
DECLARE
    t text;
BEGIN
    FOR t IN
        SELECT c.table_name
        FROM information_schema.columns c
        WHERE c.table_schema = current_schema()
          AND c.column_name = 'bln_is_deleted'
    LOOP
        EXECUTE format('UPDATE %I SET bln_is_deleted = false WHERE bln_is_deleted IS NULL', t);
        EXECUTE format('ALTER TABLE %I ALTER COLUMN bln_is_deleted SET DEFAULT false', t);
    END LOOP;
END $$;


-- ---------------------------------------------------------------------
-- 3. Indexes for the queries the application actually runs
-- ---------------------------------------------------------------------
-- Hibernate creates indexes for primary keys but not for foreign keys, and
-- not for the columns these tables are filtered and sorted by. Every one of
-- these backs a query that exists in the codebase today.
--
-- IF NOT EXISTS keeps this migration re-runnable and harmless if an index was
-- already added by hand on the server.

-- Events by customer — the booking portal's main read.
CREATE INDEX IF NOT EXISTS idx_event_master_customer
    ON event_master (ser_cust_id);

-- Availability checks and the admin diary.
CREATE INDEX IF NOT EXISTS idx_event_master_event_date
    ON event_master (dte_event_date);

-- Event reference lookup.
CREATE INDEX IF NOT EXISTS idx_event_master_code
    ON event_master (txt_event_master_code);

-- Customer lookup by email is how a login is joined to a customer record, and
-- it runs on every single request that resolves ownership.
CREATE INDEX IF NOT EXISTS idx_customer_master_email_lower
    ON customer_master (LOWER(txt_email));

CREATE INDEX IF NOT EXISTS idx_user_master_email
    ON user_master (txt_email);

-- Child collections, all loaded by event id.
CREATE INDEX IF NOT EXISTS idx_event_decor_extras_selection_event
    ON event_decor_extras_selection (ser_event_master_id);

CREATE INDEX IF NOT EXISTS idx_event_menu_food_selection_event
    ON event_menu_food_selection (ser_event_master_id);
