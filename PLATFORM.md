# Diamond Events — Platform Requirements, Analysis and Plan

**Status:** living document. Last updated 14 August 2026.

---

## 0. How to use this document

This is the single source of truth for what the platform does, what is
actually wired up, what is dead, what has been fixed, and what is left. It
covers all three repositories together, because most real behaviour crosses
at least two of them.

**It is updated as work happens, not afterwards.** When a task is finished the
row moves to Done with a note on how it was verified. When a new use case or
edge case turns up mid-work — and they do; several in §9 were found by writing
tests, not by reading code — it is added to the relevant section on the spot,
whether or not it gets fixed straight away.

### Status vocabulary

| | Meaning |
|---|---|
| ✅ | Working, and a test proves it |
| ✔︎ | Working, verified by hand, no automated test |
| 🟡 | Works, with a known gap recorded here |
| ❌ | Broken or missing |
| ⬜ | Not started |
| ❓ | Blocked on a business decision, not a technical one |
| 💀 | Dead code — present, unreachable |

### Where the work happens

| Repository | Branch | What it is |
|---|---|---|
| `diamond_event_be` | `feat/platform-rebuild` | Spring Boot 3.4.5 / Java 21 API, PostgreSQL 16 |
| `diamond_ev_cj` | `feat/platform-rebuild` | React 19 / Vite customer booking journey |
| `diamond_ev_admin` | `feat/platform-rebuild` | React admin portal (CRA + craco) |

The existing branches are never touched. All work is on `feat/platform-rebuild`
in each repository.

---

## 1. What the business does

Diamond Events is a UK events company. A customer books a venue and everything
that goes in it — a hall, a menu, decor, services, extras and outside
suppliers — for a wedding, a walima, a birthday or a corporate event. Staff
price the booking, take payments against it, and the kitchen works from an
itinerary derived from the menu the customer chose.

Three applications serve that:

- **Customer journey** — a fourteen-step booking flow, signed in with Google.
- **Admin portal** — staff manage the catalogue, price it, and work bookings.
- **API** — the domain, the database and the documents.

---

## 2. Domain model

61 entities. Grouped by what they are for, with an honest status.

### 2.1 The booking itself

| Entity | Table | Status | Note |
|---|---|---|---|
| `EventMaster` | `event_master` | ✅ | The booking. The aggregate root in practice. |
| `EventRunningOrder` | `event_running_order` | ✅ | Times: guest arrival, meal, end of night, plus ceremony-specific slots. |
| `EventBudget` | `event_budget` | ✅ | Totals per booking. |
| `EventPayment` + `EventPaymentDocument` | `event_payment` | ✔︎ | Staff-side. No customer-facing payment. |
| `EventQuote` / `EventQuoteLine` | `event_quote` | 💀 | **Orphaned.** Nothing references them. Tables exist, empty. Superseded by `EventBudget`. |

### 2.2 What the customer chose

| Entity | Status | Note |
|---|---|---|
| `EventMenuCategorySelection` | ✅ | No repository — cascaded from `EventMaster`. |
| `EventMenuSubCategorySelection` | ✅ | As above. |
| `EventMenuFoodSelection` | ✅ | The dishes. 320 rows in dev. |
| `EventDecorCategorySelection` | ✅ | |
| `EventDecorPropertySelection` | ✅ | |
| `EventDecorPropertyValueSelection` | ✅ | Cascaded. |
| `EventDecorExtrasSelection` | ✅ | Services and extras both land here. |
| `EventDecorReferenceDocument` | ✅ | Customer's uploaded inspiration images. |
| `EventVendorMasterSelection` | 💀 ❓ | 0 rows, correctly: the supplier picker is commented out of the journey. See §5.6. |
| `EventFoodSelection` | 💀 | **Orphaned.** Superseded by `EventMenuFoodSelection`. |
| `EventServicesMaster` | 💀 | **Orphaned.** Services are stored as `EventDecorExtrasSelection`. |

### 2.3 Catalogue

