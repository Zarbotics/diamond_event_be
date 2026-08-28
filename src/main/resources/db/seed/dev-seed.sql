-- ═══════════════════════════════════════════════════════════════════════
-- Development seed data.
--
-- NOT a Flyway migration, and deliberately not under db/migration: it must
-- never run against production. Apply it by hand:
--
--   psql -h localhost -U postgres -d diamond_ev -f src/main/resources/db/seed/dev-seed.sql
--
-- Why this exists: a clean database gives every catalogue endpoint an empty
-- array, so the booking journey renders a sequence of blank screens and there
-- is no way to tell a broken query from a genuinely empty table. That made the
-- journey untestable on a fresh checkout, which is how several of the
-- catalogue bugs survived as long as they did.
--
-- Idempotent — safe to run repeatedly. Every insert is keyed on its natural
-- business code and skipped if already present.
--
-- The content mirrors the shape of the live catalogue (a South-Asian wedding
-- and events caterer operating in and around London) without copying any real
-- customer, supplier or pricing data.
-- ═══════════════════════════════════════════════════════════════════════

BEGIN;

-- ── Geography ─────────────────────────────────────────────────────────
-- country_master carries the active flag twice: bln_is_active from BaseEntity
-- and a legacy is_active column. Both are set, because the venue query reads
-- one and the journey displays the other.
INSERT INTO country_master (txt_country_code, txt_country_name, short_name, bln_is_active, is_active, bln_is_deleted, default_country, created_date)
SELECT 'GB', 'United Kingdom', 'UK', true, true, false, 1, now()
WHERE NOT EXISTS (SELECT 1 FROM country_master WHERE txt_country_code = 'GB');

INSERT INTO state_master (txt_state_code, txt_state_name, ser_country_id, bln_is_active, bln_is_deleted, created_date)
SELECT 'ENG', 'England', (SELECT ser_country_id FROM country_master WHERE txt_country_code = 'GB'), true, false, now()
WHERE NOT EXISTS (SELECT 1 FROM state_master WHERE txt_state_code = 'ENG');

INSERT INTO city_master (txt_city_code, txt_city_name, ser_state_id, bln_is_active, bln_is_deleted, created_date)
SELECT v.code, v.name, (SELECT ser_state_id FROM state_master WHERE txt_state_code = 'ENG'), true, false, now()
FROM (VALUES
    ('LON', 'London'),
    ('BHM', 'Birmingham'),
    ('MAN', 'Manchester')
) AS v(code, name)
WHERE NOT EXISTS (SELECT 1 FROM city_master WHERE txt_city_code = v.code);

-- ── Venues ────────────────────────────────────────────────────────────
INSERT INTO venue_master (txt_venue_code, txt_venue_name, txt_address, txt_email_address, txt_phone_number, txt_web_link, ser_city_master_id, bln_is_active, bln_is_deleted, created_date)
SELECT v.code, v.name, v.address, v.email, v.phone, v.web,
       (SELECT ser_city_id FROM city_master WHERE txt_city_code = v.city), true, false, now()
FROM (VALUES
    ('VEN-001', 'The Grand Ballroom, Mayfair',   '18 Park Lane, London W1K 1BE',        'events@grandballroom.example', '020 7000 0101', 'https://example.com/grand',   'LON'),
    ('VEN-002', 'Riverside Pavilion, Richmond',  'Thames Bank, Richmond TW9 1TH',       'hello@riverside.example',      '020 7000 0102', 'https://example.com/riverside','LON'),
    ('VEN-003', 'The Orangery, Hertfordshire',   'Manor Park, St Albans AL2 3NX',       'bookings@orangery.example',    '01727 000103',  'https://example.com/orangery', 'LON'),
    ('VEN-004', 'Alexandra Hall, Birmingham',    '4 Broad Street, Birmingham B1 2EA',   'events@alexandra.example',     '0121 000 0104', 'https://example.com/alexandra','BHM'),
    ('VEN-005', 'The Northern Rooms, Manchester','12 Deansgate, Manchester M3 2BW',     'events@northern.example',      '0161 000 0105', 'https://example.com/northern', 'MAN')
) AS v(code, name, address, email, phone, web, city)
WHERE NOT EXISTS (SELECT 1 FROM venue_master WHERE txt_venue_code = v.code);

-- ── Halls ─────────────────────────────────────────────────────────────
-- A venue on its own is not selectable: the Event Venue step lists halls
-- within each venue and the customer picks one of those. Seeding venues
-- without halls left every venue showing "No halls available", so the step
-- could not be completed and the review screen could never show a venue.
--
-- Capacities matter too — the step refuses a hall whose capacity is below the
-- guest count, so there has to be a range to choose from.
INSERT INTO venue_master_detail (txt_hall_code, txt_hall_name, num_capacity, txt_capacity, num_price,
                                 ser_venue_master_id, bln_is_active, bln_is_deleted, created_date)
SELECT v.code, v.name, v.capacity, v.capacity || ' seated', v.price,
       (SELECT ser_venue_master_id FROM venue_master WHERE txt_venue_code = v.venue),
       true, false, now()
FROM (VALUES
    ('HALL-001-A', 'The Ballroom',        400, 6500.00, 'VEN-001'),
    ('HALL-001-B', 'The Library Room',    120, 2800.00, 'VEN-001'),
    ('HALL-002-A', 'Riverside Marquee',   300, 5200.00, 'VEN-002'),
    ('HALL-002-B', 'The Terrace',          90, 2400.00, 'VEN-002'),
    ('HALL-003-A', 'The Orangery',        250, 4800.00, 'VEN-003'),
    ('HALL-004-A', 'Alexandra Main Hall', 500, 5900.00, 'VEN-004'),
    ('HALL-004-B', 'The Gallery',         150, 3100.00, 'VEN-004'),
    ('HALL-005-A', 'Deansgate Suite',     350, 5400.00, 'VEN-005')
) AS v(code, name, capacity, price, venue)
WHERE NOT EXISTS (SELECT 1 FROM venue_master_detail WHERE txt_hall_code = v.code);

