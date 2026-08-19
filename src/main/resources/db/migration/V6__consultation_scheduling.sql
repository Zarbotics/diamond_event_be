-- =====================================================================
-- V6 — Consultations: the tables, and the constraint that makes them safe
-- =====================================================================
--
-- Replaces the Calendly widget at the end of the booking journey. See §12 of
-- PLATFORM.md for the design and the edge cases.
--
-- Written as SQL rather than left to Hibernate because the important part of
-- this schema cannot be expressed in JPA at all: the exclusion constraint at
-- the bottom, which is what actually stops two customers taking the same slot.
-- ddl-auto=update would create the tables and silently omit it, and the system
-- would work perfectly until the first time two people clicked at once.
--
-- Times are all `timestamptz`. The business is in the UK, so local time skips
-- an hour each March and repeats one each October; 01:30 on the last Sunday in
-- October is two different instants and there is no way to tell which one a
-- stored local time meant. UTC has no ambiguous values, so every comparison
-- here is arithmetic rather than a case analysis.
-- =====================================================================

-- btree_gist lets a GiST index hold a plain equality column (the host) beside
-- a range column (the meeting). Without it the exclusion constraint below can
-- only be written over the range, which would stop two hosts being busy at the
-- same time — the opposite of what is wanted.
--
-- Trusted since PostgreSQL 13, so the database owner can create it. On 12 or
-- earlier this needs running once by hand as superuser.
CREATE EXTENSION IF NOT EXISTS btree_gist;


