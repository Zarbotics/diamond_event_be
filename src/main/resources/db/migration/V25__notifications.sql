-- Notifications that reach the person who needs to know.
--
-- THE PROBLEM THIS SOLVES
--
-- `notification_master` holds zero rows. Not zero unread — zero, in a
-- production database with 296 bookings on it. Whatever the feature was meant
-- to do, it has never done it for anybody.
--
-- Reading the code says why, and it is not that nothing fires. Two things
-- fire: a new event, and a new customer. Both of them call
--
--     createNotification(ServiceCurrentUser.getCurrentUserId(), ...)
--
-- which addresses the notification to *the person who just did the thing*.
-- Telling somebody what they themselves have this second done is a receipt,
-- not a notification. And when the thing is done in the customer journey the
-- current user is the customer, so a booking arriving from the website
-- notified the customer who made it and nobody in the office at all.
--
-- The target it carries is an API path — "/eventMaster/getByEventId123" —
-- rather than a route in the portal, so following one would go nowhere a
-- person can use.
--
-- WHAT A NOTIFICATION IS FOR
--
-- Somebody who was not watching needs to know something happened. That gives
-- the shape: it is addressed to an audience rather than to an actor, it says
-- what happened and to what, it can be followed to the thing itself, and it
-- has a state per person — read is a fact about a reader, not about an event.
--
-- WHY ONE ROW PER RECIPIENT
--
-- Read, unread and dismissed are personal. Two members of staff seeing the
-- same booking arrive have their own state on it, and a shared row cannot
-- carry that without a second table holding exactly what this one holds. The
-- cost is a handful of rows per event on a business with two members of
-- staff, which is not a cost.
--
-- WHAT IS DELIBERATELY NOT HERE
--
-- `notification_master` is untouched and still stands. It is read by the SSE
-- registration path and by the portal's current dropdown; ripping it out in
-- the same change that builds the replacement would mean two things failing
-- at once with no way to tell which. It goes when this is in use.
--
-- Also not here: email and push. This is the in-application bell. Whether a
-- notification should also leave the building is a question about
-- preferences, quiet hours and unsubscribes, and answering it badly is worse
-- than not answering it.

CREATE TABLE notification (
    ser_notification_id     BIGSERIAL PRIMARY KEY,

    -- Who is being told. One row each.
    ser_user_id             BIGINT NOT NULL
        REFERENCES user_master (ser_user_id) ON DELETE CASCADE,

    -- What kind of thing happened. Used for grouping and for filtering, so it
    -- is a fixed vocabulary rather than free text — the old column was free
    -- text and had already acquired one value in two spellings.
    txt_category            VARCHAR(40) NOT NULL,

    -- How much it matters. Three levels: most things are NORMAL, a few are
    -- URGENT and have to survive being scrolled past, and the rest are LOW and
    -- exist to be findable rather than to interrupt.
    txt_priority            VARCHAR(20) NOT NULL DEFAULT 'NORMAL',

    txt_title               VARCHAR(200) NOT NULL,
    txt_body                VARCHAR(500),

    -- What it is about, so the portal can route to it and so a booking that is
    -- deleted can take its notifications with it. The route is a portal path,
    -- not an API one.
    txt_entity_type         VARCHAR(40),
    num_entity_id           BIGINT,
    txt_route               VARCHAR(300),

    /*
      Who caused it, where a person did. Null for anything the system decided
      on its own, which is how "the system noticed" is told apart from
      "somebody did this".

      Deliberately not a foreign key. It is a label on a record of something
      that has already happened, and a constraint here can only ever do one
      thing: refuse to record it. An actor who is not in user_master — a
      service account, a session from a system that has since been unwound, a
      test — is a slightly poorer notification, not a reason to lose one.
    */
    ser_actor_user_id       BIGINT,

    -- Read is a timestamp rather than a flag, because "when did the office
    -- first see this" is a question worth being able to answer and a boolean
    -- throws it away. Dismissed is separate: a notification can be read and
    -- still wanted in the list.
    dte_read_on             TIMESTAMP(6),
    dte_dismissed_on        TIMESTAMP(6),

    /*
      Notifications that are the same thing happening repeatedly collapse on
      this. Twelve payments against one booking in an afternoon is one line
      that says twelve, not twelve lines — which is the difference between a
      bell somebody reads and a bell somebody turns off.
    */
    txt_group_key           VARCHAR(120),

    bln_is_active           BOOLEAN DEFAULT TRUE,
    bln_is_deleted          BOOLEAN DEFAULT FALSE,
    bln_is_approved         BOOLEAN,
    created_by              INTEGER,
    created_date            TIMESTAMP(6),
    updated_by              INTEGER,
    updated_date            TIMESTAMP(6),

    CONSTRAINT ck_notification_priority
        CHECK (txt_priority IN ('LOW', 'NORMAL', 'URGENT')),

    CONSTRAINT ck_notification_category
        CHECK (txt_category IN (
            'BOOKING_TAKEN',        -- a customer submitted an enquiry
            'BOOKING_CHANGED',      -- an existing booking was edited
            'PAYMENT_RECEIVED',
            'CONSULTATION_BOOKED',
            'CONSULTATION_CANCELLED',
            'CAPACITY_REACHED',     -- a day is now full
            'SYSTEM'
        ))
);

/*
  The index the bell uses, and the only query that runs on every page load:
  this user's undismissed notifications, newest first. Partial, because a
  dismissed notification is never in that list and there will eventually be
  far more of those than live ones.
*/
CREATE INDEX ix_notification_inbox
    ON notification (ser_user_id, created_date DESC)
    WHERE dte_dismissed_on IS NULL AND bln_is_deleted = FALSE;

/* The unread count, which the bell asks for separately and often. */
CREATE INDEX ix_notification_unread
    ON notification (ser_user_id)
    WHERE dte_read_on IS NULL AND dte_dismissed_on IS NULL AND bln_is_deleted = FALSE;

/* Collapsing repeats needs to find the open one for a group quickly. */
CREATE INDEX ix_notification_group
    ON notification (ser_user_id, txt_group_key)
    WHERE txt_group_key IS NOT NULL AND dte_dismissed_on IS NULL;

COMMENT ON TABLE notification IS
    'In-application notifications, one row per recipient. Replaces notification_master, which was addressed to the actor rather than to an audience and consequently held nothing.';
