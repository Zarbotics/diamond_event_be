-- Stage M3 of the food menu work. See PLATFORM.md §17.
--
-- WHAT IS WRONG TODAY
--
-- A menu item's place in the tree is recorded twice: in parent_menu_item_id,
-- which every query in the application walks, and in txt_path, an ltree that
-- nothing maintains. In the live catalogue **fourteen rows disagree**.
--
-- Eight raitas and chutneys carry paths beginning MI_1007.SUB_023 while their
-- parent pointer says MI_1006.SUB_023. Six items under Reception Displays claim
-- MI_1015.SUB_001.MI_1019.* — four levels deep — while their parent is the
-- depth-two MI_1002.MI_1019.
--
-- Nothing is broken by this today, precisely because nothing reads the path.
-- That is the danger rather than the reassurance: the first query written
-- against txt_path — "everything under Desserts", which is the natural way to
-- ask — silently returns the wrong answer for those fourteen, and returns it
-- confidently.
--
-- WHY REPAIR RATHER THAN DROP
--
-- The column could go instead, and one representation is better than two. But
-- an ltree earns its keep: "every dish anywhere under Desserts" is one operator
-- against an index, and the alternative is a recursive query every time. So it
-- is repaired and then guarded — MenuPathMatchesParentIT fails the build if the
-- two ever disagree again, which is what makes keeping it honest rather than
-- decorative.
--
-- HOW
--
-- The parent pointer is authoritative: it is what the application has always
-- walked, so it is what the live menu actually looks like. Each node keeps its
-- own last label and takes its prefix from its parent, applied top down so that
-- a corrected parent is in place before its children are recalculated.

WITH RECURSIVE corrected AS (
    -- Roots keep the label they have. A root's path is its own label and
    -- nothing else, so there is nothing above it to disagree with.
    SELECT
        i.ser_menu_item_id,
        subpath(i.txt_path, nlevel(i.txt_path) - 1) AS path
    FROM menu_item i
    WHERE i.parent_menu_item_id IS NULL

    UNION ALL

    -- Every child hangs its own label off its parent's corrected path.
    SELECT
        c.ser_menu_item_id,
        p.path || subpath(c.txt_path, nlevel(c.txt_path) - 1)
    FROM menu_item c
    JOIN corrected p ON p.ser_menu_item_id = c.parent_menu_item_id
)
UPDATE menu_item i
SET txt_path = corrected.path
FROM corrected
WHERE corrected.ser_menu_item_id = i.ser_menu_item_id
  AND i.txt_path IS DISTINCT FROM corrected.path;
