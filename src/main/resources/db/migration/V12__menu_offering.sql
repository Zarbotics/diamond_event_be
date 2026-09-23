-- Stage M2 of the food menu work. See PLATFORM.md §17.
--
-- WHAT IS WRONG TODAY
--
-- A menu item has exactly one parent, so where a dish appears is a property of
-- the dish. Offering a chocolate brownie in the Dessert Buffet, on the Dessert
-- Stand, as a Trio and Served To The Table therefore takes four chocolate
-- brownies.
--
-- That is what the live catalogue has become: 368 selectable rows holding 238
-- distinct dishes. Twenty-one desserts exist five times each. Renaming one is
-- five edits, repricing it is five edits, and the copies have already drifted —
-- Trifle is PER_GUEST in three of its five rows and unset in a fourth. No
-- report can total "brownies for Saturday", because five ids are five dishes.
--
-- WHAT THIS MIGRATION DOES
--
-- It makes "this dish is offered here, on these terms" a row of its own, rather
-- than something inferred from a parent pointer. A dish then no longer has to
-- live in one branch of the tree to appear in it.
--
-- WHAT IT DELIBERATELY DOES NOT DO
--
-- It does not merge the duplicates. The backfill is an identity mapping: every
-- selectable item gets one offering, of itself, under its current parent, with
-- its current price and rule copied across. Nothing changes, nothing reads it,
-- and the whole stage is undone by dropping a table.
--
-- Merging is a separate step, and a supervised one. Deciding by name that two
-- rows are the same dish is a guess, and a wrong guess here silently changes
-- what a customer is charged — "Kheer" under Desi Desserts and "Kheer" in a set
-- menu may well be priced differently on purpose. So the merge is offered to a
-- person in the admin screen (M4), one dish at a time, and this migration only
-- builds the thing that makes a merge expressible at all.

CREATE TABLE IF NOT EXISTS menu_offering (
    ser_offering_id     BIGSERIAL PRIMARY KEY,

    -- The dish. What it is, what it is called, what it is made of.
    ser_menu_item_id    BIGINT NOT NULL REFERENCES menu_item (ser_menu_item_id),

    -- Where it is offered: the subcategory, or the composite's section. Also a
    -- menu_item, because that is what sections are today.
    ser_section_id      BIGINT NOT NULL REFERENCES menu_item (ser_menu_item_id),

    -- The terms it is offered on here. A dish can legitimately cost a different
    -- amount in a buffet than plated, which is exactly why these live on the
    -- offering and not on the dish.
    num_price           NUMERIC(38,2),

    -- PER_GUEST or FLAT. Nullable for now because 238 of the live items have
    -- never said, and M3 is where saying becomes compulsory — a NOT NULL here
    -- would reject the backfill of the very rows that need fixing.
    txt_price_rule      VARCHAR(16),

    num_position        INTEGER,

    created_date        TIMESTAMP,
    updated_date        TIMESTAMP,
    created_by          INTEGER DEFAULT 0,
    updated_by          INTEGER DEFAULT 0,
    bln_is_deleted      BOOLEAN DEFAULT FALSE,
    bln_is_active       BOOLEAN DEFAULT TRUE,
    bln_is_approved     BOOLEAN DEFAULT FALSE,

    -- One offering of a dish per section. Two would mean the same dish listed
    -- twice in one place, which is a data-entry mistake rather than a menu.
    CONSTRAINT uq_menu_offering UNIQUE (ser_menu_item_id, ser_section_id)
);

-- Both directions are read: "what is offered in this section" builds a menu,
-- "where is this dish offered" is what makes a rename one edit.
CREATE INDEX IF NOT EXISTS ix_menu_offering_section ON menu_offering (ser_section_id);
CREATE INDEX IF NOT EXISTS ix_menu_offering_item ON menu_offering (ser_menu_item_id);

-- The backfill: one offering per selectable item, of itself, under its parent.
--
-- Restricted to items that have a parent. A selectable item at the root of the
-- tree has nowhere to be offered, and there are none in production — but the
-- join would silently drop it rather than fail, so the WHERE says so out loud.
--
-- Guarded by the unique constraint so that re-running this is a no-op rather
-- than a second set of offerings.
INSERT INTO menu_offering (
    ser_menu_item_id, ser_section_id, num_price, txt_price_rule, num_position,
    created_date, updated_date, created_by, updated_by,
    bln_is_deleted, bln_is_active, bln_is_approved
)
SELECT
    i.ser_menu_item_id,
    i.parent_menu_item_id,
    i.num_price,
    i.enm_price_multiplier_type,
    i.num_display_order,
    i.created_date,
    i.updated_date,
    COALESCE(i.created_by, 0),
    COALESCE(i.updated_by, 0),
    COALESCE(i.bln_is_deleted, FALSE),
    COALESCE(i.bln_is_active, TRUE),
    COALESCE(i.bln_is_approved, FALSE)
FROM menu_item i
WHERE i.bln_is_selectable IS TRUE
  AND i.parent_menu_item_id IS NOT NULL
ON CONFLICT ON CONSTRAINT uq_menu_offering DO NOTHING;
