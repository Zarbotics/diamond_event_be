-- A version counter on the event, so two people editing one booking is not
-- resolved by whoever presses Save second.
--
-- The failure this addresses is silent on both sides. An administrator opens a
-- booking to change the guest count while the customer is choosing their menu;
-- both save; one set of changes is simply gone, with nothing in the interface
-- or the log to say it happened. It is found weeks later, when the kitchen
-- cooks for the wrong number.
--
-- Hibernate maintains this column through @Version. It is also compared
-- explicitly against the copy the client is holding, which is the case that
-- actually bites: the two edits are minutes apart rather than milliseconds, so
-- there is no overlapping transaction for the database to notice.

ALTER TABLE event_master
    ADD COLUMN IF NOT EXISTS num_version BIGINT;

-- Existing rows start at zero rather than NULL. A NULL version would make
-- Hibernate treat every one of them as a new row on first save, which is a far
-- worse failure than the one being fixed.
UPDATE event_master
SET num_version = 0
WHERE num_version IS NULL;

ALTER TABLE event_master
    ALTER COLUMN num_version SET DEFAULT 0;

ALTER TABLE event_master
    ALTER COLUMN num_version SET NOT NULL;