| Entity | Status | Dev rows |
|---|---|---|
| `MenuItem` (ltree tree: category → sub-category → dish) | ✅ | 37 |
| `MenuItemRole` | ✅ | 3 |
| `MenuFoodMaster` | ✔︎ | 20 |
| `MenuComponent` | ✔︎ | 0 |
| `Ingredient`, `MenuItemIngredient` | ✔︎ | 0 |
| `VenueMaster` / `VenueMasterDetail` (halls) / `VenueMasterDetailDocument` | ✅ | 5 / 8 |
| `DecorCategoryMaster` → `DecorCategoryPropertyMaster` → `DecorCategoryPropertyValue` (+ documents) | ✅ | 4 / 6 / 16 |
| `DecorExtrasMaster` / `DecorExtrasOption` (+ documents) | ✅ | 10 / 18 |
| `EventType` (+ `EventTypeDocument`) | ✅ | 11 |
| `VendorMaster` | ✔︎ | 5 |
| `CountryMaster` / `StateMaster` / `CityMaster` | 🟡 | 1 / 1 / 3. `CountryMaster.isActive` was a primitive over a nullable column — a NULL took out the whole venue list with a 500. Fixed. |

### 2.4 Pricing

| Entity | Status | Note |
|---|---|---|
| `PriceVersion` | ✅ | Versioned price books. Exactly one default — now enforced under failure. |
| `MenuItemPrice` | ✅ | Price per item per version. |
| `PriceEntry` | ✔︎ | |
| `PricingRule` | ✔︎ | |

### 2.5 Kitchen itinerary

| Entity | Status | Note |
|---|---|---|
| `ItineraryItem` / `ItineraryItemType` | ✔︎ | Prep steps and their kinds. |
| `ItineraryAssignment` / `ItineraryAssignmentDetail` | ✔︎ | Which prep steps a dish needs, with a multiplier. |
| `MenuItemItineraryMap` | ✔︎ | |
| `EventMenuItinerary` | ✔︎ | Per-dish calculated quantities for one booking. |
| `EventItinerarySummary` | ✔︎ | Aggregated per booking. |
| `EventItineraryResult` | 💀 | **Orphaned.** |

### 2.6 Catering-only (food delivery, no venue)

| Entity | Status |
|---|---|
| `CateringDeliveryBooking` / `CateringDeliveryItemDetail` | 💀 ❓ See §5.4 — the feature is parked, both entry points commented out |

### 2.7 Identity

| Entity | Status | Note |
|---|---|---|
| `UserMaster` | ✅ | Roles `ROLE_ADMIN` / `ROLE_USER`. |
| `CustomerMaster` | ✅ | The booking customer. |
| `RefreshToken` | ✅ | |
| `EmailVerificationToken` | ✔︎ | |
| `SsoHandoffCode` | ✅ | Single-use code for the OAuth → SPA handoff. |
| `NotificationMaster` | ✔︎ | The only paginated endpoint in the API. |

### 2.8 Dead entities — summary

Five entities are referenced by nothing outside their own file. Hibernate
`ddl-auto=update` still creates their tables, so they exist and are empty:

`EventFoodSelection`, `EventItineraryResult`, `EventQuote`, `EventQuoteLine`,
`EventServicesMaster`

**Recommendation:** delete the classes, leave the tables. Dropping tables is
irreversible and they cost nothing; the classes cost attention every time
somebody greps for how food selection works and finds two answers. ⬜ *Not yet
done — see §10.*

---

## 3. API surface

40 controllers, roughly 340 endpoints.

**Every endpoint is `POST`**, including reads (`POST /customerMaster/getAllData`).
Only `/notifications` paginates. Nothing else does, so any list endpoint returns
every row — fine for a catalogue, not fine for `event_master`, which grows with
every booking forever.

| Area | Base path |
|---|---|
| Auth | `/auth` |
| Bookings | `/eventMaster`, `/eventBudget`, `/eventPayment` |
| Customers | `/customerMaster` |
| Menu | `/menu/item`, `/menu/component`, `/menu/ingredient`, `/menuFoodMaster`, `/menuItemRole`, `/admin/menu` |
| Pricing | `/menu/price-version`, `/api/menu-item-price`, `/menu/price-entry`, `/api/price-calculator` |
| Decor | `/decorCategoryMaster`, `/decorCategoryPropertyMaster`, `/decorCategoryPropertyValue`, `/decorExtras`, `/extrasOption` |
| Venues | `/venueMaster`, `/countryMaster`, `/stateMaster`, `/cityMaster` |
| Itinerary | `/itinerary/assignment`, `/itinerary/item-type`, `/admin/itinerary`, `/eventItinerary` |
| Catering-only | `/cateringDelivery`, `/cateringPayment` |
| Other | `/vendorMaster`, `/eventType`, `/notifications`, `/analytics`, `/dashboardStats`, `/deimg` |