-- ── Event types ───────────────────────────────────────────────────────
-- Main events first; sub-events reference them by parent_event_type.
INSERT INTO event_type (txt_event_type_code, txt_event_type_name, bln_is_main_event, bln_is_active, bln_is_deleted, created_date)
SELECT v.code, v.name, true, true, false, now()
FROM (VALUES
    ('ET-WED', 'Wedding'),
    ('ET-COR', 'Corporate Event'),
    ('ET-PRV', 'Private Celebration')
) AS v(code, name)
WHERE NOT EXISTS (SELECT 1 FROM event_type WHERE txt_event_type_code = v.code);

INSERT INTO event_type (txt_event_type_code, txt_event_type_name, bln_is_main_event, parent_event_type, bln_is_active, bln_is_deleted, created_date)
SELECT v.code, v.name, false,
       (SELECT ser_event_type_id FROM event_type WHERE txt_event_type_code = v.parent),
       true, false, now()
FROM (VALUES
    ('ET-WED-MEH', 'Mehndi',            'ET-WED'),
    ('ET-WED-NIK', 'Nikkah',            'ET-WED'),
    ('ET-WED-WAL', 'Walima',            'ET-WED'),
    ('ET-WED-REC', 'Reception',         'ET-WED'),
    ('ET-COR-CON', 'Conference',        'ET-COR'),
    ('ET-COR-AWD', 'Awards Dinner',     'ET-COR'),
    ('ET-PRV-BIR', 'Birthday',          'ET-PRV'),
    ('ET-PRV-ANN', 'Anniversary',       'ET-PRV')
) AS v(code, name, parent)
WHERE NOT EXISTS (SELECT 1 FROM event_type WHERE txt_event_type_code = v.code);

-- ── Food ──────────────────────────────────────────────────────────────
INSERT INTO menu_food_master (
    txt_menu_food_code, txt_menu_food_name,
    bln_is_starter, bln_is_appetiser, bln_is_main_course,
    bln_is_salad_and_condiment, bln_is_dessert, bln_is_drink,
    bln_is_active, bln_is_deleted, created_date)
SELECT v.code, v.name, v.starter, v.appetiser, v.main, v.salad, v.dessert, v.drink, true, false, now()
FROM (VALUES
    ('FD-ST-01', 'Chicken Tikka',                true,  false, false, false, false, false),
    ('FD-ST-02', 'Seekh Kebab',                  true,  false, false, false, false, false),
    ('FD-ST-03', 'Vegetable Samosa',             true,  false, false, false, false, false),
    ('FD-ST-04', 'Paneer Tikka',                 true,  false, false, false, false, false),
    ('FD-AP-01', 'Chaat Papri',                  false, true,  false, false, false, false),
    ('FD-AP-02', 'Spiced Cauliflower Bites',     false, true,  false, false, false, false),
    ('FD-MC-01', 'Chicken Karahi',               false, false, true,  false, false, false),
    ('FD-MC-02', 'Lamb Biryani',                 false, false, true,  false, false, false),
    ('FD-MC-03', 'Butter Chicken',               false, false, true,  false, false, false),
    ('FD-MC-04', 'Daal Makhani',                 false, false, true,  false, false, false),
    ('FD-MC-05', 'Grilled Sea Bass, Herb Butter',false, false, true,  false, false, false),
    ('FD-SC-01', 'Kachumber Salad',              false, false, false, true,  false, false),
    ('FD-SC-02', 'Raita',                        false, false, false, true,  false, false),
    ('FD-SC-03', 'Mint & Tamarind Chutneys',     false, false, false, true,  false, false),
    ('FD-DS-01', 'Gulab Jamun',                  false, false, false, false, true,  false),
    ('FD-DS-02', 'Kheer',                        false, false, false, false, true,  false),
    ('FD-DS-03', 'Chocolate Torte',              false, false, false, false, true,  false),
    ('FD-DR-01', 'Mango Lassi',                  false, false, false, false, false, true),
    ('FD-DR-02', 'Rose Sharbat',                 false, false, false, false, false, true),
    ('FD-DR-03', 'Masala Chai',                  false, false, false, false, false, true)
) AS v(code, name, starter, appetiser, main, salad, dessert, drink)
WHERE NOT EXISTS (SELECT 1 FROM menu_food_master WHERE txt_menu_food_code = v.code);

-- ── Decor ─────────────────────────────────────────────────────────────
INSERT INTO decor_category_master (txt_decor_category_code, txt_decor_category_name, txt_description, num_price, num_display_order, bln_is_active, bln_is_deleted, created_date)
SELECT v.code, v.name, v.descr, v.price, v.ord, true, false, now()
FROM (VALUES
    ('DC-STG', 'Stage & Backdrop',   'The centrepiece of the room — where the couple or guests of honour are seated.', 2500.00, 1),
    ('DC-TBL', 'Table Styling',      'Centrepieces, linen, charger plates and place settings.',                        1200.00, 2),
    ('DC-ENT', 'Entrance & Walkway', 'How your guests are welcomed, from the door to the room.',                        850.00, 3),
    ('DC-LGT', 'Lighting',           'Uplighting, pin-spots and dance-floor lighting.',                                 950.00, 4)
) AS v(code, name, descr, price, ord)
WHERE NOT EXISTS (SELECT 1 FROM decor_category_master WHERE txt_decor_category_code = v.code);

INSERT INTO decor_category_property_master (txt_property_code, txt_property_name, txt_description, txt_input_type, ser_decor_category_id, num_price, num_display_order, bln_is_required, bln_is_active, bln_is_deleted, created_date)
SELECT v.code, v.name, v.descr, v.input,
       (SELECT ser_decor_category_id FROM decor_category_master WHERE txt_decor_category_code = v.cat),
       v.price, v.ord, v.required, true, false, now()
