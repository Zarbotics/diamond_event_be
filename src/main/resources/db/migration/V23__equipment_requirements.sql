-- What an event needs on the day, worked out from what was sold.
--
-- THE PROBLEM THIS SOLVES
--
-- A wedding for 300 with a plated main, a dessert buffet and a grazing bar
-- needs 300 dinner plates, a stack of dessert bowls, boards and tongs for the
-- grazing station, linen for 30 tables, and chafing dishes for the buffet.
-- Somebody works that out by hand, every time, from the menu — and gets it
-- wrong occasionally, which is how a hundred guests end up sharing cutlery.
--
-- It is arithmetic on things the system already knows: the menu chosen, the
-- guest count, the table count. What it lacks is somebody having written down,
-- once, what each dish implies.
--
-- WHAT IS DELIBERATELY NOT HERE
--
-- Stock. This calculates what is needed; it does not claim to know what is in
-- the cupboard, and it draws no distinction between equipment the venue owns
-- and equipment it hires. Those are real problems and a different system —
-- putting a half-built version of them here would mean a screen that looks
-- like it knows whether you have 300 chargers when it does not.
--
-- WHAT REPLACES WHAT
--
-- The itinerary tables — itinerary_item, itinerary_item_type,
-- itinerary_assignment, itinerary_assignment_detail, menu_item_itinerary_map,
-- event_menu_itinerary, event_itinerary_summary, event_itinerary_result — were
-- an earlier attempt at exactly this. Every one of them is empty.
--
-- They are not dropped here. This migration adds the replacement and leaves
-- the old tables in place, so the two can be compared on a real database
-- before anything is thrown away. A later migration removes them once this
-- is in use.
--
-- WHY THE OLD SHAPE IS NOT BEING REUSED
--
-- It said the same thing twice. An equipment rule could live in
-- itinerary_assignment_detail (with a multiplier from an enum of PER_GUEST,
-- PER_TABLE, PER_ITEM, PER_PORTION, FLAT) or in menu_item_itinerary_map (with
-- a free-text multiplier documented as PER_GUEST|PER_DISH|PER_STATION|FIXED).
-- Two tables, two vocabularies, neither a subset of the other, and no rule
-- about which wins. PER_STATION — the one a grazing bar actually needs —
-- existed only in the half that was free text.
--
-- Here there is one table and one vocabulary.

-- ── The catalogue ─────────────────────────────────────────────────────

CREATE TABLE equipment_category (
    ser_equipment_category_id   SERIAL PRIMARY KEY,
    txt_name                    VARCHAR(120) NOT NULL,
    txt_description             TEXT,
    num_display_order           INTEGER,
    bln_is_active               BOOLEAN DEFAULT TRUE,
    bln_is_deleted              BOOLEAN DEFAULT FALSE,
    bln_is_approved             BOOLEAN,
    created_by                  INTEGER,
    created_date                TIMESTAMP(6),
    updated_by                  INTEGER,
    updated_date                TIMESTAMP(6)
);

CREATE UNIQUE INDEX ux_equipment_category_name
    ON equipment_category (LOWER(txt_name))
    WHERE bln_is_deleted = false;

CREATE TABLE equipment_item (
    ser_equipment_item_id       SERIAL PRIMARY KEY,
    ser_equipment_category_id   INTEGER REFERENCES equipment_category (ser_equipment_category_id),

    txt_code                    VARCHAR(60),
    txt_name                    VARCHAR(160) NOT NULL,
    txt_description             TEXT,

    -- What one of them is: "each", "pair", "set of 6". Printed beside the
    -- number so a picking list saying "12 linen" says 12 of what.
    txt_unit                    VARCHAR(40) DEFAULT 'each',

    num_display_order           INTEGER,
    bln_is_active               BOOLEAN DEFAULT TRUE,
    bln_is_deleted              BOOLEAN DEFAULT FALSE,
    bln_is_approved             BOOLEAN,
    created_by                  INTEGER,
    created_date                TIMESTAMP(6),
    updated_by                  INTEGER,
    updated_date                TIMESTAMP(6)
);

CREATE UNIQUE INDEX ux_equipment_item_name
    ON equipment_item (LOWER(txt_name))
    WHERE bln_is_deleted = false;

-- ── The rules ─────────────────────────────────────────────────────────
--
-- One row is one sentence: "a dessert buffet needs 1 dessert stand per
-- station", "every event needs 1 tablecloth per table".

