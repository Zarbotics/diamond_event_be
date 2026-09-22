-- The server prices the booking, and the office decides how VAT works.
--
-- THE PROBLEM THIS SOLVES
--
-- Nothing on the server works out what an event costs. Every figure stored
-- against a booking — the décor amount, the food amount, the VAT, the quoted
-- price, the final amount — arrives from a browser and is written down as
-- given.
--
-- The office's rules live in eventPayload.js: five hundred lines of
-- JavaScript deciding what VAT is charged on, whether a décor category price
-- replaces its properties, which food categories share a price. The customer
-- journey has its own, separate arithmetic, in different files, computing
-- subtotals its own way. The two have never been checked against each other
-- because there is nowhere they could be.
--
-- Two things follow. A customer's browser can post any price it likes and the
-- server will keep it. And the journey never posts a quoted total at all, so
-- every enquiry a customer submits is stored quoted at zero and re-priced by
-- hand in the office afterwards.
--
-- WHY THE PRICE IS STORED AND NOT JUST CALCULATED ON DEMAND
--
-- Because a quote given to a customer is a promise. If the price were derived
-- on every read, raising the price of lamb next March would silently raise
-- every quote issued before it, including the ones already accepted. The
-- catalogue price is what a dish costs today; the stored line price is what
-- this customer was told, and they are not the same fact.
--
-- WHY OVERRIDES ARE MARKED RATHER THAN JUST WRITTEN
--
-- The office can and should be able to say "this one is £400 because I agreed
-- it with them on the phone". Storing that as if the engine produced it loses
-- the only thing anybody afterwards needs to know: that a person decided it.
--
-- WHAT IS DELIBERATELY NOT HERE
--
-- The old price_version / price_entry / pricing_rule tables are untouched.
-- They back a preview endpoint that nothing in either portal calls, and
-- folding them in while also moving pricing off the client would be two
-- changes at once with no way to tell which broke a quote.

-- ── A setting that offers a choice ────────────────────────────────────
--
-- V21 gave settings four types: BOOLEAN, INTEGER, DECIMAL, STRING. VAT needs
-- a fifth. "Which things is VAT charged on" is one of a small fixed set of
-- answers, and a free text box would let somebody type "Totl" and turn VAT
-- off across the business without a word of complaint.

ALTER TABLE app_setting
    ADD COLUMN IF NOT EXISTS txt_allowed_values TEXT;

COMMENT ON COLUMN app_setting.txt_allowed_values IS
    'For CHOICE settings: the permitted values, comma separated, in the order they should be offered.';

ALTER TABLE app_setting DROP CONSTRAINT IF EXISTS ck_app_setting_value_type;
ALTER TABLE app_setting ADD CONSTRAINT ck_app_setting_value_type
    CHECK (txt_value_type IN ('BOOLEAN', 'INTEGER', 'DECIMAL', 'STRING', 'CHOICE'));

-- ── How a thing is priced ─────────────────────────────────────────────
--
-- PER_GUEST     multiply by the guest count
-- PER_TABLE     multiply by the table count
-- PER_STATION   one station per N guests, rounded up, multiply by that
-- FLAT          the price is the price, whoever comes
--
-- The same four words the equipment rules use (V23), deliberately. A grazing
-- bar that needs one station per fifty guests for its boards and tongs is
-- charged for on the same basis, and a business that has learned one
-- vocabulary should not have to learn a second one for money.

ALTER TABLE menu_item
    ADD COLUMN IF NOT EXISTS num_guests_per_station NUMERIC(10, 2);

COMMENT ON COLUMN menu_item.num_guests_per_station IS
    'For PER_STATION pricing: how many guests one station serves. Ignored otherwise.';

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_name = 'menu_item' AND column_name = 'enm_price_multiplier_type'
                 AND data_type = 'character varying') THEN
        ALTER TABLE menu_item DROP CONSTRAINT IF EXISTS ck_menu_item_multiplier;
        ALTER TABLE menu_item ADD CONSTRAINT ck_menu_item_multiplier
            CHECK (enm_price_multiplier_type IS NULL
                OR enm_price_multiplier_type IN ('PER_GUEST', 'PER_TABLE', 'PER_STATION', 'FLAT'));
    END IF;
END $$;

-- ── What the engine worked out, line by line ──────────────────────────
--
-- One row per priced thing on the booking. It is written afresh on every save
-- and is never edited by hand: it is the engine's working, kept so that a
-- total can be taken apart by whoever is asked to justify it.

