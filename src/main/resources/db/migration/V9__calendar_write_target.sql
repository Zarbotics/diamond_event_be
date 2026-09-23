-- Which of a host's connected calendars consultations are written to.
--
-- Busy times are read from every calendar somebody has connected: a person with
-- work in Outlook and their own life in Google is genuinely unavailable for
-- both, and reading only one produces a system that books over their dentist.
--
-- Writing is the opposite. A consultation goes in exactly one calendar, because
-- writing it to several means the same meeting exists two or three times and
-- every later change has to find and match all the copies. That is where
-- duplicated meetings and sync loops come from.
ALTER TABLE calendar_connection
    ADD COLUMN IF NOT EXISTS bln_is_write_target boolean NOT NULL DEFAULT false;

-- At most one write target per host, enforced by the database rather than by
-- remembering to clear the old one. Two would mean every consultation written
-- twice, which is exactly the failure the single-writer rule exists to prevent.
CREATE UNIQUE INDEX IF NOT EXISTS ux_calendar_connection_write_target
    ON calendar_connection (ser_host_id)
    WHERE bln_is_write_target = true AND bln_is_deleted = false;