-- ---------------------------------------------------------------------
-- Who takes consultations
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS consultation_host (
    ser_host_id        serial PRIMARY KEY,
    ser_user_id        integer,
    txt_display_name   varchar(160) NOT NULL,
    txt_email          varchar(255) NOT NULL,
    -- The zone this host's working hours are expressed in. Stored per host
    -- because "09:00 to 17:00" is a statement about their local clock, not
    -- about UTC, and it has to survive the clocks changing.
    txt_time_zone      varchar(64)  NOT NULL DEFAULT 'Europe/London',
    bln_is_active      boolean      NOT NULL DEFAULT true,
    bln_is_deleted     boolean      NOT NULL DEFAULT false,
    -- Round-robin assignment picks the host who has waited longest.
    dte_last_assigned  timestamptz,
    created_date       timestamptz  NOT NULL DEFAULT now(),
    updated_date       timestamptz  NOT NULL DEFAULT now(),
    created_by         integer      NOT NULL DEFAULT 0,
    updated_by         integer      NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_consultation_host_email
    ON consultation_host (lower(txt_email)) WHERE bln_is_deleted = false;


-- ---------------------------------------------------------------------
-- What kind of meeting, and the rules around booking one
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS consultation_type (
    ser_consultation_type_id serial PRIMARY KEY,
    txt_name                 varchar(160) NOT NULL,
    txt_description          text,
    num_duration_minutes     integer NOT NULL,
    -- Kept clear either side of the meeting and treated as busy. Without this
    -- a day fills with back-to-back calls and no time between them.
    num_buffer_before_minutes integer NOT NULL DEFAULT 0,
    num_buffer_after_minutes  integer NOT NULL DEFAULT 0,
    -- How soon a meeting may be booked. Stops somebody taking a slot that
    -- starts in four minutes.
    num_minimum_notice_hours integer NOT NULL DEFAULT 24,
    -- And how far out. Stops a slot being taken in 2032 and sitting there.
    num_maximum_advance_days integer NOT NULL DEFAULT 90,
    txt_location_kind        varchar(32) NOT NULL DEFAULT 'VIDEO',
    bln_is_active            boolean NOT NULL DEFAULT true,
    bln_is_deleted           boolean NOT NULL DEFAULT false,
    created_date             timestamptz NOT NULL DEFAULT now(),
    updated_date             timestamptz NOT NULL DEFAULT now(),
    created_by               integer NOT NULL DEFAULT 0,
    updated_by               integer NOT NULL DEFAULT 0,

    CONSTRAINT ck_consultation_type_duration
        CHECK (num_duration_minutes BETWEEN 5 AND 480),
    CONSTRAINT ck_consultation_type_advance
        CHECK (num_maximum_advance_days BETWEEN 1 AND 730)
);


-- ---------------------------------------------------------------------
-- When a host is normally available: recurring, weekly, in their own zone
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS consultation_availability_rule (
    ser_availability_rule_id serial PRIMARY KEY,
    ser_host_id     integer NOT NULL REFERENCES consultation_host (ser_host_id),
    -- ISO-8601: Monday is 1, Sunday is 7. Matching java.time.DayOfWeek rather
    -- than Postgres's own 0-6, because the arithmetic happens in Java.
    num_day_of_week integer NOT NULL,
    tme_start_time  time    NOT NULL,
    tme_end_time    time    NOT NULL,
    bln_is_deleted  boolean NOT NULL DEFAULT false,
    created_date    timestamptz NOT NULL DEFAULT now(),
    updated_date    timestamptz NOT NULL DEFAULT now(),
    created_by      integer NOT NULL DEFAULT 0,
    updated_by      integer NOT NULL DEFAULT 0,

    CONSTRAINT ck_availability_day CHECK (num_day_of_week BETWEEN 1 AND 7),
    CONSTRAINT ck_availability_order CHECK (tme_end_time > tme_start_time)
);

CREATE INDEX IF NOT EXISTS ix_availability_rule_host
    ON consultation_availability_rule (ser_host_id) WHERE bln_is_deleted = false;


-- ---------------------------------------------------------------------
-- One-off departures from the weekly pattern
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS consultation_availability_exception (
    ser_availability_exception_id serial PRIMARY KEY,
    ser_host_id    integer NOT NULL REFERENCES consultation_host (ser_host_id),
    dte_on_date    date    NOT NULL,
    -- false closes the day (bank holiday, leave); true opens hours that the
    -- weekly rules do not cover (a Saturday, for one week only).
    bln_is_available boolean NOT NULL DEFAULT false,
    tme_start_time time,
    tme_end_time   time,
    txt_reason     varchar(255),
    bln_is_deleted boolean NOT NULL DEFAULT false,
    created_date   timestamptz NOT NULL DEFAULT now(),
    updated_date   timestamptz NOT NULL DEFAULT now(),
    created_by     integer NOT NULL DEFAULT 0,
    updated_by     integer NOT NULL DEFAULT 0,

    -- An opening has to say when. A closure covers the whole day.
    CONSTRAINT ck_exception_hours CHECK (
        (bln_is_available = false)
        OR (tme_start_time IS NOT NULL AND tme_end_time IS NOT NULL
            AND tme_end_time > tme_start_time)
    )
);

CREATE INDEX IF NOT EXISTS ix_availability_exception_host_date
    ON consultation_availability_exception (ser_host_id, dte_on_date)
    WHERE bln_is_deleted = false;


-- ---------------------------------------------------------------------
-- A connected Google or Microsoft account
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS calendar_connection (
    ser_calendar_connection_id serial PRIMARY KEY,
    ser_host_id      integer NOT NULL REFERENCES consultation_host (ser_host_id),
    txt_provider     varchar(32)  NOT NULL,
    txt_account_email varchar(255) NOT NULL,
    -- Which calendar in that account. A person routinely has several.
    txt_calendar_id  varchar(512),
    txt_calendar_name varchar(255),
    -- Encrypted before they get here. A refresh token in plaintext is a
    -- standing grant to read and write the team's calendars to anyone who
    -- reaches the database or a backup of it.
    txt_access_token_encrypted  text,
    txt_refresh_token_encrypted text,
    dte_token_expires_at timestamptz,
    txt_sync_token   text,
    dte_last_synced_at timestamptz,
    -- HEALTHY, NEEDS_RECONNECT, ERROR. A revoked token has to be visible to
    -- an administrator rather than showing up as slots quietly going wrong.
    txt_sync_status  varchar(32) NOT NULL DEFAULT 'HEALTHY',
    txt_sync_error   text,
    bln_is_deleted   boolean NOT NULL DEFAULT false,
    created_date     timestamptz NOT NULL DEFAULT now(),
    updated_date     timestamptz NOT NULL DEFAULT now(),
    created_by       integer NOT NULL DEFAULT 0,
    updated_by       integer NOT NULL DEFAULT 0,

    CONSTRAINT ck_calendar_provider CHECK (txt_provider IN ('GOOGLE', 'MICROSOFT'))
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_calendar_connection_account
    ON calendar_connection (ser_host_id, txt_provider, lower(txt_account_email))
    WHERE bln_is_deleted = false;


-- ---------------------------------------------------------------------
-- Busy periods imported from a connected calendar
-- ---------------------------------------------------------------------
-- Only the times, never the subject or the attendees: blocking a slot needs
-- to know that the host is busy and nothing else about why.
CREATE TABLE IF NOT EXISTS calendar_busy_block (
    ser_busy_block_id serial PRIMARY KEY,
    ser_calendar_connection_id integer NOT NULL
        REFERENCES calendar_connection (ser_calendar_connection_id) ON DELETE CASCADE,
    ser_host_id     integer NOT NULL REFERENCES consultation_host (ser_host_id),
    dte_starts_at   timestamptz NOT NULL,
    dte_ends_at     timestamptz NOT NULL,
    txt_external_id varchar(512),
    dte_imported_at timestamptz NOT NULL DEFAULT now(),

    CONSTRAINT ck_busy_block_order CHECK (dte_ends_at > dte_starts_at)
);

CREATE INDEX IF NOT EXISTS ix_busy_block_host_window
    ON calendar_busy_block (ser_host_id, dte_starts_at, dte_ends_at);


-- ---------------------------------------------------------------------
-- The meeting
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS consultation_booking (
    ser_consultation_booking_id serial PRIMARY KEY,
    ser_host_id     integer NOT NULL REFERENCES consultation_host (ser_host_id),
    ser_consultation_type_id integer NOT NULL
        REFERENCES consultation_type (ser_consultation_type_id),
    ser_cust_id     integer,
    -- The booking it is about, where there is one. A consultation can also be
    -- arranged by staff with no event behind it.
    ser_event_master_id integer,

    txt_customer_name  varchar(255) NOT NULL,
    txt_customer_email varchar(255) NOT NULL,
    txt_customer_phone varchar(64),
    txt_notes          text,

    dte_starts_at   timestamptz NOT NULL,
    dte_ends_at     timestamptz NOT NULL,
    -- What the customer saw, so a confirmation can be re-rendered in the zone
    -- they booked in rather than ours.
    txt_customer_time_zone varchar(64),

    -- BOOKED, CANCELLED, COMPLETED, NO_SHOW.
    txt_status      varchar(32) NOT NULL DEFAULT 'BOOKED',
    txt_cancellation_reason text,
    dte_cancelled_at timestamptz,

    -- Single-use, unguessable, so the cancel and reschedule links in the
    -- confirmation email do not need the customer to be signed in.
    txt_management_token varchar(64),

    -- The event this created in the host's own calendar, so it can be moved
    -- or removed when the booking changes.
    txt_external_event_id varchar(512),
    txt_external_sync_status varchar(32) NOT NULL DEFAULT 'PENDING',
    txt_external_sync_error  text,

    bln_is_deleted  boolean NOT NULL DEFAULT false,
    created_date    timestamptz NOT NULL DEFAULT now(),
    updated_date    timestamptz NOT NULL DEFAULT now(),
    created_by      integer NOT NULL DEFAULT 0,
    updated_by      integer NOT NULL DEFAULT 0,

    CONSTRAINT ck_booking_order CHECK (dte_ends_at > dte_starts_at),
    CONSTRAINT ck_booking_status
        CHECK (txt_status IN ('BOOKED', 'CANCELLED', 'COMPLETED', 'NO_SHOW'))
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_booking_management_token
    ON consultation_booking (txt_management_token)
    WHERE txt_management_token IS NOT NULL;

CREATE INDEX IF NOT EXISTS ix_booking_host_window
    ON consultation_booking (ser_host_id, dte_starts_at, dte_ends_at)
    WHERE txt_status = 'BOOKED';

CREATE INDEX IF NOT EXISTS ix_booking_event
    ON consultation_booking (ser_event_master_id)
    WHERE ser_event_master_id IS NOT NULL;


-- ---------------------------------------------------------------------
-- The constraint this whole file exists for
-- ---------------------------------------------------------------------
-- One host cannot hold two live bookings that overlap.
--
-- This is the difference between a scheduling system and something that looks
-- like one. The service layer checks for a clash before inserting, and that
-- check is worth having because it produces a civil "that slot has just gone"
-- instead of a database error — but it cannot be what guarantees it. Between
-- reading and writing is precisely where the other booking arrives, and no
-- amount of application code closes a gap that exists by definition. Two
-- customers pressing the same slot in the same second is not an unlikely
-- accident; on a popular Saturday it is the expected case.
--
-- `&&` is range overlap, so a meeting ending exactly when the next begins is
-- allowed — tstzrange is half-open, [start, end).
--
-- Scoped to BOOKED: a cancelled meeting must not keep its slot reserved.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'ex_consultation_booking_no_overlap'
    ) THEN
        ALTER TABLE consultation_booking
            ADD CONSTRAINT ex_consultation_booking_no_overlap
            EXCLUDE USING gist (
                ser_host_id WITH =,
                tstzrange(dte_starts_at, dte_ends_at) WITH &&
            ) WHERE (txt_status = 'BOOKED' AND bln_is_deleted = false);
    END IF;
END $$;