---

## 4. Security model

| Concern | Status | Note |
|---|---|---|
| Sign-in | ✅ | Google OAuth2 in development; **master also has Apple**, kept out of dev branches because there is one Apple developer account and it is bound to production. `VITE_APPLE_SIGNIN_ENABLED` gates the button. |
| Handoff to the SPA | ✅ | Single-use `SsoHandoffCode`. A token-in-URL path still exists and the tests use it. 🟡 Worth retiring — see §10. |
| Roles | ✅ | `ROLE_ADMIN`, `ROLE_USER`. |
| Privilege escalation | ✅ | `/auth/signup` is public and set `ROLE_ADMIN` on every new account — anyone who posted an email and password became an administrator. Now `ROLE_USER`. |
| Ownership | ✅ | `AccessGuard` asserts a customer may only touch their own bookings. 10 tests. |
| Unauthenticated API calls | ✅ | 401 JSON, never a 302 — the frontend interceptor follows redirects silently and the customer sits on a screen that never recovers. |
| CORS | ✅ | localhost origins included by default. |
| Personal data in logs | ✅ | Fixed; guarded by a test. See §9. |

---

## 5. The customer journey

Fourteen steps. `numFormState` is saved with the booking at each one and is
what restores a returning customer. Redux is persisted to `localStorage`, so a
reload in the same browser is restored from there; `numFormState` covers a
different device, a cleared browser, or picking a booking from the list.

### 5.1 The steps

| # | Route | Purpose | Status |
|---|---|---|---|
| 1 | `personalDetail` | Name, phone, email | ✅ |
| 2 | `selectEvent` | New booking, or resume one | ✅ |
| 3 | `getStarted` | Which kind of event | ✅ |
| 4 | `clientDetail` | Couple / celebrant / contact | ✅ |
| 5 | `selectEventType` | The date | ✅ |
| 6 | `eventDetail` | Guests, tables, running order | ✅ |
| 7 | `eventVenue` | City → venue → hall | ✅ |
| 8 | `eventCatering` | Menu | ✅ |
| 9 | `services` | Optional | ✅ |
| 10 | `decor` | Optional, with image upload | ✅ |
| 11 | `extras` | Optional | ✅ |
| 12 | `externalSuuppliers` | Outside suppliers + terms | ✅ |
| 13 | `review` | Check everything before submitting | ✅ |
| 14 | `confirmed` | Reference number, consultation link | ✅ |

*(`externalSuuppliers` is misspelled in the route. Cosmetic; renaming it breaks
any bookmarked URL. Left alone deliberately.)*

### 5.2 Business rules

| Rule | Status | Note |
|---|---|---|
| A date already booked cannot be chosen | ✅ | Day cells render inert. Verified — repeated test runs filled August 2026 and the calendar correctly offered nothing. |
| Past dates cannot be chosen | ✅ | |
| A hall smaller than the guest count is refused | ✅ | "Your guest count is 150, which exceeds the hall's capacity." |
| Each course has a selection limit | ✅ | Remaining dishes lock once reached. |
| Couple events ask for bride and groom | ✅ | wedding, nikkah, walima, mehndi, reception, engagement, anniversary. Others ask for a celebrant. |
| Services, decor and extras are optional | ✅ | The step advances with nothing chosen. |
| Terms must be accepted | ✅ | Real checkbox. |
| The event reference is unique | ✅ | Was read-max-and-add-one with nothing preventing collisions; two customers starting at once both got `DE-26-1005`. Now a retry loop plus a partial unique index. |

### 5.3 Edge cases

Anything not marked ✅/✔︎ is an open item and is carried into §10.

