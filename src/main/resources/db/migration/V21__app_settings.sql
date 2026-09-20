-- Things the business configures, in one place.
--
-- WHY A TABLE AND NOT A PROPERTIES FILE
--
-- Because the people who need to change these are not the people who can
-- deploy. "How far ahead do we take bookings" and "are we selling catering
-- this season" are commercial decisions that change on a Tuesday afternoon,
-- and every day they live in Java is a day the business has to ask an engineer
-- for something it should be able to do itself.
--
-- WHY TYPED, AND NOT A BAG OF STRINGS
--
-- A settings table with two columns is a settings table nobody trusts. There
-- is no way to render an editor for it, no way to stop somebody typing "yes"
-- into a number, and no way to tell which of forty rows still matters. So each
-- row declares what it is: its type, the range it will accept, the group it
-- belongs to on screen, and a sentence saying what changing it does.
--
-- The value stays text. Postgres has no union type, the alternative is four
-- nullable value columns that are wrong three ways at once, and the type
-- column is what tells the reader how to parse it.
--
-- WHY THE KEY IS NOT AN ENUM
--
-- Because a deployment adds settings and must not have to migrate existing
-- rows to do it. The application knows the keys it cares about; a key it does
-- not recognise is ignored rather than fatal, which is what makes it safe to
-- add one ahead of the code that reads it.

CREATE TABLE app_setting (
    ser_setting_id      SERIAL PRIMARY KEY,

    -- Dotted and lowercase by convention: catering.booking.enabled.
    txt_key             VARCHAR(120) NOT NULL,

    txt_value           TEXT,

    -- BOOLEAN | INTEGER | DECIMAL | STRING. Checked below rather than left to
    -- convention, because the admin screen renders from it and a typo would
    -- silently produce a text box where a switch belongs.
    txt_value_type      VARCHAR(20)  NOT NULL,

    -- What the office sees. The key is for the code; nobody should have to
    -- read "catering.booking.enabled" to find the catering switch.
    txt_label           VARCHAR(200) NOT NULL,
    txt_description     TEXT,

    -- The heading it appears under.
    txt_group           VARCHAR(80),
    num_display_order   INTEGER,

    -- Bounds for the numeric types. Null means unbounded. These exist so the
    -- refusal is a sentence on the screen rather than an exception three
    -- layers down when something divides by a zero somebody typed.
    num_min             NUMERIC(14, 4),
    num_max             NUMERIC(14, 4),

    -- Whether an unauthenticated or customer-facing caller may read it. The
    -- journey needs to know whether catering is on sale; it has no business
    -- knowing anything else that ends up in here.
    bln_is_public       BOOLEAN      NOT NULL DEFAULT false,

    bln_is_active       BOOLEAN      DEFAULT true,
    bln_is_deleted      BOOLEAN      DEFAULT false,
    -- Carried by BaseEntity, which every entity here extends. Unused for a
    -- setting, but Hibernate validates the schema against the mapping at
    -- startup and a missing column stops the whole application, not just this
    -- table.
    bln_is_approved     BOOLEAN,
    created_by          INTEGER,
    created_date        TIMESTAMP(6),
    updated_by          INTEGER,
    updated_date        TIMESTAMP(6),

    CONSTRAINT ck_app_setting_value_type
        CHECK (txt_value_type IN ('BOOLEAN', 'INTEGER', 'DECIMAL', 'STRING'))
);

-- One row per key among the living. Partial, so a deleted row does not block
-- its key being used again.
CREATE UNIQUE INDEX ux_app_setting_key
    ON app_setting (txt_key)
    WHERE bln_is_deleted = false;

-- ── Seeded settings ───────────────────────────────────────────────────
--
-- Two to begin with, both replacing a decision currently frozen into code.

INSERT INTO app_setting (txt_key, txt_value, txt_value_type, txt_label, txt_description,
                         txt_group, num_display_order, num_min, num_max, bln_is_public,
                         bln_is_active, bln_is_deleted, created_date)
VALUES
    ('catering.booking.enabled', 'false', 'BOOLEAN',
     'Take catering-only bookings',
     'When this is off, customers are not offered catering delivery in the booking journey and the '
     || 'office does not see catering screens. Bookings already taken are not hidden or changed — '
     || 'they stay reachable so nothing in progress is stranded.',
     'Catering', 10, NULL, NULL, true, true, false, now()),

    ('booking.horizon.months', '120', 'INTEGER',
     'How far ahead bookings may be taken (months)',
     'A date beyond this is refused, with a message naming the year. It exists because a stray '
     || 'keypress turns 2027 into 2207, and that booking is accepted, never appears in any diary, '
     || 'never gets chased, and is found years later. 120 months is ten years — far beyond anything '
     || 'plausible, so it rejects only the impossible. Lower it to whatever the business actually '
     || 'takes.',
     'Bookings', 10, 1, 1200, true, true, false, now());
