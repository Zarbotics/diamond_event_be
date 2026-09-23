-- When the customer agreed to the terms.
--
-- WHAT THIS IS FOR
--
-- The last step of the journey puts the full terms and payment policy in front
-- of the customer and will not let them continue until they tick to say they
-- have read it. That tick lived in a React `useState` and nowhere else: it was
-- gone the moment the component unmounted, so a customer who came back to the
-- step was asked to agree again, and — more to the point — the business had no
-- record that anybody had ever agreed to anything.
--
-- The policy it gates is a contract. It says the £1000 deposit is
-- non-refundable, that the balance falls due four weeks before the event, and
-- that the venue may cancel and keep the deposit if a third-party decorator is
-- brought in. Every one of those is a clause somebody may one day dispute, and
-- "the box was ticked" is not evidence unless it was written down.
--
-- WHY A TIMESTAMP RATHER THAN A FLAG
--
-- Because the question asked in a dispute is never "did they agree" on its
-- own; it is "when". A NULL means not yet agreed and carries exactly as much
-- information as `false` would, so nothing is lost by the column being the
-- more useful of the two.
--
-- The application never clears it. Agreement is a thing that happened, and a
-- later save of the same booking must not be able to un-happen it — the same
-- reasoning as `ser_booking_id` in V11 and V16, and the same failure if it
-- were writable from a DTO: every re-save would blank it silently.
--
-- Undone by dropping one column.

ALTER TABLE event_master
    ADD COLUMN IF NOT EXISTS dte_terms_accepted_on TIMESTAMP(6);

COMMENT ON COLUMN event_master.dte_terms_accepted_on IS
    'When the customer accepted the terms and payment policy. NULL means they have not. Never cleared once set.';