FROM (VALUES
    ('DP-STG-STYLE', 'Backdrop style',   'The overall look of the stage.',            'SELECT', 'DC-STG',   0.00, 1, true),
    ('DP-STG-SEAT',  'Seating',          'What the couple or guests of honour sit on.','SELECT', 'DC-STG', 350.00, 2, false),
    ('DP-TBL-CENTR', 'Centrepiece',      'What sits at the centre of each table.',    'SELECT', 'DC-TBL',   0.00, 1, true),
    ('DP-TBL-LINEN', 'Linen colour',     'Tablecloth and napkin colour.',             'SELECT', 'DC-TBL',   0.00, 2, false),
    ('DP-ENT-ARCH',  'Entrance arch',    'The frame your guests walk through.',       'SELECT', 'DC-ENT', 400.00, 1, false),
    ('DP-LGT-COLR',  'Uplighting colour','The wash of colour around the room.',       'SELECT', 'DC-LGT',   0.00, 1, false)
) AS v(code, name, descr, input, cat, price, ord, required)
WHERE NOT EXISTS (SELECT 1 FROM decor_category_property_master WHERE txt_property_code = v.code);

INSERT INTO decor_category_property_value (txt_property_value, txt_description, ser_property_id, bln_is_document, bln_is_active, bln_is_deleted, created_date)
SELECT v.value, v.descr,
       (SELECT ser_property_id FROM decor_category_property_master WHERE txt_property_code = v.prop),
       false, true, false, now()
FROM (VALUES
    ('Floral arch',        'Fresh flowers over a curved frame.',       'DP-STG-STYLE'),
    ('Draped panels',      'Layered fabric with concealed lighting.',  'DP-STG-STYLE'),
    ('Mirrored panels',    'Reflective panels with floral detailing.', 'DP-STG-STYLE'),
    ('Two-seat sofa',      'Upholstered, ivory.',                      'DP-STG-SEAT'),
    ('Throne chairs',      'A pair, carved and gilded.',               'DP-STG-SEAT'),
    ('Tall floral',        'Raised arrangement on a stand.',           'DP-TBL-CENTR'),
    ('Low floral',         'Compact arrangement, easier to talk over.','DP-TBL-CENTR'),
    ('Candelabra',         'Five-arm, with taper candles.',            'DP-TBL-CENTR'),
    ('Ivory',              NULL,                                       'DP-TBL-LINEN'),
    ('Blush',              NULL,                                       'DP-TBL-LINEN'),
    ('Deep navy',          NULL,                                       'DP-TBL-LINEN'),
    ('Floral arch',        'Matching the stage.',                      'DP-ENT-ARCH'),
    ('Balloon arch',       'Toned to your palette.',                   'DP-ENT-ARCH'),
    ('Warm white',         NULL,                                       'DP-LGT-COLR'),
    ('Champagne',          NULL,                                       'DP-LGT-COLR'),
    ('Deep navy',          NULL,                                       'DP-LGT-COLR')
) AS v(value, descr, prop)
WHERE NOT EXISTS (
    SELECT 1 FROM decor_category_property_value pv
    JOIN decor_category_property_master pm ON pm.ser_property_id = pv.ser_property_id
    WHERE pm.txt_property_code = v.prop AND pv.txt_property_value = v.value);

-- ── Extras and services ───────────────────────────────────────────────
-- bln_is_service splits this table into two lists in the journey: "extras"
-- are things, "services" are people.
INSERT INTO decor_extras_master (txt_extras_code, txt_extras_name, txt_description, num_price, num_display_order, bln_is_service, bln_is_active, bln_is_deleted, created_date)
SELECT v.code, v.name, v.descr, v.price, v.ord, v.is_service, true, false, now()
FROM (VALUES
    ('EX-DNC', 'Dance floor',        'Fitted to the room, in white or mirrored finish.', 650.00, 1, false),
    ('EX-CHR', 'Chair covers',       'With a sash in your chosen colour.',               450.00, 2, false),
    ('EX-CAK', 'Cake table',         'Styled to match your stage.',                      200.00, 3, false),
    ('EX-WLK', 'Walkway runner',     'Full-length, with edge florals.',                  380.00, 4, false),
    ('EX-FOG', 'Low-lying fog',      'For the first dance.',                             250.00, 5, false),
    ('SV-PHO', 'Photography',        'Full-day coverage, two photographers.',           1800.00, 1, true),
    ('SV-VID', 'Videography',        'Full-day coverage and a highlights film.',        2200.00, 2, true),
    ('SV-DJ',  'DJ',                 'Five hours, with sound and lighting.',             900.00, 3, true),
    ('SV-MC',  'Master of ceremonies','Runs the order of the day.',                      600.00, 4, true),
    ('SV-VLT', 'Valet parking',      'Two attendants, four hours.',                      550.00, 5, true)
) AS v(code, name, descr, price, ord, is_service)
WHERE NOT EXISTS (SELECT 1 FROM decor_extras_master WHERE txt_extras_code = v.code);

INSERT INTO decor_extras_option (txt_option_code, txt_option_name, txt_description, ser_extras_id, bln_is_document, bln_is_active, bln_is_deleted, created_date)
SELECT v.code, v.name, v.descr,
       (SELECT ser_extras_id FROM decor_extras_master WHERE txt_extras_code = v.extra),
       false, true, false, now()
