-- Stage M5 of the food menu work. See PLATFORM.md §17.
--
-- WHAT THIS IS FOR
--
-- "What did this dish cost when the Khans booked in March?" and "put the new
-- prices in now, to take effect on the first of April."
--
-- Neither is answerable today. A menu_offering carries one price, so changing
-- it overwrites what the old one was, and a price rise has to be typed in on
-- the morning it starts.
--
-- WHAT ALREADY EXISTS AND IS BEING KEPT
--
-- price_version: a named, dated, prioritised price list with DRAFT / PUBLISHED
-- / RETIRED. It is empty in production and always has been, but the shape is
-- right and it is exactly what "prepare next year's prices" needs. Reused
-- rather than replaced.
--
-- WHAT ALREADY EXISTS AND IS NOT BEING USED
--
-- price_entry, which prices a *target id* under one of ITEM, ROLE, BUNDLE,
-- COMBINATION, STATION or TYPE, by one of DIRECT, MIN_OF, MAX_OF, SUM or
-- FORMULA. Empty, and a general pricing engine for a business that prices
-- dishes. Two further problems make it the wrong home even if it were wanted:
-- it prices an *item*, which loses the distinction M2 established — a brownie
-- is £3.50 plated and £4.00 on a stand — and nothing in the application has
-- ever read it.
--
-- So the price list is per offering, which is where the price already lives.
--
-- WHAT THIS MIGRATION DOES NOT DO
--
-- Nothing reads it. menu_offering.num_price stays and stays authoritative; this
-- table is filled from it, not the other way round. The stage is undone by
-- dropping a table. M5b moves the reads across, and only then does an
-- offering's own price become a cache of the effective version.

CREATE TABLE IF NOT EXISTS menu_offering_price (
    ser_offering_price_id BIGSERIAL PRIMARY KEY,

    ser_offering_id       BIGINT NOT NULL REFERENCES menu_offering (ser_offering_id),
    ser_price_version_id  BIGINT NOT NULL REFERENCES price_version (ser_price_version_id),

    num_price             NUMERIC(38,2),

    -- Travels with the price, because a price change can change how it is
    -- charged: the same sweet cart may be £2.00 a head this year and a £600
    -- flat hire next. Keeping the rule on the offering alone would apply next
    -- year's rule to last year's figure.
    txt_price_rule        VARCHAR(16),

    created_date          TIMESTAMP,
    updated_date          TIMESTAMP,
    created_by            INTEGER DEFAULT 0,
    updated_by            INTEGER DEFAULT 0,
    bln_is_deleted        BOOLEAN DEFAULT FALSE,
    bln_is_active         BOOLEAN DEFAULT TRUE,
    bln_is_approved       BOOLEAN DEFAULT FALSE,

    -- One price per offering per version. Two would make "what does this cost
    -- in April" ambiguous, which is the one question the table exists to answer.
    CONSTRAINT uq_menu_offering_price UNIQUE (ser_offering_id, ser_price_version_id)
);

CREATE INDEX IF NOT EXISTS ix_menu_offering_price_version
    ON menu_offering_price (ser_price_version_id);
CREATE INDEX IF NOT EXISTS ix_menu_offering_price_offering
    ON menu_offering_price (ser_offering_id);

-- The list the current prices belong to.
--
-- Open-ended, and effective from the beginning rather than from today, because
-- bookings taken before this migration were quoted from these same figures.
--
-- NOT marked bln_is_default, and that is deliberate. The flag belongs to a
-- separate mechanism — ServicePriceVersionImpl.setAsDefault and the five places
-- that call findByBlnIsDefaultTrue... — which assumes there is exactly one
-- default and **nothing in the schema enforces it**: the finder returns a single
-- Optional and throws "Query did not return a unique result" the moment two
-- exist. Setting the flag here made this migration a silent participant in a
-- mechanism it has nothing to do with, and it broke an existing test the first
-- time both wanted a default at once.
--
-- Nothing is lost by leaving it clear. This version is PUBLISHED, open-ended
-- and the only one, so findVersionsEffectiveOn selects it on every date; the
-- default flag is a different question, and it belongs to whoever is answering
-- it. The missing unique constraint is recorded in PLATFORM.md §17 rather than
-- added here, because deciding what happens to a database that already has two
-- defaults is a data decision and not a migration's to take.
INSERT INTO price_version (
    txt_version_code, txt_name, txt_description,
    dte_effective_from, dte_effective_to,
    bln_is_default, num_priority, price_version_status,
    created_date, updated_date, created_by, updated_by,
    bln_is_deleted, bln_is_active, bln_is_approved
)
SELECT
    'PV-CURRENT',
    'Current prices',
    'The prices already in the catalogue when versioning was introduced.',
    TIMESTAMP '2000-01-01 00:00:00',
    NULL,
    FALSE,
    1,
    'PUBLISHED',
    NOW(), NOW(), 0, 0,
    FALSE, TRUE, TRUE
WHERE NOT EXISTS (SELECT 1 FROM price_version WHERE txt_version_code = 'PV-CURRENT');

-- Every offering's price, as it stands, recorded against that list.
--
-- Including the ones with no price. An offering that is deliberately unpriced
-- is a fact about the price list — 334 of them in production — and leaving them
-- out would make "this dish had no price in March" indistinguishable from "this
-- dish did not exist in March".
INSERT INTO menu_offering_price (
    ser_offering_id, ser_price_version_id, num_price, txt_price_rule,
    created_date, updated_date, created_by, updated_by,
    bln_is_deleted, bln_is_active, bln_is_approved
)
SELECT
    o.ser_offering_id,
    (SELECT ser_price_version_id FROM price_version WHERE txt_version_code = 'PV-CURRENT'),
    o.num_price,
    o.txt_price_rule,
    o.created_date, o.updated_date,
    COALESCE(o.created_by, 0), COALESCE(o.updated_by, 0),
    COALESCE(o.bln_is_deleted, FALSE), COALESCE(o.bln_is_active, TRUE), COALESCE(o.bln_is_approved, FALSE)
FROM menu_offering o
ON CONFLICT ON CONSTRAINT uq_menu_offering_price DO NOTHING;