CREATE TABLE equipment_requirement (
    -- BIGSERIAL, not SERIAL: the entity holds this as a Long, and Hibernate
    -- validates the schema against the mapping at startup. A mismatch stops
    -- the whole application rather than just this table.
    ser_equipment_requirement_id BIGSERIAL PRIMARY KEY,

    -- WHAT TRIGGERS IT
    --
    -- Either a dish, or nothing at all. A requirement with a menu item
    -- applies when that dish is on the menu; one without applies to every
    -- event, which is how "a tablecloth per table" gets said once instead of
    -- against all 238 dishes.
    --
    -- The check below is what stops a third kind of row appearing later and
    -- nobody knowing what it means.
    ser_menu_item_id            BIGINT REFERENCES menu_item (ser_menu_item_id),
    bln_applies_to_every_event  BOOLEAN NOT NULL DEFAULT FALSE,

    ser_equipment_item_id       INTEGER NOT NULL REFERENCES equipment_item (ser_equipment_item_id),

    -- HOW MANY, AND OF WHAT
    --
    -- PER_GUEST    quantity x guests        — a dinner plate each
    -- PER_TABLE    quantity x tables        — two candelabra per table
    -- PER_STATION  quantity x stations      — tongs on a grazing bar
    -- PER_EVENT    quantity                 — one cake knife, however many come
    --
    -- Decimal on purpose. Half a metre of runner per guest is a real rule,
    -- and so is "one chafing dish per 40 guests" expressed as 0.025.
    num_quantity                NUMERIC(12, 4) NOT NULL DEFAULT 1,
    enm_basis                   VARCHAR(20) NOT NULL,

    -- HOW MANY STATIONS THERE ARE
    --
    -- Only read for PER_STATION. A grazing bar is not one thing for 300
    -- people: the venue puts out another every so many guests, and this says
    -- how often. Null means one station regardless of size.
    num_guests_per_station      INTEGER,

    -- BREAKAGE AND SPARES
    --
    -- Added after the multiplication and before rounding up, so 300 plates at
    -- 5% is 315. Null is none. It exists because sending exactly 300 plates
    -- to a 300-cover wedding is how a table ends up short.
    num_spare_percent           NUMERIC(6, 3),

    txt_notes                   TEXT,

    bln_is_active               BOOLEAN DEFAULT TRUE,
    bln_is_deleted              BOOLEAN DEFAULT FALSE,
    bln_is_approved             BOOLEAN,
    created_by                  INTEGER,
    created_date                TIMESTAMP(6),
    updated_by                  INTEGER,
    updated_date                TIMESTAMP(6),

    CONSTRAINT ck_equipment_requirement_basis
        CHECK (enm_basis IN ('PER_GUEST', 'PER_TABLE', 'PER_STATION', 'PER_EVENT')),

    -- A rule is about a dish or about every event, never both and never
    -- neither. Without this, a row with no menu item and the flag false is a
    -- rule that can never fire, and nobody would notice it had been saved.
    CONSTRAINT ck_equipment_requirement_source
        CHECK ((ser_menu_item_id IS NOT NULL AND bln_applies_to_every_event = FALSE)
            OR (ser_menu_item_id IS NULL AND bln_applies_to_every_event = TRUE)),

    -- Quantities are positive. A rule for zero of something is a rule that
    -- should have been deleted.
    CONSTRAINT ck_equipment_requirement_quantity
        CHECK (num_quantity > 0),

    CONSTRAINT ck_equipment_requirement_station_size
        CHECK (num_guests_per_station IS NULL OR num_guests_per_station > 0),

    CONSTRAINT ck_equipment_requirement_spare
        CHECK (num_spare_percent IS NULL OR num_spare_percent >= 0)
);

CREATE INDEX ix_equipment_requirement_menu_item
    ON equipment_requirement (ser_menu_item_id)
    WHERE bln_is_deleted = false;

CREATE INDEX ix_equipment_requirement_every_event
    ON equipment_requirement (bln_applies_to_every_event)
    WHERE bln_is_deleted = false AND bln_applies_to_every_event = true;

-- ── Seeded categories ─────────────────────────────────────────────────
--
-- The groupings any catering operation uses. Items are left to the business,
-- because a venue's crockery is its own; the headings are not.

INSERT INTO equipment_category (txt_name, txt_description, num_display_order,
                                bln_is_active, bln_is_deleted, created_date)
VALUES
    ('Crockery',  'Plates, bowls, cups and saucers.',                    10, true, false, now()),
    ('Cutlery',   'Knives, forks, spoons and serving utensils.',         20, true, false, now()),
    ('Glassware', 'Glasses, jugs and decanters.',                        30, true, false, now()),
    ('Linen',     'Tablecloths, napkins, runners and skirting.',         40, true, false, now()),
    ('Serveware', 'Platters, boards, stands, tongs and chafing dishes.', 50, true, false, now()),
    ('Furniture', 'Tables, staging and anything the equipment sits on.', 60, true, false, now()),
    ('Other',     'Anything that does not fit the headings above.',      90, true, false, now());
