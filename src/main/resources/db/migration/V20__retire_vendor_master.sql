-- Vendors, removed.
--
-- WHAT IT WAS MEANT TO BE
--
-- A directory of outside firms — photographers, florists, DJs — that an event
-- could be linked to. It was modelled twice over: `event_master.ser_vendor_id`
-- said an event had exactly one vendor, and `event_vendor_master_selection`
-- said it had many. Both existed at once, which is the clearest possible sign
-- that nobody had decided what the relationship was.
--
-- WHAT IT ACTUALLY WAS
--
-- Unused. At the time of writing, across 195 events:
--
--   event_vendor_master_selection          0 rows
--   event_master with a ser_vendor_id      0 of 195
--   vendor_master                          5 rows, all from the development seed
--
-- The journey's supplier picker was commented out, so nothing ever wrote to it.
-- The row it would have written carried a name, a type, an address and a phone
-- number — which is not a supplier record, it is a contact.
--
-- WHY IT IS NOT BEING FIXED INSTEAD
--
-- Because the business does not work that way. Diamond Events does not engage
-- and re-charge outside firms — clause 5 of the terms says the opposite, that
-- third-party décor and catering are grounds for cancelling the booking. What
-- actually happens is that the *customer* brings their own people and tells the
-- venue who they are, which is `event_external_supplier` (V18) with a category
-- (V19). One concept, one table, one screen.
--
-- Keeping an empty parallel model beside it would leave the next person to read
-- this schema with two answers to "where are suppliers" and no way to tell
-- which is live.
--
-- UNDOING IT
--
-- There is nothing to restore: no row anywhere referenced any of this. Anybody
-- wanting a directory of firms the venue engages should build it deliberately,
-- against a decision that this is how the business runs, rather than inherit
-- these columns.

-- THE VIEW IN THE WAY
--
-- `v_event_summary` (V1) joins vendor_master and selects two of its columns, so
-- PostgreSQL refuses to drop either the column or the table underneath it.
--
-- It is dropped and rebuilt rather than dropped with CASCADE. CASCADE would
-- take the view away silently and leave the schema one object short of what V1
-- describes, and the next person to go looking for it would find a migration
-- that says nothing about having removed it.
--
-- The rebuild is V1's definition with `ven.txt_vendor_code`,
-- `ven.txt_vendor_name` and the join that produced them removed. Nothing else
-- about it changes. Nothing in the application reads the view — it is a
-- reporting convenience — so losing two always-null columns costs nothing;
-- they were always null because no event ever carried a vendor.

DROP VIEW IF EXISTS v_event_summary;

ALTER TABLE event_master DROP COLUMN IF EXISTS ser_vendor_id;

DROP TABLE IF EXISTS event_vendor_master_selection;

DROP TABLE IF EXISTS vendor_master;

CREATE VIEW v_event_summary AS
 SELECT em.ser_event_master_id,
    em.txt_event_master_code,
    em.txt_event_master_name,
    em.dte_event_date,
    et.txt_event_type_name,
    cm.txt_cust_code,
    cm.txt_cust_name,
    cm.txt_email,
    cm.txt_phone_number_1,
    cm.txt_address_1,
    em.num_number_of_guests,
    em.txt_number_of_guests,
    em.num_number_of_tables,
    em.txt_bride_name,
    em.txt_bride_first_name,
    em.txt_bride_last_name,
    em.txt_groom_name,
    em.txt_groom_first_name,
    em.txt_groom_last_name,
    vm.txt_venue_code,
    vm.txt_venue_name,
    c.txt_city_name,
    em.txt_event_remarks,
    em.txt_decore_remarks,
    em.txt_external_supplier_remarks,
    em.txt_catering_remarks,
    em.txt_event_extras_remarks,
    em.txt_venue_remarks,
    cm.txt_first_name AS cust_txt_first_name,
    cm.txt_last_name AS cust_txt_last_name
   FROM ((((public.event_master em
     LEFT JOIN public.event_type et ON ((em.ser_event_type_id = et.ser_event_type_id)))
     LEFT JOIN public.customer_master cm ON ((em.ser_cust_id = cm.ser_cust_id)))
     LEFT JOIN public.venue_master vm ON ((em.ser_venue_master_id = vm.ser_venue_master_id)))
     LEFT JOIN public.city_master c ON ((vm.ser_city_master_id = c.ser_city_id)));