FROM (VALUES
    ('OP-DNC-WHT', 'White gloss',    NULL,                    'EX-DNC'),
    ('OP-DNC-MIR', 'Mirrored',       NULL,                    'EX-DNC'),
    ('OP-CHR-IVY', 'Ivory',          NULL,                    'EX-CHR'),
    ('OP-CHR-CHM', 'Champagne',      NULL,                    'EX-CHR'),
    ('OP-PHO-STD', 'Standard album', '40 pages.',             'SV-PHO'),
    ('OP-PHO-PRM', 'Premium album',  '80 pages, boxed.',      'SV-PHO'),
    -- The remaining services had no options at all, which is how the
    -- inner-join bug in RepositoryDecorExtrasMaster went unnoticed: four of
    -- the five services simply never reached the customer.
    ('OP-VID-HL',  'Highlights film',  'Five to eight minutes.',        'SV-VID'),
    ('OP-VID-FULL','Full ceremony film','Unedited, plus highlights.',   'SV-VID'),
    ('OP-DJ-STD',  'DJ only',          'Five hours.',                   'SV-DJ'),
    ('OP-DJ-LIVE', 'DJ with live percussion', 'Five hours, dhol for the entrances.', 'SV-DJ'),
    ('OP-MC-EN',   'English',          NULL,                            'SV-MC'),
    ('OP-MC-BI',   'Bilingual',        'English plus Urdu or Punjabi.', 'SV-MC'),
    ('OP-VLT-4',   'Four hours',       'Two attendants.',               'SV-VLT'),
    ('OP-VLT-8',   'Eight hours',      'Four attendants.',              'SV-VLT'),
    ('OP-CAK-RND', 'Round table',      'Seats a three-tier cake.',      'EX-CAK'),
    ('OP-CAK-SQR', 'Square table',     'Seats a larger display.',       'EX-CAK'),
    ('OP-WLK-IVY', 'Ivory runner',     NULL,                            'EX-WLK'),
    ('OP-WLK-BLK', 'Black mirror runner', NULL,                         'EX-WLK')
) AS v(code, name, descr, extra)
WHERE NOT EXISTS (SELECT 1 FROM decor_extras_option WHERE txt_option_code = v.code);

-- ── Suppliers the customer may already have booked themselves ─────────
INSERT INTO vendor_master (txt_vendor_code, txt_vendor_name, enm_vendor_type, txt_address, txt_phone_number, bln_is_active, bln_is_deleted, created_date)
SELECT v.code, v.name, v.type, v.address, v.phone, true, false, now()
FROM (VALUES
    ('VN-001', 'Aperture Studios',      'PHOTOGRAPHY', '3 Camden High Street, London NW1 7JE', '020 7000 0201'),
    ('VN-002', 'Northlight Films',      'VIDEOGRAPHY', '22 Quay Street, Manchester M3 4AE',    '0161 000 0202'),
    ('VN-003', 'Sound & Motion',        'DJ',          '9 Bridge Road, Birmingham B1 2JR',     '0121 000 0203'),
    ('VN-004', 'Petal & Stem',          'FLORIST',     '77 Kings Road, London SW3 4NX',        '020 7000 0204'),
    ('VN-005', 'The Sweet Table Co.',   'CAKE',        '14 Mill Lane, St Albans AL1 3TE',      '01727 000205')
) AS v(code, name, type, address, phone)
WHERE NOT EXISTS (SELECT 1 FROM vendor_master WHERE txt_vendor_code = v.code);

-- ── A signed-in customer to walk the journey as ───────────────────────
-- Password: DevPassword123!   (bcrypt, cost 10)
-- Email is pre-verified so /auth/login succeeds without an SMTP server.
--
-- ROLE_USER, not ROLE_ADMIN: this account exists to exercise the customer
-- journey, and giving it staff privileges would mean the journey was never
-- actually tested against the authorisation rules customers hit.
INSERT INTO user_master (txt_email, txt_password, txt_name, txt_first_name, txt_last_name, txt_role,
                         bln_email_verified, bln_is_active, bln_is_deleted, bln_is_approved, created_date)
SELECT 'dev.customer@example.com',
       '$2a$10$0buxxJB/A5yXTo7yG01ArOAu6WML6qQtlUGPZ1r.NyOT6/823pLR2',
       'Dev Customer', 'Dev', 'Customer', 'ROLE_USER',
       true, true, false, true, now()
WHERE NOT EXISTS (SELECT 1 FROM user_master WHERE txt_email = 'dev.customer@example.com');

INSERT INTO customer_master (txt_cust_code, txt_cust_name, txt_first_name, txt_last_name, txt_email,
                             txt_phone_number_1, txt_address_1, txt_postal_code, txt_country_code,
                             ser_city_id, ser_country_id,
                             bln_is_active, bln_is_deleted, bln_is_approved, created_date)
SELECT 'CUS-DEV-001', 'Dev Customer', 'Dev', 'Customer', 'dev.customer@example.com',
       '+44 7700 900123', '1 Example Street', 'W1A 1AA', 'GB',
       (SELECT ser_city_id FROM city_master WHERE txt_city_code = 'LON'),
       (SELECT ser_country_id FROM country_master WHERE txt_country_code = 'GB'),
       true, false, true, now()
WHERE NOT EXISTS (SELECT 1 FROM customer_master WHERE txt_email = 'dev.customer@example.com');

-- A member of staff, for the admin portal.
-- Password: DevPassword123!
INSERT INTO user_master (txt_email, txt_password, txt_name, txt_first_name, txt_last_name, txt_role,
                         bln_email_verified, bln_is_active, bln_is_deleted, bln_is_approved, created_date)
SELECT 'dev.admin@example.com',
       '$2a$10$0buxxJB/A5yXTo7yG01ArOAu6WML6qQtlUGPZ1r.NyOT6/823pLR2',
       'Dev Admin', 'Dev', 'Admin', 'ROLE_ADMIN',
       true, true, false, true, now()
WHERE NOT EXISTS (SELECT 1 FROM user_master WHERE txt_email = 'dev.admin@example.com');