CREATE TABLE event_price_line (
    ser_event_price_line_id     BIGSERIAL PRIMARY KEY,
    /*
      Cascades, because a line of working has no meaning without the booking
      it is working out. Without this, deleting an event is blocked by rows
      that exist only to explain it — and every teardown, every cleanup
      script and every future test has to know that.
    */
    ser_event_master_id         INTEGER NOT NULL
        REFERENCES event_master (ser_event_master_id) ON DELETE CASCADE,

    -- FOOD | DECOR | DECOR_PROPERTY | EXTRA | SERVICE | ITINERARY | SERVING_DISHES
    txt_section                 VARCHAR(30) NOT NULL,

    -- What was priced, named as a person would name it. Denormalised on
    -- purpose: the catalogue row may be renamed or withdrawn later, and the
    -- quote has to keep reading the way it read when it was sent.
    txt_description             VARCHAR(255) NOT NULL,
    num_source_id               BIGINT,

    num_unit_price              NUMERIC(12, 2) NOT NULL DEFAULT 0,
    enm_basis                   VARCHAR(20) NOT NULL DEFAULT 'FLAT',
    num_quantity                NUMERIC(12, 4) NOT NULL DEFAULT 1,
    num_line_total              NUMERIC(12, 2) NOT NULL DEFAULT 0,

    -- Was this figure decided by a person rather than by the rules, and what
    -- would the rules have said? Both, so the difference can be seen.
    bln_is_overridden           BOOLEAN NOT NULL DEFAULT FALSE,
    num_catalogue_total         NUMERIC(12, 2),

    -- Whether VAT was charged on this line, and how much. Per line rather
    -- than per section, because the office can make VAT apply to some
    -- sections and not others and a total that cannot show which lines
    -- carried the VAT cannot be checked against a return.
    bln_is_vatable              BOOLEAN NOT NULL DEFAULT FALSE,
    num_vat                     NUMERIC(12, 2) NOT NULL DEFAULT 0,

    -- The sentence shown next to the number. Built once, here, so the two
    -- portals and the PDF cannot each phrase it differently.
    txt_reason                  VARCHAR(500),

    bln_is_active               BOOLEAN DEFAULT TRUE,
    bln_is_deleted              BOOLEAN DEFAULT FALSE,
    bln_is_approved             BOOLEAN,
    created_by                  INTEGER,
    created_date                TIMESTAMP(6),
    updated_by                  INTEGER,
    updated_date                TIMESTAMP(6),

    CONSTRAINT ck_event_price_line_section
        CHECK (txt_section IN ('FOOD', 'DECOR', 'DECOR_PROPERTY', 'EXTRA',
                               'SERVICE', 'ITINERARY', 'SERVING_DISHES')),
    CONSTRAINT ck_event_price_line_basis
        CHECK (enm_basis IN ('PER_GUEST', 'PER_TABLE', 'PER_STATION', 'FLAT'))
);

CREATE INDEX ix_event_price_line_event
    ON event_price_line (ser_event_master_id)
    WHERE bln_is_deleted = FALSE;

-- ── What the engine made of the whole booking ─────────────────────────
--
-- These sit on the budget beside the figures the client sends, rather than
-- replacing them. For as long as pricing.server.authoritative is off, both
-- are written and only the client's is used — which is what makes it possible
-- to see where the two disagree on real bookings before anything depends on
-- the answer.

ALTER TABLE event_budget
    ADD COLUMN IF NOT EXISTS num_calculated_food         NUMERIC(12, 2),
    ADD COLUMN IF NOT EXISTS num_calculated_decor        NUMERIC(12, 2),
    ADD COLUMN IF NOT EXISTS num_calculated_extras       NUMERIC(12, 2),
    ADD COLUMN IF NOT EXISTS num_calculated_services     NUMERIC(12, 2),
    ADD COLUMN IF NOT EXISTS num_calculated_vat          NUMERIC(12, 2),
    ADD COLUMN IF NOT EXISTS num_calculated_subtotal     NUMERIC(12, 2),
    ADD COLUMN IF NOT EXISTS num_calculated_total        NUMERIC(12, 2),
    ADD COLUMN IF NOT EXISTS dte_calculated_on           TIMESTAMP(6);

COMMENT ON COLUMN event_budget.num_calculated_total IS
    'What the pricing engine made the final amount. Compare with num_final_amount, which is what the client sent.';

