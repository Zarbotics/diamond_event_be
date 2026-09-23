-- Stage 1 of putting a booking above the event. See PLATFORM.md §15.
--
-- WHAT IS WRONG TODAY
--
-- event_master is doing two jobs. It is the event — a date, a venue, a running
-- order, a guest count, a menu — and it is also the booking: the customer, the
-- contact, the budget, the payments, the consultation.
--
-- For a single-event customer the two coincide and nothing looks wrong. The
-- problem is the case this business trades on: a wedding is a mehndi on the
-- Friday, a nikkah on the Saturday and a walima on the Sunday. One family, one
-- negotiation, one deposit — and three event_master rows that know nothing
-- about each other. Three budgets, no total, a deposit with no row to live on,
-- and cancelling "the wedding" is three separate cancellations of which one can
-- be missed.
--
-- WHAT THIS MIGRATION DOES, AND DELIBERATELY DOES NOT DO
--
-- It creates the table and the column, and backfills one booking per event so
-- that every event that exists today has a parent. Nothing reads either yet:
-- no entity maps the association, no endpoint returns it, no screen shows it.
--
-- That is the point of doing it this way. This stage changes no behaviour at
-- all and is undone by dropping a column, which means it can go to production
-- on its own and sit there while the stages that do change behaviour are built
-- and reviewed separately.
--
-- The column stays nullable. Events created after this migration will have no
-- booking until stage 2 wires the save paths up, and that is expected rather
-- than an oversight — a NOT NULL constraint here would reject every new
-- booking the moment this deployed.

CREATE TABLE IF NOT EXISTS booking (
    ser_booking_id      BIGSERIAL PRIMARY KEY,

    -- The reference a person quotes on the telephone. Backfilled from the
    -- event's own code, so a booking created by this migration can be traced
    -- back to the event it came from without a join.
    txt_booking_code    VARCHAR(255),

    -- Who the booking is with. The thing that makes three events one wedding.
    ser_cust_id         INTEGER REFERENCES customer_master (ser_cust_id),

    -- Optimistic locking, the same as event_master gained in V10. A booking
    -- will eventually carry the budget and the payments, which are exactly the
    -- fields two people edit at once.
    num_version         BIGINT DEFAULT 0 NOT NULL,

    created_date        TIMESTAMP,
    updated_date        TIMESTAMP,
    created_by          INTEGER DEFAULT 0,
    updated_by          INTEGER DEFAULT 0,
    bln_is_deleted      BOOLEAN DEFAULT FALSE,
    bln_is_active       BOOLEAN DEFAULT TRUE,
    bln_is_approved     BOOLEAN DEFAULT FALSE
);

ALTER TABLE event_master
    ADD COLUMN IF NOT EXISTS ser_booking_id BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_event_master_booking'
    ) THEN
        ALTER TABLE event_master
            ADD CONSTRAINT fk_event_master_booking
            FOREIGN KEY (ser_booking_id) REFERENCES booking (ser_booking_id);
    END IF;
END $$;

-- Every read that follows this column goes event → booking or booking →
-- events, and the second of those is a sequential scan without this.
CREATE INDEX IF NOT EXISTS ix_event_master_booking ON event_master (ser_booking_id);

-- The backfill: one booking per event, including deleted ones.
--
-- Deleted events are included on purpose. The invariant worth having is "every
-- event that existed when this ran has a parent", with no exceptions to
-- remember — an exception is what makes a later join quietly drop rows.
--
-- Guarded so that re-running this migration against a database where it has
-- already run is a no-op rather than a second set of bookings.
INSERT INTO booking (
    txt_booking_code, ser_cust_id,
    created_date, updated_date, created_by, updated_by,
    bln_is_deleted, bln_is_active, bln_is_approved
)
SELECT
    e.txt_event_master_code,
    e.ser_cust_id,
    e.created_date,
    e.updated_date,
    COALESCE(e.created_by, 0),
    COALESCE(e.updated_by, 0),
    COALESCE(e.bln_is_deleted, FALSE),
    COALESCE(e.bln_is_active, TRUE),
    COALESCE(e.bln_is_approved, FALSE)
FROM event_master e
WHERE e.ser_booking_id IS NULL;

-- Attach each event to the booking just made for it.
--
-- Matched on the code because that is what was copied into it, and because V5
-- made txt_event_master_code unique. Events with no code — there are none in
-- production, but a NULL would match nothing and silently skip the row — are
-- handled by the id-ordered fallback below.
UPDATE event_master e
SET ser_booking_id = b.ser_booking_id
FROM booking b
WHERE e.ser_booking_id IS NULL
  AND e.txt_event_master_code IS NOT NULL
  AND b.txt_booking_code = e.txt_event_master_code;

-- Anything left: an event with no reference code. Paired with an unclaimed
-- booking rather than left parentless, so the invariant holds without
-- exception.
WITH unclaimed AS (
    SELECT b.ser_booking_id,
           ROW_NUMBER() OVER (ORDER BY b.ser_booking_id) AS n
    FROM booking b
    WHERE NOT EXISTS (SELECT 1 FROM event_master e WHERE e.ser_booking_id = b.ser_booking_id)
),
orphaned AS (
    SELECT e.ser_event_master_id,
           ROW_NUMBER() OVER (ORDER BY e.ser_event_master_id) AS n
    FROM event_master e
    WHERE e.ser_booking_id IS NULL
)
UPDATE event_master e
SET ser_booking_id = u.ser_booking_id
FROM orphaned o
JOIN unclaimed u ON u.n = o.n
WHERE e.ser_event_master_id = o.ser_event_master_id;