| Edge case | Status |
|---|---|
| Customer reloads mid-journey | ✅ Redux persisted to localStorage |
| Customer returns on another device | ✅ Restored from `numFormState` |
| Customer finishes the last selection step and comes back | ✅ Resumes at Review. Recorded step 4 (Client Details) until fixed — eight steps of position discarded |
| Customer has several bookings | ✅ Listed on `selectEvent` |
| A month is fully booked | ✅ Calendar offers the next month |
| A menu course has no options | ✔︎ "Low-lying fog" is seeded with none to exercise this |
| Access token expires mid-journey | ✔︎ Axios interceptor refreshes and retries on 401 |
| Refresh token invalid | ✔︎ Interceptor logs out |
| Two customers race for the same date | 🟡 Server rejects the second with `already_booked`, but there is no database constraint behind it |
| Two customers race for the same reference | ✅ Partial unique index |
| A step half-saves | ✅ One transaction; a caught failure rolls back |
| Backend unreachable mid-journey | 🟡 Toast, no retry offered |
| Customer uploads a huge or non-image file | ✅ Client checks type and 3MB; server now allowlists extensions and caps at 3MB/30MB |
| Upload named `../../../etc/…` | ✅ Was an arbitrary file write **as root**. See §9 |
| Uploaded SVG or HTML served back from the API origin | ✅ Refused at write; content type now mapped from the extension, `nosniff` set |
| Customer books with 0 guests | ✅ Refused, with the message on the field |
| Guest count larger than every room the company has | ✅ Was an unwinnable screen — see §9 |
| Guest count larger than a particular room | ✅ Refused up front and says why, rather than on click |
| Customer picks a date years out | 🟡 No upper bound |
| Journey completed twice in two tabs | ❌ Not considered |
| Admin edits a booking a customer has open | ❌ No optimistic locking anywhere |

### 5.4 Catering-only flow 💀 ❓

`BookCatering` is a complete second booking flow for food delivery without a
venue. Its route is registered and `cateringDelivery/saveOrUpdate` still
exists, but **nothing can reach it**: the "Book Food Delivery" selector in
`GetStarted` is commented out, and so is the branch in
`AddOrSelectExistingEvent` that lists existing catering bookings to resume.

It has been repaired anyway (real checkboxes, named info button) so it works if
it comes back, and the finding is recorded at the top of the file. **Whether it
comes back is a business decision, not a technical one.**

### 5.6 External suppliers 💀 ❓

Step 12 is named `externalSuuppliers` and the entity, the admin `vendors`
screen and five seeded suppliers all exist. **The customer is never shown
them.** `fetchVendors()` is commented out, so is the picker that would render
them, and so is the notes box beside it.

What the step actually renders now is two static informational blocks — "Table
plan" and "Room layout", both *"to be sent no later than 4 weeks before
event"* — plus the terms checkbox and Submit. It is a notes-and-terms step
wearing a supplier step's name.

The payload still carries `vendorMasterSelections` and
`txtExternalSupplierRemarks` on every submission; both are always empty. That
is why `event_vendor_master_selection` has no rows: nothing is broken, there is
simply nothing to choose. Recorded here because "the table is empty" reads like
data loss until you find out why, and I spent time on exactly that.

### 5.5 Marketing site 💀 ❓

`Home`, `About`, `Stages`, `Venues`, `Catering`, `Events`, `Navbar` and
`Footer` all exist, and the entire `Layout` route that renders them is
commented out. The portal links out to `https://diamondevents.uk/` instead.

The Navbar's five nav links are `<span onClick>` — not keyboard operable — and
point at routes that no longer resolve. **Deliberately left alone:** fixing
accessibility on a screen nobody can reach is waste, and whether the React
marketing site returns or the WordPress site stays is a business decision.

---

## 6. Admin portal

182 components. Screens, by route:

| Area | Routes |
|---|---|
| Bookings | `events`, `event-master/add`, `event-master/:eventId`, `event-master/view/:eventId`, `event-stats` |
| Customers | `customers`, `customers/:id` |
| Menu | `food-menu`, `menu-category`, `menu-management`, `menu-composition` |
| Pricing | `price-setup`, `price-assignment`, `price-assignment/add`, `price-assignment/:id` |
| Decor | `decor`, `decor-extras`, `decor-properties`, `decor-property-values`, `decor-services` |
| Itinerary | `itinerary-item`, `itinerary-type`, `itinerary-assignment` |
| Catalogue | `venues`, `vendors`, `caterings` |
| Other | `calender-schedule`, `campaign-analysis`, `demo-2`, `fb*` (Firebase demo leftovers) |