-- ── A booking to open ─────────────────────────────────────────────────
--
-- Why the seed carries one. The admin portal's Events list was empty on a
-- fresh database, so the portal end-to-end test could only run after the
-- customer-journey test had been run first and had left an event behind. A
-- suite that passes or fails depending on what ran before it is not a suite,
-- and the ordering was invisible — the portal test simply reported "no events
-- to open" and nobody could tell whether the list was broken or bare.
--
-- Filled in, not bare. The form marks the date, the guest count and the table
-- count required, so an event missing them cannot be submitted at all: the
-- test that was meant to prove the save path could never reach it. Decor is
-- left for the test to choose, because a coherent decor selection spans four
-- tables and none of it is what that test is about.
--
-- Dated well ahead on purpose, so the capacity rule has nothing to say about
-- it and a test that saves this booking is testing the save and not the rule.
INSERT INTO booking (txt_booking_code, ser_cust_id, created_date, bln_is_deleted, bln_is_active)
SELECT 'DEV-BOOK-001',
       (SELECT ser_cust_id FROM customer_master WHERE txt_email = 'dev.customer@example.com'),
       now(), false, true
WHERE NOT EXISTS (SELECT 1 FROM booking WHERE txt_booking_code = 'DEV-BOOK-001');

INSERT INTO event_master (txt_event_master_code, txt_event_master_name,
                          ser_cust_id, ser_event_type_id, ser_booking_id,
                          dte_event_date, num_number_of_guests, num_number_of_tables,
                          txt_contact_person_first_name, txt_contact_person_last_name,
                          txt_contact_person_phone_no,
                          txt_bride_first_name, txt_bride_last_name,
                          txt_groom_first_name, txt_groom_last_name,
                          txt_event_status, is_edit_allowed,
                          created_date, bln_is_deleted, bln_is_active)
SELECT 'DEV-EV-001', 'Dev Wedding',
       (SELECT ser_cust_id FROM customer_master WHERE txt_email = 'dev.customer@example.com'),
       (SELECT ser_event_type_id FROM event_type WHERE bln_is_deleted = false
         ORDER BY ser_event_type_id LIMIT 1),
       (SELECT ser_booking_id FROM booking WHERE txt_booking_code = 'DEV-BOOK-001'),
       -- Five years out. Far enough that no other seeded or test-created
       -- booking shares the day, which is what keeps the capacity rule quiet.
       (date_trunc('year', now()) + interval '5 years' + interval '190 days')::timestamp,
       120, 12,
       -- The form marks the contact person and, for a wedding, both families'
       -- names required. Left out, the seeded booking opens but cannot be
       -- submitted, which puts the end-to-end test back to filling in a form
       -- rather than proving one saves.
       'Dev', 'Contact', '+44 7700 900456',
       'Ayesha', 'Khan',
       'Bilal', 'Ahmed',
       'Enquiry', true,
       now(), false, true
WHERE NOT EXISTS (SELECT 1 FROM event_master WHERE txt_event_master_code = 'DEV-EV-001');

-- And its budget row, without which the booking is invisible.
--
-- The portal's Events list is not a list of events: it is a list of budgets,
-- filtered by the status tab across the top. An event with no event_budget row
-- appears under no tab at all — the search finds it through the API and the
-- screen shows nothing. That cost an afternoon to work out, which is why it is
-- written down here rather than quietly fixed.
--
-- Quoted, not Enquiry, and that matters for the end-to-end test. The moment a
-- booking has a quoted price the save path sets its status to 'Quoted', so a
-- seed that said 'Enquiry' was correct exactly once: the test found it under
-- the default tab, saved it, and every run after that looked for it where it no
-- longer was. Seeding the state the save produces makes the test repeatable.
--
-- num_qouted_price, spelled the way the column is. The typo is in the schema,
-- and correcting it there is not a seed's decision: 108 live rows and four save
-- paths read that name.
INSERT INTO event_budget (ser_event_master_id, txt_status, num_qouted_price, num_final_amount,
                          num_total_budget, num_paid_amount,
                          created_date, bln_is_deleted, bln_is_active)
SELECT e.ser_event_master_id, 'Quoted', 0, 0, 0, 0, now(), false, true
FROM event_master e
WHERE e.txt_event_master_code = 'DEV-EV-001'
  AND NOT EXISTS (SELECT 1 FROM event_budget b WHERE b.ser_event_master_id = e.ser_event_master_id);

COMMIT;

-- What landed.
SELECT 'event types'      AS catalogue, count(*) FROM event_type
UNION ALL SELECT 'venues',              count(*) FROM venue_master
UNION ALL SELECT 'halls',               count(*) FROM venue_master_detail
UNION ALL SELECT 'food items',          count(*) FROM menu_food_master
UNION ALL SELECT 'decor categories',    count(*) FROM decor_category_master
UNION ALL SELECT 'decor properties',    count(*) FROM decor_category_property_master
UNION ALL SELECT 'decor values',        count(*) FROM decor_category_property_value
UNION ALL SELECT 'extras and services', count(*) FROM decor_extras_master
UNION ALL SELECT 'suppliers',           count(*) FROM vendor_master
UNION ALL SELECT 'users',               count(*) FROM user_master;

-- ═══════════════════════════════════════════════════════════════════════
-- The menu tree
--
-- The journey's Food Menu screen does not read menu_food_master — that is the
-- older, flat structure still used elsewhere. It reads a three-level
-- menu_item tree (category → course → dish) filtered by role, through
-- /admin/menu/getMenuWithPrices.
--
-- Without these rows the Food Menu step renders its heading, an empty summary
-- rail and nothing else, which reads as a broken screen rather than an empty
-- catalogue.
-- ═══════════════════════════════════════════════════════════════════════

BEGIN;