-- ── Settings ──────────────────────────────────────────────────────────
--
-- Every default below reproduces exactly what the code does today: VAT at 20%
-- on décor and extras, and on nothing else. Nothing about what any existing
-- booking costs changes when this migration runs.
--
-- THE VAT MODEL
--
-- Whoever runs the business decides. There are three modes:
--
--   NONE      no VAT at all
--   TOTAL     VAT on everything, worked out on the whole bill
--   SECTIONS  VAT on whichever parts are switched on below
--
-- SECTIONS is the default because it is what the system does today, and the
-- six switches beneath it start set to today's answer. A business that is
-- standard-rated across the board switches to TOTAL and never thinks about
-- the six again.

INSERT INTO app_setting (
    txt_key, txt_value, txt_value_type, txt_label, txt_description,
    txt_group, num_display_order, num_min, num_max, txt_allowed_values,
    bln_is_public, bln_is_active, bln_is_deleted, bln_is_approved, created_date
) VALUES

('pricing.server.authoritative', 'false', 'BOOLEAN',
 'Use the prices the system works out',
 'When off, the system still works every price out and records it, but the figures sent by the booking screens are the ones used. Turn this on once the two have been seen to agree. Turning it on also means a customer''s browser can no longer influence what a booking costs.',
 'Pricing', 10, NULL, NULL, NULL, FALSE, TRUE, FALSE, TRUE, NOW()),

('pricing.vat.mode', 'SECTIONS', 'CHOICE',
 'How VAT is charged',
 'NONE charges no VAT at all. TOTAL charges it on the whole bill. SECTIONS charges it only on the parts switched on below.',
 'VAT', 10, NULL, NULL, 'NONE,TOTAL,SECTIONS', FALSE, TRUE, FALSE, TRUE, NOW()),

('pricing.vat.percent', '20', 'DECIMAL',
 'VAT rate (%)',
 'The percentage added as VAT. Used by both TOTAL and SECTIONS.',
 'VAT', 20, 0, 100, NULL, FALSE, TRUE, FALSE, TRUE, NOW()),

-- The six. Each names a part of the bill. They are read only when the mode
-- is SECTIONS, and the screen says so.
('pricing.vat.on.food', 'false', 'BOOLEAN',
 'Charge VAT on food',
 'Only used when VAT is charged by section. Off, which is how the system behaves today — worth checking with whoever does the books, as catering is normally standard-rated in the UK.',
 'VAT', 30, NULL, NULL, NULL, FALSE, TRUE, FALSE, TRUE, NOW()),

('pricing.vat.on.decor', 'true', 'BOOLEAN',
 'Charge VAT on décor',
 'Only used when VAT is charged by section. On, which is how the system behaves today.',
 'VAT', 40, NULL, NULL, NULL, FALSE, TRUE, FALSE, TRUE, NOW()),

('pricing.vat.on.extras', 'true', 'BOOLEAN',
 'Charge VAT on extras',
 'Only used when VAT is charged by section. On, which is how the system behaves today.',
 'VAT', 50, NULL, NULL, NULL, FALSE, TRUE, FALSE, TRUE, NOW()),

('pricing.vat.on.services', 'false', 'BOOLEAN',
 'Charge VAT on services',
 'Only used when VAT is charged by section. Off, which is how the system behaves today.',
 'VAT', 60, NULL, NULL, NULL, FALSE, TRUE, FALSE, TRUE, NOW()),

('pricing.vat.on.itinerary', 'false', 'BOOLEAN',
 'Charge VAT on the itinerary charge',
 'Only used when VAT is charged by section. Off, which is how the system behaves today.',
 'VAT', 70, NULL, NULL, NULL, FALSE, TRUE, FALSE, TRUE, NOW()),

('pricing.vat.on.servingdishes', 'false', 'BOOLEAN',
 'Charge VAT on the serving dishes charge',
 'Only used when VAT is charged by section. Off, which is how the system behaves today.',
 'VAT', 80, NULL, NULL, NULL, FALSE, TRUE, FALSE, TRUE, NOW()),

-- A décor category can carry its own price and so can each option beneath
-- it. Charging both double-charges the customer. The office's JavaScript
-- already assumes the category price wins where it is set; this says so out
-- loud.
('pricing.decor.category.price.replaces.properties', 'true', 'BOOLEAN',
 'A décor category price replaces its options',
 'When a décor category has a price of its own, the prices on the options beneath it are not added as well. This is how the system behaves today.',
 'Pricing', 20, NULL, NULL, NULL, FALSE, TRUE, FALSE, TRUE, NOW())

ON CONFLICT DO NOTHING;
