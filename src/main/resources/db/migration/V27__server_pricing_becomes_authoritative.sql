-- The system's own figures become the ones the customer is charged.
--
-- WHAT THIS TURNS ON
--
-- pricing.server.authoritative was seeded 'false' in V24, deliberately. The
-- engine worked every price out and recorded it beside the figure the booking
-- screens sent, and only the screens' figure was used — so the two could be
-- compared on real bookings before anything depended on the answer.
--
-- WHY IT IS SAFE TO TURN ON NOW
--
-- Three reasons, in order of how much they mattered.
--
-- 1. The comparison found a defect that would have made this switch harmful.
--    num_discount exists on BOTH event_master and event_budget. The save writes
--    the budget's copy; the engine read the event's. Across the 312 bookings in
--    the database the event's column is null on every one and the budget's is
--    set on every one — nothing has ever written the event's copy. Switching
--    this on before fixing it would have silently ignored every discount the
--    office had entered and billed the customer the full amount. Both columns
--    exist and both names read correctly, so nothing about the code looked
--    wrong. It is fixed, and the engine is now told the discount rather than
--    going looking for it.
--
-- 2. The engine now refuses to impose a figure it could not work out. A
--    per-guest price multiplied by a guest count nobody entered is zero;
--    arithmetically correct, and as an answer a lie — the booking is not free,
--    it is unpriced. 94 of the 312 bookings have no guest count. Where the
--    engine cannot price a booking it records what it made of it, leaves the
--    live quote alone, and says why in the log.
--
-- 3. There was nothing to lose. Every one of the 312 bookings is quoted at
--    zero: no quoted price, no final amount, nothing paid. The browser's
--    arithmetic has never produced a figure that reached a customer through
--    this path, so there is no existing quote for the engine to contradict and
--    no override to preserve.
--
-- WHAT THIS DOES NOT CHANGE
--
-- The office can still override any figure the system produces — that is the
-- whole point of the arrangement, and it is what the price boxes on the booking
-- are for. What changes is that an empty box now means "charge what the rules
-- say" and is answered by the rules, rather than by whatever the browser last
-- calculated.
--
-- It also remains a setting. Somebody can put it back from the Settings screen
-- without a deployment, which is why it is a row rather than a constant.

UPDATE app_setting
SET txt_value    = 'true',
    txt_description = 'The system works out every price from what has been chosen — the menu, the décor, the extras, the guest count — and those are the figures on the booking. The office can still override any of them on the booking itself; an empty price box means "charge the usual". Turning this off hands the arithmetic back to the booking screens, which is how it used to work.',
    updated_date = NOW()
WHERE txt_key = 'pricing.server.authoritative';

-- No INSERT fallback. Migrations run in order, so V24's row is always there by
-- the time this runs; an "in case it is missing" insert here would only be a
-- second place for the setting's wording to drift. (The first attempt at one
-- also named a column that does not exist — app_setting calls it
-- txt_value_type, not txt_type.)