-- The ids are stated rather than generated. ServiceMenuSelectionImpl calls
-- getAllActiveItemsByRoleId(1) with the 1 written into the source, so role 1
-- has to be the top of the tree. A plain multi-row INSERT does not guarantee
-- which row gets which identity value — Postgres assigned them in a different
-- order on the first run here, role 1 came out as "Dish", and every dish was
-- then returned as a top-level menu with no courses under it.
-- The codes and the hierarchy are production's, not invented ones.
--
-- They were MENU / COURSE / DISH with no parent_menu_role_id at all, and that
-- made a development database unable to accept a single new menu item:
-- validateParentRole treats a role with no parent role as a root, so "DISH
-- cannot have a parent" came back for every dish, every section and every
-- category alike. The menu screen could be looked at and not used.
--
-- parent_menu_role_id is what says a Subcategory goes under a Category and an
-- Item under a Subcategory. Production has it; this did not.
INSERT INTO menu_item_role (ser_menu_item_role_id, txt_code, txt_name, bln_is_component_role, bln_is_active, bln_is_deleted, created_date)
SELECT v.id, v.code, v.name, v.component, true, false, now()
FROM (VALUES
    (1, 'CATEGORY',    'Category',    false),
    (2, 'SUBCATEGORY', 'Subcategory', false),
    (3, 'ITEM',        'Item',        false),
    -- The component kinds, which are the headings on a stand. Production has
    -- three; one is enough to show what they are for.
    (22, 'SELECTION',  'Selection Items', true)
) AS v(id, code, name, component)
WHERE NOT EXISTS (SELECT 1 FROM menu_item_role WHERE txt_code = v.code);

UPDATE menu_item_role SET parent_menu_role_id = 1 WHERE txt_code = 'SUBCATEGORY' AND parent_menu_role_id IS NULL;
UPDATE menu_item_role SET parent_menu_role_id = 2 WHERE txt_code = 'ITEM' AND parent_menu_role_id IS NULL;

-- Identity columns do not advance when a value is supplied, so the sequence
-- has to be moved past the rows just inserted or the next generated id
-- collides with one of them.
SELECT setval(pg_get_serial_sequence('menu_item_role', 'ser_menu_item_role_id'),
              (SELECT max(ser_menu_item_role_id) FROM menu_item_role));

-- Level 1: the menus a customer chooses between.
-- getAllActiveItemsByRoleId(1) is what the journey asks for, so these carry
-- the first role; the id is looked up rather than assumed.
INSERT INTO menu_item (txt_code, txt_name, txt_description, txt_path, num_display_order,
                       bln_is_selectable, bln_is_composite, bln_has_selection_limit,
                       ser_menu_item_role_id, bln_is_active, bln_is_deleted, created_date)
SELECT v.code, v.name, v.descr, text2ltree(v.path), v.ord, false, false, false,
       (SELECT ser_menu_item_role_id FROM menu_item_role WHERE txt_code = 'CATEGORY'),
       true, false, now()
FROM (VALUES
    ('MI-SET',  'Set Menu',      'Our classic three-course service.',                  'set',  1),
    ('MI-FEAST','Sharing Feast', 'Served to the centre of the table, family style.',   'feast', 2)
) AS v(code, name, descr, path, ord)
WHERE NOT EXISTS (SELECT 1 FROM menu_item WHERE txt_code = v.code);

-- Level 2: the courses within each menu.
INSERT INTO menu_item (txt_code, txt_name, txt_description, txt_path, num_display_order,
                       bln_is_selectable, bln_is_composite, bln_has_selection_limit, num_selection_limit,
                       parent_menu_item_id, ser_menu_item_role_id, bln_is_active, bln_is_deleted, created_date)
SELECT v.code, v.name, v.descr, text2ltree(v.path), v.ord, false, false, true, v.limit_n,
       (SELECT ser_menu_item_id FROM menu_item WHERE txt_code = v.parent),
       (SELECT ser_menu_item_role_id FROM menu_item_role WHERE txt_code = 'SUBCATEGORY'),
       true, false, now()
FROM (VALUES
    ('MI-SET-STARTERS', 'Starters',   'Choose up to three, served to share on arrival.', 'set.starters',  1, 3, 'MI-SET'),
    ('MI-SET-MAINS',    'Main course','Choose up to three.',                             'set.mains',     2, 3, 'MI-SET'),
    ('MI-SET-SIDES',    'Sides',      'Choose up to two.',                               'set.sides',     3, 2, 'MI-SET'),
    ('MI-SET-DESSERTS', 'Desserts',   'Choose up to two.',                               'set.desserts',  4, 2, 'MI-SET'),
    ('MI-SET-DRINKS',   'Drinks',     'Choose up to two.',                               'set.drinks',    5, 2, 'MI-SET'),
    ('MI-FEAST-GRILL',  'From the grill', 'Choose up to three.',                         'feast.grill',   1, 3, 'MI-FEAST'),
    ('MI-FEAST-CURRY',  'Curries',    'Choose up to three.',                             'feast.curry',   2, 3, 'MI-FEAST'),
    ('MI-FEAST-SWEET',  'Sweets',     'Choose up to two.',                               'feast.sweet',   3, 2, 'MI-FEAST')
) AS v(code, name, descr, path, ord, limit_n, parent)
WHERE NOT EXISTS (SELECT 1 FROM menu_item WHERE txt_code = v.code);

-- Level 3: the dishes. Priced per guest, which is how this business quotes.
INSERT INTO menu_item (txt_code, txt_name, txt_description, txt_path, num_display_order, num_price,
                       enm_price_multiplier_type, bln_is_selectable, bln_is_composite,
                       parent_menu_item_id, ser_menu_item_role_id, bln_is_active, bln_is_deleted, created_date)
SELECT v.code, v.name, v.descr, text2ltree(v.path), v.ord, v.price, 'PER_GUEST', true, false,
       (SELECT ser_menu_item_id FROM menu_item WHERE txt_code = v.parent),
       (SELECT ser_menu_item_role_id FROM menu_item_role WHERE txt_code = 'ITEM'),
       true, false, now()
