-- Stage 3 of putting a booking above the event. See PLATFORM.md §15.3.
--
-- WHAT THIS IS FOR
--
-- A deposit is paid for a wedding, not for the Saturday. Today a payment hangs
-- off event_budget, which hangs off one event_master row, so a family paying
-- £2,000 against a mehndi, a nikkah and a walima has to have that money
-- recorded against one of the three days — and every total the office works
-- from is then the total for a day rather than for the wedding.
--
-- One booking per event is still true, so this migration changes nothing
-- anybody can see. That is deliberate and it is the whole method: the column
-- arrives and is filled while it cannot matter, so that stage 4 — "add another
-- day to this wedding" in the journey — is a change to behaviour and not a
-- change to behaviour plus a data migration on live payments.
--
-- WHAT IT DOES NOT DO
--
-- ser_event_budget_id stays, stays NOT NULL, and stays the column every read
-- path uses. Nothing is moved off the budget; a second parent is added beside
-- it. Stage 5 removes the first one, once neither frontend needs it, and is
-- the only stage in this sequence that deletes anything.
--
-- Undone by dropping one column.

ALTER TABLE event_payment
    ADD COLUMN IF NOT EXISTS ser_booking_id BIGINT;

-- Backfill: payment -> budget -> event -> booking.
--
-- Left as a join rather than a correlated subquery so a payment whose budget
-- or event has gone simply does not match, instead of being set to NULL and
-- looking identical to one this migration never reached.
UPDATE event_payment p
SET    ser_booking_id = e.ser_booking_id
FROM   event_budget b
JOIN   event_master e ON e.ser_event_master_id = b.ser_event_master_id
WHERE  p.ser_event_budget_id = b.ser_event_budget_id
  AND  e.ser_booking_id IS NOT NULL
  AND  p.ser_booking_id IS NULL;

-- Nullable, for the same reason it was in V11: payments taken between this
-- migration and the code that fills the column in would be rejected outright
-- by a NOT NULL constraint. The application fills it on write from stage 3
-- onward; anything that slips through is repaired by re-running the UPDATE
-- above, which is written to be safe to run again.
CREATE INDEX IF NOT EXISTS idx_event_payment_booking
    ON event_payment (ser_booking_id);

-- Guarded rather than a bare ADD CONSTRAINT: PostgreSQL has no
-- `IF NOT EXISTS` for constraints, and the rest of this file can be re-run.
-- A migration that is safe to run twice is a migration somebody can repair by
-- hand at two in the morning.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_event_payment_booking'
    ) THEN
        ALTER TABLE event_payment
            ADD CONSTRAINT fk_event_payment_booking
            FOREIGN KEY (ser_booking_id) REFERENCES booking (ser_booking_id);
    END IF;
END $$;