**Status:** ✅ third-party CDN assets removed (Jost self-hosted, Font Awesome
and jVectorMap dropped). Otherwise largely unreviewed — see §10.

`demo-2` and the `fb*` routes are template leftovers. 💀

---

## 7. Cross-cutting concerns

| Concern | Status | Note |
|---|---|---|
| Schema ownership | ✅ | Flyway owns it (V2–V5). `ddl-auto=update` still on, which is why orphan entities still get tables. 🟡 |
| Transactions | ✅ | See §9. |
| Logging | ✅ | See §9. |
| Third-party assets | ✅ | Nothing loaded from an external host, in either frontend. Asserted by a test. |
| Accessibility | 🟡 | Journey is WCAG 2.2 AA on the paths covered. Admin portal unreviewed. |
| Pagination | ❌ | One endpoint out of ~340. |
| REST semantics | ❌ | Everything is POST. |
| Optimistic locking | ❌ | Nowhere. |
| API documentation | ❌ | None. |
| Error monitoring | ❌ | None. |

---

## 8. Testing

| Suite | Count | Runs with |
|---|---|---|
| Backend unit | 55 | `mvn test` |
| Backend integration | 16 | `mvn verify` (skips itself without a database) |
| Journey end-to-end | 20 | `npm run test:e2e` — desktop and mobile |
| Admin | 0 | ❌ |

The end-to-end tests write real bookings and must be pointed at a development
database. They found faults no unit test could: a Save button that saved and
did not advance on seven separate steps; nine controls that could not be
operated by keyboard; a menu rail that covered the Save button on a phone.

**Every fix below was checked by removing it and watching the test fail.** A
test that passes with and without the fix proves nothing.

---

## 9. Work completed

### Security
- ✅ **Privilege escalation.** `/auth/signup` set `ROLE_ADMIN` on every new
  account. Anyone who posted an email and a password became an administrator.
- ✅ **401 not 302** for unauthenticated API calls.
- ✅ **Ownership checks** — `AccessGuard`, 10 tests.

### Correctness
- ✅ **Venue never saved.** `ser_venue_master_id` was null on every booking.
  Set in eight places in the service; now derived in the setter so a ninth
  cannot get it wrong.
- ✅ **Catalogue hiding 4 of 5 services.** `LEFT JOIN FETCH … WHERE o.blnIsDeleted = false`
  collapses the outer join to an inner one, so anything without options vanished.
- ✅ **Duplicate event references.** Read-max-and-add-one with no constraint.
  Retry loop plus partial unique index (`V5`).
- ✅ **`CountryMaster.isActive`** primitive over a nullable column — one NULL
  returned a 500 for the entire venue list.
- ✅ **Multi-step writes commit whole or not at all.** 16 methods did 2+
  repository writes with no transaction. The subtlety: this service layer
  catches its own exceptions and returns an error result, and Spring only rolls
  back on exceptions that *propagate out* — so the annotation alone would have
  changed nothing. `UtilTransaction.markRollbackOnly()` closes it.
  `ServicePriceVersionImpl.setAsDefault` is the sharpest case: clear the old
  default, set the new one, and if the second failed the menu was left with **no
  default price version at all** — pricing returns nothing rather than erroring,
  so the customer sees a blank total.
- ✅ **Bulk operations reported impossible outcomes.** "Success: 12, Failed: 3"
  inside one transaction describes something the database will never produce.
  Both now fail the batch and say so.

### Customer journey
- ✅ **The journey could not be completed.** `onClick={handleSubmit}` on seven
  steps hands React's click event to `isSilent` — an object, therefore truthy —
  so the step advance never ran. Each step saved and left the customer where
  they were.
- ✅ **Nine controls unusable by keyboard** — date picker, time picker, terms
  checkbox, accordions, event cards, ceremony choice, city filter, hall list,
  decor upload. Date, times and terms are all required, so a keyboard user
  could not book at all.
- ✅ **Time picker Confirm did not commit.**
- ✅ **Phone field turned UK numbers Russian.**
- ✅ **Mobile menu rail covered the Save button** — visible, not tappable.
  Found by the mobile project, not by reading code.
- ✅ **Review screen under-reported** — "1 item chosen" for five dishes, and
  never showed a venue.
