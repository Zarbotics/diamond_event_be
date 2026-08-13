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
INSERT INTO menu_item_role (ser_menu_item_role_id, txt_code, txt_name, bln_is_component_role, bln_is_active, bln_is_deleted, created_date)
SELECT v.id, v.code, v.name, v.component, true, false, now()
FROM (VALUES
    (1, 'MENU',   'Menu',   false),
    (2, 'COURSE', 'Course', false),
    (3, 'DISH',   'Dish',   false)
) AS v(id, code, name, component)
WHERE NOT EXISTS (SELECT 1 FROM menu_item_role WHERE txt_code = v.code);

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
       (SELECT ser_menu_item_role_id FROM menu_item_role WHERE txt_code = 'MENU'),
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
       (SELECT ser_menu_item_role_id FROM menu_item_role WHERE txt_code = 'COURSE'),
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
       (SELECT ser_menu_item_role_id FROM menu_item_role WHERE txt_code = 'DISH'),
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

COMMIT;

SELECT r.txt_name AS level, count(mi.*) AS items
FROM menu_item_role r LEFT JOIN menu_item mi ON mi.ser_menu_item_role_id = r.ser_menu_item_role_id
GROUP BY r.txt_name, r.ser_menu_item_role_id ORDER BY r.ser_menu_item_role_id;
