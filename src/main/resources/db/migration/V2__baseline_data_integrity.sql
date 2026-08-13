-- =====================================================================
-- V2 — data integrity repairs
-- =====================================================================
--
-- EVERY STATEMENT HERE IS GUARDED ON THE TABLE EXISTING.
--
-- Flyway runs before Hibernate. On an existing database the tables are
-- already there and these repairs apply. On an empty one — a developer's
-- laptop, a fresh test database, CI — nothing exists yet, because there is
-- no V1 that can recreate the schema: production was built by
-- ddl-auto=update over a long period and has never been captured as a
-- migration.
--
-- Without the guards this migration fails on any empty database and the
-- application will not start at all. That is precisely what an unguarded
-- version of this file did.
--
-- Capturing a real V1 baseline from a production dump is the next step; see
-- db/migration/README.md. Until then Hibernate still creates the schema and
-- these migrations only repair what it cannot.
-- =====================================================================


-- ---------------------------------------------------------------------
-- 1. Extras / services discriminator
-- ---------------------------------------------------------------------
-- event_decor_extras_selection holds two customer-facing concepts in one
-- table, told apart by bln_is_services. EventMaster maps them as two
-- collections, each filtered by @SQLRestriction on that column.
--
-- A row where the column is NULL matches neither `= false` nor `= true`, so
-- it belongs to neither collection: invisible to the application, never shown
-- to the customer, never cleaned up when selections change. The column was
-- added to an existing table by ddl-auto, so historical rows are exactly that.
DO $$
BEGIN
    IF to_regclass('public.event_decor_extras_selection') IS NOT NULL
       AND EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema = current_schema()
                     AND table_name = 'event_decor_extras_selection'
                     AND column_name = 'bln_is_services')
    THEN
        UPDATE event_decor_extras_selection
        SET bln_is_services = false
        WHERE bln_is_services IS NULL;

        ALTER TABLE event_decor_extras_selection
            ALTER COLUMN bln_is_services SET DEFAULT false;

        -- Only tighten to NOT NULL once the backfill above has actually run
        -- and left no nulls behind.
        IF NOT EXISTS (SELECT 1 FROM event_decor_extras_selection
                       WHERE bln_is_services IS NULL)
        THEN
            ALTER TABLE event_decor_extras_selection
                ALTER COLUMN bln_is_services SET NOT NULL;
        END IF;
    END IF;
END $$;


-- ---------------------------------------------------------------------
-- 2. Soft-delete flags
-- ---------------------------------------------------------------------
-- Repositories filter on `bln_is_deleted = false`. A NULL is not false in
-- SQL, so a row with a NULL flag vanishes from every query that filters on it
-- while remaining visible to the ones that do not. Same root cause: the
-- column was added to populated tables by ddl-auto.
--
-- Driven off information_schema, so it is inherently safe on an empty
-- database: the loop simply finds nothing.
DO $$
DECLARE
    t text;
BEGIN
    FOR t IN
        SELECT c.table_name
        FROM information_schema.columns c
        JOIN information_schema.tables tb
          ON tb.table_schema = c.table_schema
         AND tb.table_name = c.table_name
        WHERE c.table_schema = current_schema()
          AND c.column_name = 'bln_is_deleted'
          AND tb.table_type = 'BASE TABLE'
    LOOP
        EXECUTE format('UPDATE %I SET bln_is_deleted = false WHERE bln_is_deleted IS NULL', t);
        EXECUTE format('ALTER TABLE %I ALTER COLUMN bln_is_deleted SET DEFAULT false', t);
    END LOOP;
END $$;


-- ---------------------------------------------------------------------
-- 3. Indexes for the queries the application actually runs
-- ---------------------------------------------------------------------
-- Hibernate indexes primary keys but not foreign keys, and not the columns
-- these tables are filtered and sorted by. Each of these backs a query that
-- exists in the codebase today.
--
-- Guarded individually: on an empty database Hibernate creates the tables
-- after this runs, so the indexes are added by the next deploy instead.
DO $$
BEGIN
    IF to_regclass('public.event_master') IS NOT NULL THEN
        -- Events by customer: the booking portal's main read.
        CREATE INDEX IF NOT EXISTS idx_event_master_customer
            ON event_master (ser_cust_id);
        -- Availability checks and the admin diary.
        CREATE INDEX IF NOT EXISTS idx_event_master_event_date
            ON event_master (dte_event_date);
        -- Reference lookup.
        CREATE INDEX IF NOT EXISTS idx_event_master_code
            ON event_master (txt_event_master_code);
    END IF;

    -- Customer lookup by email joins a login to a customer record, and now
    -- runs on every request that resolves ownership.
    IF to_regclass('public.customer_master') IS NOT NULL THEN
        CREATE INDEX IF NOT EXISTS idx_customer_master_email_lower
            ON customer_master (LOWER(txt_email));
    END IF;

    IF to_regclass('public.user_master') IS NOT NULL THEN
        CREATE INDEX IF NOT EXISTS idx_user_master_email
            ON user_master (txt_email);
    END IF;

    IF to_regclass('public.event_decor_extras_selection') IS NOT NULL THEN
        CREATE INDEX IF NOT EXISTS idx_event_decor_extras_selection_event
            ON event_decor_extras_selection (ser_event_master_id);
    END IF;

    IF to_regclass('public.event_menu_food_selection') IS NOT NULL THEN
        CREATE INDEX IF NOT EXISTS idx_event_menu_food_selection_event
            ON event_menu_food_selection (ser_event_master_id);
    END IF;
END $$;