- ✅ **Errors arrived as toasts** in the corner, 103 call sites, unattached to
  their fields.
- ✅ **Resume recorded the wrong step.** The last selection step wrote
  `numFormState: 4`, sending a returning customer back eight steps.
- ✅ **Calendar steppers had no accessible name** — four identical unnamed
  buttons around the one date the customer must get right.
- ✅ **Two-second blocking overlay per step** — ~24 seconds of imposed waiting
  across the journey, with nothing loading.

### Venue step
- ✅ **A room too small looked identical to one that fits**, and refused on
  click with a toast. The capacity was on screen but nothing tied it to the
  guest count, so the only way to find out was to press and be refused. It is
  now disabled and says *"Too small for 300 guests"* in words beside the
  capacity — not by being dimmed, which is colour alone.
- ✅ **A party larger than every room was an unwinnable screen.** The customer
  could press every hall in every venue, get the same message each time, and
  never learn that the problem was the number rather than the choice. The step
  now says so once, gives the largest size available, and names both ways out.
  Checked across every city, because discovering it city by city is the same
  dead end with more steps.

### File uploads — the most serious finding since the signup endpoint
- ✅ **Arbitrary file write, as root.** The stored path was
  `folder + "/" + UUID + "_" + file.getOriginalFilename()`. That name arrives
  in the multipart body, not the URL, so nothing in Tomcat or Spring inspects
  it — worth being precise about, because the same `../` in a *URL* is rejected
  with a 400 before any controller runs, which made the write path look safer
  than it was. I checked that separately rather than assuming it. Six `../`
  segments cleared the folder, the category and the UUID prefix; a test shows
  the old code landing a file in `/tmp`. The application writes under
  `/root/`, so it runs as root. Anyone who could reach an upload endpoint
  could write anywhere.
- ✅ **Stored cross-site scripting.** `/deimg` is fully public and served a
  content type from `Files.probeContentType`, which reports what a file
  actually is — so an uploaded `.svg` or `.html` came back as
  `image/svg+xml` or `text/html` from the API's own origin. Uploads are now
  restricted to real image formats and PDF at the point of writing, and the
  served type is mapped from the stored extension with `nosniff`.
- ✅ **Hardcoded paths.** The writer used `/root/diamondevent_be/uploads/` and
  returned a URL hardcoded to `https://diamondevents.uk:8081`, so a file
  uploaded on a developer's machine came back with a URL pointing at
  production. The reader carried three copies of the path, two commented out,
  one of them a `C:/Users/hp/Pictures` directory. An `app.upload.dir` property
  already existed and was ignored. Both now read it.

### Data protection
- ✅ **Customer data out of the logs.** Every booking save wrote the full
  request body at INFO — ~8KB, twelve times per booking, carrying the
  customer's name, their contact's phone number, the couple's names and the
  whole menu. Payments logged the transaction reference and a free-text remarks
  field. Customer lookups logged the email three times per request. Measured
  over two complete journeys afterwards: **zero lines contain customer data**,
  longest save line down from ~8,000 characters to 136. Guarded by
  `RequestPayloadLoggingTest`.

### Housekeeping
- ✅ Third-party CDN assets removed from both frontends.
- ✅ Flyway owns the schema; Jasper replaced by Thymeleaf + openhtmltopdf for
  the customer document.
- ✅ Integration tests actually run (`maven-failsafe-plugin` was never bound, so
  the only test that boots the app was never executed by the build).
- ✅ Three unreachable screens deleted (`Caterings2`, `DecorForms`,
  `DecorInputRow`).
- ✅ Two controllers logged under a third controller's name.

### Corrections to my own analysis

Recorded because they are the kind of mistake worth not repeating:

