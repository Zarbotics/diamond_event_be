-- Carrying itinerary configuration across to equipment rules.
--
-- WHY THIS EXISTS
--
-- The itinerary model and the equipment model answer the same question — given
-- the dishes sold and the numbers coming, what does this event need and how
-- many — and only one of them was finished. The itinerary screens have been
-- retired from the control panel; see GLOSSARY.md §7.1 for the evidence.
--
-- Nothing ever read the itinerary calculation, so no event loses anything. But
-- the configuration itself is real work somebody did: on a live database there
-- are 261 assignments, one per dish. This carries that across so it does not
-- have to be typed again.
--
-- HOW TO RUN IT
--
--   psql -h localhost -U postgres -d diamond_ev -f itinerary-to-equipment.sql
--
-- It is NOT part of the Flyway migrations and will never run on its own. It is
-- idempotent — run it twice and the second run inserts nothing — and it
-- touches no itinerary table, so the originals stay exactly where they are.
--
-- READ THIS BEFORE RUNNING IT
--
-- One mapping is a judgement, and it is made here in the way that matches what
-- the system actually did rather than what its labels said:
--
--   PER_GUEST    -> PER_GUEST      the only one the calculation implemented
--   PER_TABLE    -> PER_TABLE      the label's meaning
--   FLAT         -> PER_EVENT      the label's meaning
--   PER_ITEM     -> PER_EVENT      see below
--   PER_PORTION  -> PER_EVENT      see below
--
-- The itinerary calculation read only PER_GUEST. Every other type fell through
-- to `return detail.getNumMultiplierValue().intValue()` — a flat quantity. So a
-- rule marked "per table" was never multiplied by the table count, and nobody
-- would have noticed, because nothing read the answer.
--
-- PER_TABLE and FLAT are carried at their stated meaning, because those are
-- unambiguous and the equipment model implements both properly. PER_ITEM and
-- PER_PORTION have no equivalent — equipment counts per guest, per table, per
-- station or per event — so they are carried as PER_EVENT, which is what the
-- old code did with them. Anything that should really be per guest or per
-- station needs a person to say so, and the note on each migrated row says
-- which ones those are.

BEGIN;

-- 1. The catalogue. An itinerary item becomes an equipment item of the same
--    name, unless one of that name is already there.
INSERT INTO equipment_item (txt_code, txt_name, txt_description, txt_unit,
                            num_display_order, bln_is_active, bln_is_deleted,
                            created_date)
SELECT
    ii.txt_code,
    ii.txt_name,
    -- itinerary_item has no description column of its own.
    'Carried over from the itinerary catalogue.',
    'each',
    NULL,
    COALESCE(ii.bln_is_active, TRUE),
    FALSE,
    NOW()
FROM itinerary_item ii
WHERE COALESCE(ii.bln_is_deleted, FALSE) = FALSE
  AND NOT EXISTS (
      SELECT 1 FROM equipment_item ei
      WHERE LOWER(TRIM(ei.txt_name)) = LOWER(TRIM(ii.txt_name))
        AND COALESCE(ei.bln_is_deleted, FALSE) = FALSE
  );

-- 2. The rules. One assignment detail becomes one equipment requirement,
--    against the dish the assignment was for.
INSERT INTO equipment_requirement (ser_menu_item_id, bln_applies_to_every_event,
                                   ser_equipment_item_id, num_quantity,
                                   enm_basis, num_guests_per_station,
                                   num_spare_percent, txt_notes,
                                   bln_is_active, bln_is_deleted, created_date)
SELECT
    ia.ser_menu_item_id,
    FALSE,
    ei.ser_equipment_item_id,
    COALESCE(iad.num_multiplier_value, 1),
    CASE iad.enm_multiplier_type
        WHEN 'PER_GUEST' THEN 'PER_GUEST'
        WHEN 'PER_TABLE' THEN 'PER_TABLE'
        ELSE 'PER_EVENT'
    END,
    NULL,
    NULL,
    -- Says where it came from, and flags the two that were a judgement.
    TRIM(COALESCE(iad.txt_notes, '') || ' ' ||
        CASE
            WHEN iad.enm_multiplier_type IN ('PER_ITEM', 'PER_PORTION')
                THEN '[Carried over from an itinerary assignment marked '
                     || iad.enm_multiplier_type
                     || ', which has no equipment equivalent. Recorded as per '
                     || 'event, which is what the old calculation did with it. '
                     || 'Check whether it should be per guest or per station.]'
            ELSE '[Carried over from an itinerary assignment.]'
        END),
    TRUE,
    FALSE,
    NOW()
FROM itinerary_assignment ia
JOIN itinerary_assignment_detail iad
     ON iad.ser_itinerary_assignment_id = ia.ser_itinerary_assignment_id
    AND COALESCE(iad.bln_is_deleted, FALSE) = FALSE
JOIN itinerary_item ii
     ON ii.ser_itinerary_item_id = iad.ser_itinerary_item_id
JOIN equipment_item ei
     ON LOWER(TRIM(ei.txt_name)) = LOWER(TRIM(ii.txt_name))
    AND COALESCE(ei.bln_is_deleted, FALSE) = FALSE
WHERE COALESCE(ia.bln_is_deleted, FALSE) = FALSE
  AND NOT EXISTS (
      SELECT 1 FROM equipment_requirement er
      WHERE er.ser_menu_item_id = ia.ser_menu_item_id
        AND er.ser_equipment_item_id = ei.ser_equipment_item_id
        AND COALESCE(er.bln_is_deleted, FALSE) = FALSE
  );

COMMIT;

-- WHAT CAME ACROSS, AND WHAT DID NOT
--
-- Run this afterwards. The second number is the one to look at: those are
-- rules a person has to decide about.

SELECT 'equipment items now'            AS what, COUNT(*) AS how_many
FROM equipment_item WHERE COALESCE(bln_is_deleted, FALSE) = FALSE
UNION ALL
SELECT 'rules carried over',            COUNT(*)
FROM equipment_requirement
WHERE txt_notes LIKE '%Carried over from an itinerary assignment%'
  AND COALESCE(bln_is_deleted, FALSE) = FALSE
UNION ALL
SELECT 'of those, needing a decision',  COUNT(*)
FROM equipment_requirement
WHERE txt_notes LIKE '%has no equipment equivalent%'
  AND COALESCE(bln_is_deleted, FALSE) = FALSE
UNION ALL
SELECT 'assignments in the old model',  COUNT(*)
FROM itinerary_assignment
WHERE COALESCE(bln_is_deleted, FALSE) = FALSE;
