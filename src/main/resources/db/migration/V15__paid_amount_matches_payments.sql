-- Stage 3 of putting a booking above the event. See PLATFORM.md §15.3.
--
-- WHAT THIS REPAIRS
--
-- Two things owned event_budget.num_paid_amount and the wrong one won.
-- ServiceEventBudgetImpl.recalculateBudget sums the payments recorded against
-- the budget, which is right. The four event-save methods set it to whatever
-- the form sent — and the form sends the figure it loaded, read before the
-- payment was taken. The save runs last.
--
-- Production carries one instance of the result. Budget 146 reads 0.00 against
-- a recorded payment of £500, and its updated_date is four seconds after the
-- payment's: the save that followed, carrying the zero it had loaded. Nothing
-- announced it — the payment row is still there and the booking simply reads as
-- unpaid, on the screen, on the report, and in every total the office works
-- from.
--
-- The code fix is in this same change: the save paths now go through
-- setPaidAmount, which will not write over payments. This statement repairs
-- what the old behaviour already did.
--
-- WHY THIS IS SAFE TO DO MECHANICALLY
--
-- Only budgets that actually have payments are touched, and they are set to the
-- sum of those payments. The payment rows are the record of money received;
-- num_paid_amount is a cache of their total, and where the two disagree the
-- rows are the evidence.
--
-- Budgets with no payments are deliberately left alone. The catering form has a
-- Paid Amount box that is the only record of money taken for a delivery, and
-- setting those to the sum of a non-existent set of payments would erase real
-- figures — the opposite fault, made by a migration rather than a form.
--
-- Deleted payments do not count, matching sumPaidByBudgetId.

UPDATE event_budget b
SET num_paid_amount = t.total,
    updated_date    = NOW()
FROM (
    SELECT p.ser_event_budget_id, SUM(p.num_amount) AS total
    FROM event_payment p
    WHERE p.bln_is_deleted = false
    GROUP BY p.ser_event_budget_id
    HAVING SUM(p.num_amount) > 0
) AS t
WHERE b.ser_event_budget_id = t.ser_event_budget_id
  AND COALESCE(b.num_paid_amount, 0) IS DISTINCT FROM t.total;
