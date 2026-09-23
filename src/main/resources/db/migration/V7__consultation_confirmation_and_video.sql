-- =====================================================================
-- V7 — Requested-then-confirmed meetings, and video links
-- =====================================================================
--
-- Two settings per consultation type, both asked for by the business:
--
--   * whether a request has to be confirmed by the team before it counts;
--   * whether confirming it creates a Meet or Teams link.
--
-- Per type rather than global, which is how Calendly and Cal.com both do it:
-- an initial enquiry can confirm instantly while a site visit needs somebody
-- to agree to it, and those are the same system with a different switch.
--
-- The default is instant confirmation. A customer who has just finished a
-- fourteen-step booking journey is at the point of most commitment, and
-- "somebody will confirm this later" is where that commitment goes to die —
-- along with any request made on a Friday evening. Requiring confirmation is
-- a real feature; it is not a good default.
-- =====================================================================

ALTER TABLE consultation_type
    ADD COLUMN IF NOT EXISTS bln_requires_confirmation boolean NOT NULL DEFAULT false;

ALTER TABLE consultation_type
    ADD COLUMN IF NOT EXISTS bln_create_video_link boolean NOT NULL DEFAULT true;

-- How long an unanswered request keeps its slot.
--
-- This is the part of "request, then confirm" that is easy to miss. A pending
-- request has to hold the slot, or the team can confirm a meeting into a time
-- that somebody else booked while they were deciding. But a hold with no
-- expiry means one request nobody answers quietly removes a slot from sale
-- for good. So it holds, and it lapses.
ALTER TABLE consultation_type
    ADD COLUMN IF NOT EXISTS num_confirmation_window_hours integer NOT NULL DEFAULT 48;


-- ---------------------------------------------------------------------
-- The booking gains a pending state, and somewhere to keep the video link
-- ---------------------------------------------------------------------
ALTER TABLE consultation_booking
    ADD COLUMN IF NOT EXISTS dte_hold_expires_at timestamptz;

ALTER TABLE consultation_booking
    ADD COLUMN IF NOT EXISTS txt_video_join_url text;

ALTER TABLE consultation_booking
    ADD COLUMN IF NOT EXISTS dte_confirmed_at timestamptz;

ALTER TABLE consultation_booking
    ADD COLUMN IF NOT EXISTS txt_declined_reason text;


-- PENDING joins the statuses. DECLINED is the team saying no, which is not
-- the same as the customer cancelling and should not read as if it were.
ALTER TABLE consultation_booking
    DROP CONSTRAINT IF EXISTS ck_booking_status;

ALTER TABLE consultation_booking
    ADD CONSTRAINT ck_booking_status CHECK (
        txt_status IN ('PENDING', 'BOOKED', 'CANCELLED', 'DECLINED', 'COMPLETED', 'NO_SHOW')
    );


-- ---------------------------------------------------------------------
-- A pending request holds its slot
-- ---------------------------------------------------------------------
-- The exclusion constraint covered BOOKED only. With requests in the picture
-- that leaves the exact gap this whole design exists to close: two customers
-- could both have a pending request for the same slot, and the team could
-- confirm both.
--
-- PENDING now reserves the slot on the same terms as BOOKED. An expired hold
-- is swept back to DECLINED by the application, which releases it.
DO $$
BEGIN
    ALTER TABLE consultation_booking
        DROP CONSTRAINT IF EXISTS ex_consultation_booking_no_overlap;

    ALTER TABLE consultation_booking
        ADD CONSTRAINT ex_consultation_booking_no_overlap
        EXCLUDE USING gist (
            ser_host_id WITH =,
            tstzrange(dte_starts_at, dte_ends_at) WITH &&
        ) WHERE (txt_status IN ('BOOKED', 'PENDING') AND bln_is_deleted = false);
END $$;

-- Finding holds that have lapsed, without scanning the table.
CREATE INDEX IF NOT EXISTS ix_booking_hold_expiry
    ON consultation_booking (dte_hold_expires_at)
    WHERE txt_status = 'PENDING';