FROM (VALUES
    ('MI-D-CT',  'Chicken Tikka',        'Marinated overnight, cooked in the tandoor.',        'set.starters.ct',  1, 4.50,  'MI-SET-STARTERS'),
    ('MI-D-SK',  'Seekh Kebab',          'Minced lamb, coriander and green chilli.',           'set.starters.sk',  2, 4.75,  'MI-SET-STARTERS'),
    ('MI-D-VS',  'Vegetable Samosa',     'Potato and pea, hand folded.',                       'set.starters.vs',  3, 3.25,  'MI-SET-STARTERS'),
    ('MI-D-PT',  'Paneer Tikka',         'Char-grilled with peppers and onion.',               'set.starters.pt',  4, 4.25,  'MI-SET-STARTERS'),
    ('MI-D-CP',  'Chaat Papri',          'Crisp pastry, chickpeas, yoghurt, tamarind.',        'set.starters.cp',  5, 3.75,  'MI-SET-STARTERS'),
    ('MI-D-CK',  'Chicken Karahi',       'Tomato, ginger and green chilli.',                   'set.mains.ck',     1, 9.50,  'MI-SET-MAINS'),
    ('MI-D-LB',  'Lamb Biryani',         'Slow-cooked, sealed and baked.',                     'set.mains.lb',     2, 12.00, 'MI-SET-MAINS'),
    ('MI-D-BC',  'Butter Chicken',       'Mild, with cream and fenugreek.',                    'set.mains.bc',     3, 9.75,  'MI-SET-MAINS'),
    ('MI-D-DM',  'Daal Makhani',         'Black lentils, cooked overnight.',                   'set.mains.dm',     4, 7.50,  'MI-SET-MAINS'),
    ('MI-D-SB',  'Grilled Sea Bass',     'Herb butter, lemon.',                                'set.mains.sb',     5, 14.50, 'MI-SET-MAINS'),
    ('MI-D-KS',  'Kachumber Salad',      'Cucumber, tomato, red onion, lime.',                 'set.sides.ks',     1, 2.50,  'MI-SET-SIDES'),
    ('MI-D-RA',  'Raita',                'Yoghurt, mint and cumin.',                           'set.sides.ra',     2, 2.00,  'MI-SET-SIDES'),
    ('MI-D-CH',  'Mint & Tamarind Chutneys', 'Made fresh on the day.',                         'set.sides.ch',     3, 1.75,  'MI-SET-SIDES'),
    ('MI-D-GJ',  'Gulab Jamun',          'Warm, in cardamom syrup.',                           'set.desserts.gj',  1, 3.50,  'MI-SET-DESSERTS'),
    ('MI-D-KH',  'Kheer',                'Rice pudding with pistachio.',                       'set.desserts.kh',  2, 3.25,  'MI-SET-DESSERTS'),
    ('MI-D-CT2', 'Chocolate Torte',      'Flourless, with crème fraîche.',                     'set.desserts.ct2', 3, 4.50,  'MI-SET-DESSERTS'),
    ('MI-D-ML',  'Mango Lassi',          'Alphonso mango and yoghurt.',                        'set.drinks.ml',    1, 2.75,  'MI-SET-DRINKS'),
    ('MI-D-RS',  'Rose Sharbat',         'Chilled, lightly sweetened.',                        'set.drinks.rs',    2, 2.50,  'MI-SET-DRINKS'),
    ('MI-D-MC',  'Masala Chai',          'Served through the evening.',                        'set.drinks.mc',    3, 1.95,  'MI-SET-DRINKS'),
    ('MI-D-LC',  'Lamb Chops',           'Twice-cooked, chilli and lime.',                     'feast.grill.lc',   1, 13.50, 'MI-FEAST-GRILL'),
    ('MI-D-TP',  'Tandoori Prawns',      'King prawns, garlic and carom.',                     'feast.grill.tp',   2, 12.75, 'MI-FEAST-GRILL'),
    ('MI-D-MT',  'Malai Tikka',          'Cream, cheese and white pepper.',                    'feast.grill.mt',   3, 8.50,  'MI-FEAST-GRILL'),
    ('MI-D-RG',  'Rogan Josh',           'Kashmiri chilli and fennel.',                        'feast.curry.rg',   1, 11.00, 'MI-FEAST-CURRY'),
    ('MI-D-SP',  'Saag Paneer',          'Spinach, garlic and cream.',                         'feast.curry.sp',   2, 7.95,  'MI-FEAST-CURRY'),
    ('MI-D-CB',  'Chana Bhatura',        'Chickpea curry with fried bread.',                   'feast.curry.cb',   3, 7.25,  'MI-FEAST-CURRY'),
    ('MI-D-JL',  'Jalebi',               'Served warm.',                                       'feast.sweet.jl',   1, 3.00,  'MI-FEAST-SWEET'),
    ('MI-D-BF',  'Barfi Selection',      'Pistachio, coconut and almond.',                     'feast.sweet.bf',   2, 3.75,  'MI-FEAST-SWEET')
) AS v(code, name, descr, path, ord, price, parent)
WHERE NOT EXISTS (SELECT 1 FROM menu_item WHERE txt_code = v.code);

-- Two stands, in the two states the live catalogue is actually in.
--
-- A stand is one line on the menu and several things on the table. The model
-- has always been able to say that, and in production two items do — but
-- seventeen do not, and three of those have the contents typed into the item's
-- *name*: "Irn-bru, Cola, Water & Diet on request" is one row.
--
-- Both are seeded because they are the two halves of the menu screen worth
-- exercising: one that needs its name lifted apart, and one that is simply
-- flagged as a stand with nothing on it, which nine live rows are and which is
-- usually a flag somebody should clear.
INSERT INTO menu_item (txt_code, txt_name, txt_description, txt_path, num_display_order, num_price,
                       enm_price_multiplier_type, bln_is_selectable, bln_is_composite,
                       parent_menu_item_id, ser_menu_item_role_id, bln_is_active, bln_is_deleted, created_date)