1. I claimed `white/50` empty-state text failed contrast. It measures 5.28:1
   and passes. Only `text-[grey]` (#808080, 3.4:1) was a genuine failure.
2. I diagnosed "a reload loses your place in the journey" and wrote a fix. The
   Redux store *is* persisted to localStorage; reload was never affected. The
   real fault was `numFormState`, on a different path. Fix reverted, correct
   one shipped.

---

## 10. Outstanding work

Ordered by what actually costs the business the most.

### A. Correctness and risk

| # | Item | Status | Why |
|---|---|---|---|
| A1 | Pagination on `event_master` and `customer_master` | ⬜ | These grow forever. Every list endpoint returns every row. First real client with a few thousand bookings meets a wall. Additive; no breaking change. |
| A2 | Optimistic locking on `EventMaster` | ⬜ | Admin and customer can edit the same booking; last writer wins silently. |
| A3 | Database constraint behind date availability | ⬜ | The race is currently caught in application code only. |
| ~~A4~~ | ~~File upload validation~~ | ✅ | Done, and it was worse than the row said — see §9. |
| ~~A5~~ | ~~Guest count bounds~~ | ✅ | My row was wrong: 0 guests **is** validated. The real gap was the upper end, and it was a dead-end screen rather than a missing bound. See §9. |
| A5b | Upper bound on the event date | ⬜ | The year stepper goes forward indefinitely. Low priority — a booking three years out may well be legitimate, so this needs a business answer before a number. |
| ~~A6~~ | ~~Confirm `EventVendorMasterSelection` persists~~ | ✅ | **Investigated: not a bug.** The supplier picker is commented out of the journey — see §5.6. Moved to D5. |
| A7 | Turn off `ddl-auto=update` | ⬜ | Flyway owns the schema; leaving Hibernate able to add columns means orphan entities keep materialising tables. |

### B. Architecture

| # | Item | Status | Why |
|---|---|---|---|
| B1 | Booking-above-Event domain model | ⬜ | The largest item. `EventMaster` is doing the job of both a booking and an event; a customer with a nikkah and a walima on consecutive days has two unrelated bookings. |
| B2 | REST semantics and pagination across the API | ⬜ | ~340 endpoints, all POST. Breaking change across two frontends — needs to be staged. |
| B3 | Delete the five orphan entities | ⬜ | Classes only; leave the tables. |
| B4 | Retire the token-in-URL sign-in path | ⬜ | The single-use code handoff is the real route. Tests use the legacy path and would need moving first. |

### C. Features

| # | Item | Status |
|---|---|---|
| C1 | Admin operations dashboard | ⬜ |
| C2 | Itinerary table | ⬜ |
| C3 | Admin portal accessibility review | ⬜ |
| C4 | Admin portal test suite | ⬜ |
| C5 | API documentation | ⬜ |
| C6 | Error monitoring | ⬜ |

### D. Blocked on a business decision ❓

| # | Question |
|---|---|
| D1 | **Is food delivery (catering-only booking) coming back?** Both entry points are commented out. The code is repaired and ready either way. If it is not returning, `BookCatering`, `CateringDeliveryBooking` and `/cateringDelivery` should all go. |
| D2 | **Does the React marketing site return, or does WordPress stay?** Six page components and the whole `Layout` route are commented out. If WordPress stays, delete them and the Navbar with them. |
| D3 | **Should Apple sign-in be available in development?** Currently Google-only because the one Apple developer account is bound to production. |
| D4 | **Is `menu_component` / `ingredient` a live feature?** Entities, controllers and admin screens exist; no rows anywhere and nothing in the journey uses them. |
| D5 | **Should customers pick external suppliers?** Five are seeded and the admin manages them, but the picker is commented out and the step now shows notes and terms instead. If suppliers are not returning, the step should be renamed for what it does. |

---

## 11. Decisions taken

| Decision | Reasoning |
|---|---|
| Keep `BookCatering`, delete `Caterings2` / `DecorForms` / `DecorInputRow` | The first has a live route and a live backend endpoint — parked, not withdrawn. The others have no route and no importer. |
| Bulk operations fail whole rather than reporting partial success | Inside one transaction partial success is not available. Reporting it described an outcome the database cannot produce, and sent administrators hunting for failures instead of re-running. |
| Rollback marking via a helper, not inline | `TransactionAspectSupport.currentTransactionStatus()` throws where no transaction is running, and these methods are reachable from paths that were never transactional. |
| Request bodies never logged, at any level | A rule that needs no case-by-case judgement about whether this particular payload carries personal data. |
| Leave the `externalSuuppliers` misspelling | Renaming breaks bookmarked URLs for no functional gain. |
| Mobile tests run Chromium, not WebKit | The project tests a phone viewport, not Safari's engine. Safari is explicitly not covered. |
| No test for `BookCatering` | There is no path through the application to drive it. Inventing a route in would test something no customer can do. |
