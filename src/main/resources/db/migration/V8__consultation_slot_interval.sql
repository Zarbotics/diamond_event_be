-- How far apart the offered start times are.
--
-- This was a constant in ServiceConsultationImpl — SLOT_STEP_MINUTES = 30 —
-- which meant an hour-long meeting was always offered on the hour and the
-- half-hour. That is a reasonable default and a poor rule: a venue visit may
-- want hourly starts only, and a fifteen-minute call wants quarter-hours.
--
-- It belongs with the other things that describe a kind of meeting, next to
-- its duration and its buffers, so the portal can change it without a release.
--
-- Note this is NOT the same as the duration. A 60-minute meeting on a
-- 30-minute interval is offered at 09:00, 09:30, 10:00 ... — overlapping
-- candidates, of which the booked one removes its neighbours. That is how
-- these systems normally behave and is why an empty three-hour morning offers
-- five hour-long starts rather than three.
ALTER TABLE consultation_type
    ADD COLUMN IF NOT EXISTS num_slot_interval_minutes integer NOT NULL DEFAULT 30;

DO $$
BEGIN
    ALTER TABLE consultation_type
        DROP CONSTRAINT IF EXISTS ck_consultation_type_interval;

    ALTER TABLE consultation_type
        ADD CONSTRAINT ck_consultation_type_interval
        CHECK (num_slot_interval_minutes BETWEEN 5 AND 480);
END $$;