SELECT v.code, v.name, v.descr, text2ltree(v.path), v.ord, v.price, 'PER_GUEST', true, true,
       (SELECT ser_menu_item_id FROM menu_item WHERE txt_code = v.parent),
       (SELECT ser_menu_item_role_id FROM menu_item_role WHERE txt_code = 'ITEM'),
       true, false, now()
FROM (VALUES
    ('MI-D-SOFT', 'Irn-bru, Cola, Water & Diet on request', 'Self-serve through the evening.',
     'set.drinks.soft', 4, 2.25, 'MI-SET-DRINKS'),
    ('MI-D-SWEET', 'Sweet Cart', 'Help yourself.',
     'feast.sweet.cart', 3, 2.50, 'MI-FEAST-SWEET')
) AS v(code, name, descr, path, ord, price, parent)
WHERE NOT EXISTS (SELECT 1 FROM menu_item WHERE txt_code = v.code);

COMMIT;

SELECT r.txt_name AS level, count(mi.*) AS items
FROM menu_item_role r LEFT JOIN menu_item mi ON mi.ser_menu_item_role_id = r.ser_menu_item_role_id
GROUP BY r.txt_name, r.ser_menu_item_role_id ORDER BY r.ser_menu_item_role_id;

-- ═══════════════════════════════════════════════════════════════════════
-- Consultations
-- ═══════════════════════════════════════════════════════════════════════
--
-- Starting data, not defaults. Every value here is editable in the admin
-- portal — hosts, hours, meeting lengths, buffers, notice — and the point of
-- seeding is that a fresh checkout has something to book, not that these are
-- the settings the business should use.
--
-- Two hosts on purpose: one host makes round-robin assignment untestable, and
-- the second one having different hours is how you notice that slots are
-- worked out per person rather than for the company.

BEGIN;

INSERT INTO consultation_host (txt_display_name, txt_email, txt_time_zone, bln_is_active, bln_is_deleted)
SELECT * FROM (VALUES
    ('Amina Rahman', 'amina@diamondevents.example', 'Europe/London', true, false),
    ('Tom Whitfield', 'tom@diamondevents.example', 'Europe/London', true, false)
) AS v(name, email, zone, active, deleted)
WHERE NOT EXISTS (SELECT 1 FROM consultation_host WHERE lower(txt_email) = lower(v.email));

INSERT INTO consultation_type (
    txt_name, txt_description, num_duration_minutes,
    num_buffer_before_minutes, num_buffer_after_minutes,
    num_minimum_notice_hours, num_maximum_advance_days,
    txt_location_kind, bln_requires_confirmation, bln_create_video_link,
    num_confirmation_window_hours, bln_is_active, bln_is_deleted)
SELECT * FROM (VALUES
    -- Books outright: the customer has just finished the journey and should
    -- leave with a time, not a maybe.
    ('Initial consultation',
     'A first conversation about your event — the detail, the pricing and what happens next.',
     45, 0, 15, 24, 90, 'VIDEO', false, true, 48, true, false),
    -- Needs agreeing: somebody has to be free to travel, so the team decides.
    ('Venue visit',
     'Meet us at the venue to walk the room and talk through the layout.',
     60, 30, 30, 72, 90, 'IN_PERSON', true, false, 48, true, false)
) AS v(name, descr, mins, bb, ba, notice, advance, kind, confirm, video, confirm_window, active, deleted)
WHERE NOT EXISTS (SELECT 1 FROM consultation_type WHERE txt_name = v.name);

-- Amina: Monday to Friday, 09:00-17:00 with an hour for lunch.
INSERT INTO consultation_availability_rule (ser_host_id, num_day_of_week, tme_start_time, tme_end_time, bln_is_deleted)
SELECT h.ser_host_id, d.day, t.starts, t.ends, false
FROM consultation_host h
CROSS JOIN (VALUES (1), (2), (3), (4), (5)) AS d(day)
CROSS JOIN (VALUES ('09:00'::time, '13:00'::time), ('14:00'::time, '17:00'::time)) AS t(starts, ends)
WHERE lower(h.txt_email) = 'amina@diamondevents.example'
  AND NOT EXISTS (
      SELECT 1 FROM consultation_availability_rule r
      WHERE r.ser_host_id = h.ser_host_id AND r.num_day_of_week = d.day AND r.tme_start_time = t.starts);

-- Tom: afternoons only, and Saturdays — deliberately different, so anything
-- that quietly assumes one shared calendar shows up straight away.
INSERT INTO consultation_availability_rule (ser_host_id, num_day_of_week, tme_start_time, tme_end_time, bln_is_deleted)
SELECT h.ser_host_id, d.day, '13:00'::time, '18:00'::time, false
FROM consultation_host h
CROSS JOIN (VALUES (2), (4), (6)) AS d(day)
WHERE lower(h.txt_email) = 'tom@diamondevents.example'
  AND NOT EXISTS (
      SELECT 1 FROM consultation_availability_rule r
      WHERE r.ser_host_id = h.ser_host_id AND r.num_day_of_week = d.day);

-- Christmas, as an example of a closure. Bank holidays are managed in the
-- portal; this is here so the exception path has something in it.
INSERT INTO consultation_availability_exception (ser_host_id, dte_on_date, bln_is_available, txt_reason, bln_is_deleted)
SELECT h.ser_host_id, d.on_date, false, 'Closed', false
FROM consultation_host h
CROSS JOIN (VALUES ('2026-12-25'::date), ('2026-12-26'::date), ('2027-01-01'::date)) AS d(on_date)
WHERE h.bln_is_deleted = false
  AND NOT EXISTS (
      SELECT 1 FROM consultation_availability_exception e
      WHERE e.ser_host_id = h.ser_host_id AND e.dte_on_date = d.on_date);

COMMIT;

SELECT h.txt_display_name AS host, count(r.*) AS availability_rules
FROM consultation_host h
LEFT JOIN consultation_availability_rule r ON r.ser_host_id = h.ser_host_id
GROUP BY h.txt_display_name, h.ser_host_id ORDER BY h.ser_host_id;
