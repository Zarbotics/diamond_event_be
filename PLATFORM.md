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
| `diamond_event_be` | `feat/platform-rebuild` | Spring Boot 3.4.5 / Java 21 API, PostgreSQL 16. Serves `http://localhost:8080/diamond` |
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

**Base address: `http://localhost:8080/diamond`.** Port and context path are
set in `application.properties` and three things are configured against them —
`VITE_API_BASE_URL` in each frontend, and the authorised redirect URI on the
Google OAuth client (`/diamond/login/oauth2/code/google`). Changing either half
means changing all three.


40 controllers, roughly 340 endpoints.

**Every endpoint is `POST`**, including reads (`POST /customerMaster/getAllData`).

Pagination is partial. `eventMaster/search`, `customerMaster/search` and
`/notifications` page properly, each capping the requested size at 250 so a
client cannot opt out of it. Everything else returns every row — fine for a
catalogue that grows when someone adds a venue, not fine for anything that
grows with trade.

*(An earlier version of this section said only `/notifications` paginated.
That was wrong: `eventMaster/search` already did, with the same cap. Found
while implementing A1.)*

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

> **Open action, not a code change.** The Google client secret was committed to
> `application.properties` and is still in this repository's git history.
> Removing it from the current file does not remove it from the commits that
> carried it, and rewriting history would not help — every existing clone still
> has it. The only thing that closes it is **rotating the secret in the Google
> console**. See RUNNING.md.


| Concern | Status | Note |
|---|---|---|
| Sign-in | ✅ | Needs `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET`; unset, the backend now refuses the sign-in and says so rather than letting Google answer `invalid_client`. Google OAuth2 in development; **master also has Apple**, kept out of dev branches because there is one Apple developer account and it is bound to production. `VITE_APPLE_SIGNIN_ENABLED` gates the button. |
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
| Vite falls back to 5174 because 5173 is busy | ✅ Fallback ports in the dev CORS default; both failure modes documented in the CJ README |
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
| Backend unit | 136 | `mvn test` |
| Backend integration | 135 | `mvn verify` (skips itself without a database) |
| Journey end-to-end | 48 | `npm run test:e2e` — desktop and mobile |
| Admin portal | 57 | `npm test` |
| Admin portal end-to-end | 8 | `npm run test:e2e` — desktop only |

*(Counts as of this pass. The admin portal figure was 19 and could not have
grown: Create React App excludes `node_modules` from Jest transformation
wholesale, so any test rendering a real screen died on `Cannot use import
statement outside a module` before reaching an assertion, and portal components
throw from inside a stylesheet without a styled-components provider. Both are
fixed once, in `customize-cra-config.js` and `utility/testRender.js`.)*

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

### Pagination
- ✅ **The admin's customer table fetched every customer to show ten.** It
  paged and searched in the browser over the whole table. Invisible at fifty
  customers; the kind of thing that stops working all at once rather than
  gradually. `customerMaster/search` now pages server-side, capped at 250 —
  paging a client can opt out of by asking for a million rows is not paging,
  and that is the assertion the test leans on hardest.
- ✅ **A search box, because paging without one is a step backwards.** Ten rows
  at a time and no way to reach someone on page ninety is worse than what was
  there before. The query matches name, code, email and phone; the input is
  debounced and resets to page one, since page five of the previous results
  almost never exists in the new ones and an empty table reads as "no such
  customer" when it means "no such page".

### Consultations
- ✅ **Calendly is gone.** The widget was an iframe from calendly.com pointed at
  two hardcoded personal links, so the consultation lived where this system
  could not see it and the last screen of the journey depended on a third
  party's script loading.
- ✅ **Two bugs found only by the end-to-end tests.** The booking sent an empty
  email, because I read it from the event and `event_master` has no customer
  email column — its only email fields are flags about whether a notification
  was sent. And on a conflict I set the error message then refreshed the list,
  and the refresh clears the error as it starts: the message appeared and
  vanished in the same tick, so pressing a slot somebody had just taken looked
  exactly like pressing a slot and nothing happening.

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
| ~~A1~~ | ~~Pagination on `event_master` and `customer_master`~~ | ✅ | Both done. `event_master` already had it; `customer_master` now does, end to end. See §9. |
| ~~A1b~~ | ~~`eventMaster/getAllDataAdminPortal` returns every event, in full, on every page load~~ | ✅ | **Done, and there were two callers rather than one — but only one of them reachable.** The admin Events *grid* fetches all 296 events — 624 KB measured, 60 fields each with the food, extras, decor and running-order collections nested inside — and then discarded almost all of it with `events.slice()` to show ten. It now reads the page the parent already fetches from the paginated search endpoint for the table view, which also fixes a bug nobody had reported: the status tabs and the search box were silently ignored in card view. **Correction, found later by the admin end-to-end suite:** the List View / Grid View toggle in `EventStats` is commented out — and was before this work — so `EventGrid` cannot currently be reached from the interface at all. The fix to it is therefore protective rather than a live saving: it stops the 624 KB request coming back the moment somebody uncomments that button. The caller that was actually firing in production was the *calendar*. The *calendar* was the second caller. A month view cannot be paginated — one missing event makes it wrong — so that one got a narrower row instead: `eventMaster/calendarEntries`, five fields built in the query. Five integration tests, one asserting the shape of the DTO itself, because the way this regresses is somebody needing one more field and putting the whole event back. |
| ~~A2~~ | ~~Optimistic locking on `EventMaster`~~ | ✅ | **Done, and the first design was wrong in a way worth recording.** `@Version` alone catches only overlapping transactions — the writes have to land milliseconds apart — while the case that actually happens is an administrator and a customer with the same booking open for several minutes. So the version the client fetched travels back with the save. The first attempt refused on staleness alone, and broke the journey: every step saves, so by the second screen the browser's copy is a revision behind through nothing but its own progress, and the customer was told somebody else had edited their booking — by their own previous click. Caught by the end-to-end suite, not by reasoning. The rule is now *who* rather than *how old*: the save is refused only when the last person to save was somebody else. That needed `updated_by` to be written at all — the column is annotated `@LastModifiedBy` but Spring Data auditing is not switched on, so it had sat at 0 on every booking since the beginning. Refusals come back as 409 with words a person can act on. Five integration tests plus a structural one, matching the capacity guard: a fifth update path that forgets the check fails the build. |
| ~~A3~~ | ~~Database constraint behind date availability~~ | ✅ | **Not a constraint — it could not be one.** How many events a day holds is a *count* (two, or three on a Sunday unless the Monday is used), and a constraint cannot count. It is an advisory lock on the day instead, taken inside `canBookEvent` so all six call sites get it. Proved by racing two real transactions: without the lock both customers are told they have the last place and the day ends with three events; with it, exactly one wins. A second test asserts two *different* days still do not contend, because a lock that serialised the whole journey would pass the first test and be far worse. |
| ~~A3b~~ | ~~One save path writes the event date without ever checking capacity~~ | ✅ | **Answered by the business: the capacity is a hard limit and `canBookEvent` must run wherever an event is created.** `saveAndUpdate` now checks, before the create/update branch rather than inside it — both halves write a date, the update branch directly and the create branch through `MapperEventMaster.toEntity`, so a check in one would have left the other exactly as it was. Existing over-capacity days stay editable, which matters for the rows already in production: the count excludes the event being edited, is only incremented when the date actually changes, and the comparison is strictly greater — so saving one of those three without moving it is allowed, while moving a fourth onto that day is not. |
| ~~A4~~ | ~~File upload validation~~ | ✅ | Done, and it was worse than the row said — see §9. |
| ~~A5~~ | ~~Guest count bounds~~ | ✅ | My row was wrong: 0 guests **is** validated. The real gap was the upper end, and it was a dead-end screen rather than a missing bound. See §9. |
| A5b | Upper bound on the event date | 🟡 | **The typo is guarded; the business question is still open.** The year stepper goes forward indefinitely, so 2027 becomes 2207 with one stray keypress — and that booking was accepted, never appeared in any diary, never got chased, and would be found years later by somebody wondering why the earliest booking is in the twenty-third century. Dates more than ten years out are now refused with a message naming the year, in `canBookEvent`, so all six save paths get it. Ten years rejects only the impossible: **how far ahead the business actually takes bookings is still a question for them**, and this deliberately has not pre-empted it. |
| ~~A6~~ | ~~Confirm `EventVendorMasterSelection` persists~~ | ✅ | **Investigated: not a bug.** The supplier picker is commented out of the journey — see §5.6. Moved to D5. |
| A9 | **SSE heartbeat fired every 60s, with comments either side saying 15** | ✅ | Fixed to 15s. The emitters are created with `Long.MAX_VALUE`, so nothing on this side ever closes an idle connection — the ping is the only thing stopping nginx or a load balancer doing it at their 60-second default. A heartbeat *at* the timeout is a race against it, and losing drops the notification stream silently: nothing errors, notifications just stop until the page is reloaded. Found by auditing the other `@Scheduled` work after the lapsed-hold job turned out never to have been wired. |
| ~~A7~~ | ~~Turn off `ddl-auto=update`~~ | ✅ | Now `validate`. It compares the entities against the real schema at startup and refuses to run if they have drifted — turning a class of bug that used to surface as a runtime error on one unlucky screen into a failure to start that nobody can miss. |
| ~~A7b~~ | ~~Capture a V1 baseline so Flyway owns the schema~~ | ✅ | **Done, from a production dump.** `V1__baseline_schema.sql` is production's schema as it actually is, so a database can now be built from nothing by the migrations alone. Verified against a restored dump in all three shapes it has to handle — see §13. |
| ~~A8~~ | ~~The "choose an event" step renders every event a customer has ever had~~ | ✅ | **The screen is fixed.** Newest first, six shown, the rest behind a press, and a search once there are more than six. Ordered by id rather than event date: a booking being worked on this week may be for next summer, and sorting by date buries it behind everything already booked. Four end-to-end tests, one checked by removing the trim and watching it fail. |
| ~~A8b~~ | ~~`eventMaster/getByCustomerId` still returns every event in full~~ | ✅ | **Done — and the hard part turned out to be already gone.** The row said this touched resume because `computeNextStepFromEvent` read eight fields off the full DTO to work out which step to return to. It did, but its call site had already been commented out in favour of the event's own `numFormState`, which the save path writes rather than infers. So the forty lines went, and with them the reason this screen needed the whole event. `eventMaster/getSummariesByCustomerId` sends seven fields per event; the full event is fetched through `getEventById` for the one the customer actually picks. That fetch takes a moment on a phone, so the card says “Opening…” and every card is disabled until it lands — a second press would otherwise race the first through localStorage and the step counter. If it fails the journey stays in `new` rather than half-opening a booking against whatever `serEventMasterData` happened to hold. Seven backend integration tests, three new end-to-end tests. |

### B. Architecture

| # | Item | Status | Why |
|---|---|---|---|
| B1 | Booking-above-Event domain model | 🟡 | **Stages 1–2 landed; three to go — see §15.** The largest item. `EventMaster` is doing the job of both a booking and an event, so a wedding that is a mehndi, a nikkah and a walima is three rows that know nothing about each other: three budgets, no total, a deposit with no row to live on, and three separate cancellations. §15.3 stages it so that every step before the last is reversible by dropping a column, and the step the business actually wants — "add another day to this wedding" — comes fourth rather than first. Stage 1 built the table and backfilled a parent for all 405 events in the development database; stage 2 wired the four create paths, so the invariant stops decaying with every sale. Neither changes what any screen shows — nothing reads a booking yet, and the column can still be dropped. |
| B2 | REST semantics and pagination across the API | 🟡 | **Designed — see §15.4.** ~340 endpoints, all POST including reads. Rewriting them wholesale means changing every call site in two frontends at once with no way to test the halves separately, in exchange for tidiness — a bad trade. Instead the `/booking` endpoints B1 adds are REST from the first line, and the old surface shrinks as screens move across. Pagination is split out and does **not** wait for any of it: it is where the measured harm is, and it needs no coordination. |
| ~~B3~~ | ~~Delete the five orphan entities~~ | ✅ | Gone: `EventQuote`, `EventQuoteLine`, `EventFoodSelection`, `EventServicesMaster`, `EventItineraryResult`. Tables untouched, and all five were empty in the development database and the production dump alike. `EventItineraryResult` is the one worth naming: it looks like groundwork for the itinerary table C2 will build, which is exactly why it goes — a speculative empty class a future feature *might* reuse is what makes somebody grep for how itineraries work and find two answers. If C2 needs a results table it will be written to fit C2. |
| ~~B4~~ | ~~Retire the token-in-URL sign-in path~~ | ✅ | **Done, and the last caller was not the tests.** It was the admin portal's "open the client portal" button, which built `?accessToken=…&refreshToken=…` out of a member of staff's own localStorage — the exact thing the Google redirect was changed to stop doing, and worse, because those are back-office credentials. A URL is not a private place: browser history, server access logs, every proxy in between, and the `Referer` header of the next request the page makes. `POST /auth/handoff` mints the same single-use code the SSO redirect uses, and is deliberately carved out of the otherwise-public `/auth/**` so that a public endpoint cannot mint a signed-in session on request. The journey no longer reads tokens from its address bar at all — an old bookmark carrying them simply does not sign anybody in, which is correct: they have been through a query string. The end-to-end suite moved onto the handoff too, which is what makes it the only route. Four integration tests. |
| B6 | The food menu: one dish is five rows | 🟡 | **Designed from the production data — see §17. Approved 27 August 2026.** 368 selectable rows hold 238 distinct dishes; twenty-one desserts exist five times each, once per serving style, because the model gives an item exactly one parent. Renaming a dish is five edits and the copies have already drifted. Two of nineteen composites have any sections, and the other seventeen have their contents typed into the item's *name* — the model was fine, the screen was not. Four dishes are parented to a dessert and have never been orderable. `enm_price_multiplier_type` defaults to PER_GUEST in silence, on 238 items that never said. `price_version`, `price_entry` and `menu_item_price` are empty, and 94% of the catalogue has no price. §17.3 stages it M1–M5, additive first. |
| ~~B5~~ | ~~Two copies of the capacity rule~~ | ✅ | **Folded into `EventDayCapacity`, and the copies had already drifted.** `countEventsOnDate` excludes the event being edited; the old Sunday and Monday branches then subtracted it a second time when it was moving off the adjacent day. Moving an event from a Monday onto the Sunday before it therefore under-counted that Monday by one, the Sunday's limit went up to three, and the booking was accepted while a Monday event still stood — the one pairing the rule exists to prevent, since the team need the Monday to break down. Proved by reintroducing the subtraction and watching the new test name it. Six new integration tests; one of them records a deliberate change of behaviour — a Monday booking beside a full Sunday can now be edited in place, which the old code refused, and which is the same grandfathering already agreed for over-capacity days. |

### C. Features

| # | Item | Status | Note |
|---|---|---|---|
| ~~C1~~ | ~~Admin operations dashboard~~ | ✅ | **The dashboard is about events now.** Everything below the over-capacity warning came with the template — "Sales Report", "Sales Growth", "Top Selling Products". This business does not sell products, and the number that matters on a Monday morning is not revenue this month but what is booked for Saturday. Those panels are replaced by **Coming up**: the next three weeks grouped by day, with the count on each day so that a third event stands out, today marked, and a line for consultations waiting on us. Built entirely from endpoints that already existed — `calendarEntries` and `consultation/bookings/pending` — because inventing an endpoint per panel is how a dashboard becomes the most expensive screen in an application. Twelve tests, including the one that matters: an unreachable server and a genuinely empty diary render identically unless one of them says so, and only one means the team has nothing on. |
| C2 | Itinerary table | ❓ | **Moved to a business question — see D6.** The row said only "itinerary table", and investigating it found the feature is built and unused: seven tables (`itinerary_item`, `itinerary_item_type`, `itinerary_assignment`, `itinerary_assignment_detail`, `menu_item_itinerary_map`, `event_menu_itinerary`, `event_itinerary_summary`) with **zero rows in every one of them**, three admin screens that manage them, and a Jasper kitchen itinerary report that already renders one per event. Building a table on top of that would be guessing at what is wanted and then maintaining the guess. |
| ~~C3~~ | ~~Admin portal accessibility review~~ | ✅ | **Audited, and the portal was in better shape than a first scan suggested.** A line-by-line grep reported seventeen images with no alt text and two hundred unlabelled inputs; a scan that understands multi-line JSX found *zero* images without alt text, and antd's `Form.Item label` covers almost every control. Two real defects: the three consultation-diary filters were placeheld rather than labelled — and a placeholder vanishes the moment a value is chosen, leaving a screen-reader user hearing "Aisha" with no idea what it filters — and the sidebar toggle's accessible name came from its icon's alt text, "menu", which says nothing about pressing it or which way it goes. Both fixed, with the diary's labels asserted by test. The mobile menu backdrop is now `aria-hidden`: it is a convenience, not the way out, and it was being announced as a control that cannot be operated. **One gap this audit missed, found later by the end-to-end harness: the portal has no headings at all.** antd's `PageHeader` renders its title as a styled `div`, so there is no `h1` on any screen and the pages cannot be navigated by heading structure — which is how somebody using a screen reader moves around a page they already know. The static scan was looking for missing labels rather than missing structure. **Now fixed**: `PageHeader` wraps a string title in an `h1`, with the styling made to inherit so the change is structural and nothing moves on screen. A title that is already an element is left alone — those pass their own markup, and wrapping it would nest headings inside headings. Asserted on two screens rather than one, because a fix to a shared component that only works on the dashboard is a fix to the dashboard; checked by taking the `h1` out again and watching the test fail. |
| C4 | Admin portal test suite | 🟡 | 19 → 92 tests. The blocker was never the tests: Create React App excludes `node_modules` from Jest transformation wholesale, so any test that rendered a real screen died on `Cannot use import statement outside a module` before reaching an assertion, and portal components throw from inside a stylesheet without a styled-components provider. Both fixed once, in `customize-cra-config.js` and `utility/testRender.js`, which is what makes any of the rest possible. Covered so far: the consultation API client, the event progress rule, the Events grid, the over-capacity panel, the "Coming up" dashboard panel, the consultation diary, and error reporting. **The event form is now covered from outside instead.** It is 2,500 lines and assembles its save payload across four hundred of them, so it needs breaking up before it can be unit-tested — and breaking it up without a safety net is how a working save path stops working quietly. So the portal gained an end-to-end suite first: sign in through the form, the dashboard, the over-capacity panel, the events list, the card/table filter bug from A1b, and opening a booking. The order is deliberate — prove the screen works from outside, then change its inside. **The first step of the refactor is now taken:** the payload assembly — four hundred lines in the middle of `onFinish` — is `src/utility/eventPayload.js`, a pure function with 35 tests of its own, and `onFinish` is down to forty-five lines. Every pricing rule the business depends on is now assertable without a browser: what VAT is charged on (decor and extras, nothing else), whether a decor category price replaces its properties or adds to them, what an unpriced food category contributes, how composite dishes are separated from plain ones. Extracting it found a rule that looks like a bug and is not — categories 4, 5 and 6 are quoted as one line under 4, and the lookup that implements it compares a string id against a list of numbers, so it never matches and 5 and 6 contribute nothing, which is the wanted answer. Correcting the types would charge the group three times. That one has a test and a comment naming the trap. The file is 2,939 → 2,573 lines; the components inside it are the next slice. |
| C4b | Three faults the event form's save test turned up | 🔴 | **Found while making the safety net real, and deliberately not fixed inside a refactor.** The E2E test called "a booking can be opened and saved" never pressed Save — it asserted the button had rendered and stopped, so the payload it exists to protect had never once been sent. Making it press revealed why nobody had noticed: **(1)** the seeded booking cannot be saved at all without answering five required fields, so the test had to fill a date, a guest count, a table count and two decor choices before the form would submit — the development seed's event is a bare row, and completing it would make this test three lines instead of thirty; **(2)** the event date input is `readonly`, so it cannot be typed into — a member of staff booking seven years ahead clicks the year arrow eighty-four times, and somebody using a keyboard alone has no route at all. This is the same fault that was fixed in the customer journey and it was missed here because the audit looked at labels rather than at whether a control can be operated; **(3)** a date the API cannot parse is **accepted and silently ignored** — sending `2033-08-01` instead of `01-08-2033` returned success and left the old date standing, which is how a booking gets confirmed for the wrong day with nothing anywhere saying so. A fourth is unconfirmed and worth checking: during one press of Save the backend logged two "Saving Event Master" entries in the same millisecond. If the form does submit twice, the A2 concurrency guard will now refuse the second — and a member of staff will be told somebody else edited their booking when nobody did. |
| ~~C5~~ | ~~API documentation~~ | ✅ | **`API.md`, generated from the controllers and checked by the build.** 333 endpoints across 44 controllers, each with the one thing an OpenAPI document generated from the controllers alone could not tell you: who is allowed to call it. Authorisation lives in `PortalEndpoints`, not in the annotations, so the audience column is derived by matching each path against the allowlists. `ApiInventoryTest` regenerates the file with `-Dapi.docs.write=true` and fails when it drifts — documentation that is generated but never checked is documentation nobody trusts after the second month. Writing it found a real gap: a third of the controllers use the older `@RequestMapping(method = RequestMethod.POST)` form, and the first scan missed every one of them, reporting four live customer-facing endpoints as pointing at nothing. **springdoc-openapi is still the right long-term answer** and should be added when the build can resolve a new dependency again — see §16. |
| ~~C6~~ | ~~Error monitoring~~ | ✅ | **Both halves, with no new dependency.** Server-side: every unhandled exception now logs with an eight-character reference and returns it to the caller, so "something went wrong" becomes a single grep instead of a guess at a timestamp. Client-side: a failure in a browser used to leave a line in a console on somebody's phone and then vanish — the only evidence reaching the business was a customer saying the site did not work, weeks later, on a device nobody can reproduce. Both frontends now report what they catch, including the errors an ErrorBoundary never sees (a rejected save, a failed fetch in an event handler — most of what actually goes wrong). The admin portal had no ErrorBoundary at all, so one thrown error left a blank white page. The endpoint is public, because the failures most worth hearing about are the ones that stop somebody signing in; that is bounded by truncation, control-character stripping so a report cannot forge log lines around itself, and a ceiling per minute. No customer name, email or event detail is sent, and never the query string — that is where the single-use tokens in our own emails live. |

### E. Consultations — replacing Calendly

Specified in §12. Requested 19 August 2026.

| # | Item | Status |
|---|---|---|
| E1 | Domain, availability rules, slot generation, double-booking constraint | ✅ Done — 21 unit + 15 integration tests |
| E2 | Customer books a consultation at the end of the journey | ✅ Calendly removed; 3 tests, desktop and mobile |
| E3 | Admin: hosts, availability, meeting types, pending queue, manual booking | ✅ Done — 25 backend integration tests, 4 screens |
| E3b | Consultation emails, and the page the cancel link opens | ✅ Done — 12 integration + 4 end-to-end tests |
| E4a | `CalendarProvider` port, write-target rule, video link on confirmation | ✅ Done — 8 integration tests against a stand-in provider |
| E4b | The Google and Microsoft adapters, and token encryption | ✅ Written and tested up to the socket; needs credentials to run against the real thing |
| E5 | Admin: connect and disconnect accounts, choose the write target, sync health | ✅ Done — OAuth flow, busy import on a timer, and a fifth admin tab |

### D. Blocked on a business decision ❓

| # | Question |
|---|---|
| D1 | **Is food delivery (catering-only booking) coming back?** Both entry points are commented out. The code is repaired and ready either way. If it is not returning, `BookCatering`, `CateringDeliveryBooking` and `/cateringDelivery` should all go. |
| D2 | **Does the React marketing site return, or does WordPress stay?** Six page components and the whole `Layout` route are commented out. If WordPress stays, delete them and the Navbar with them. |
| D3 | **Should Apple sign-in be available in development?** Currently Google-only because the one Apple developer account is bound to production. |
| D4 | **Is `menu_component` / `ingredient` a live feature?** Entities, controllers and admin screens exist; no rows anywhere and nothing in the journey uses them. |
| ~~D6~~ | ~~Who takes consultations, and when?~~ **Answered:** all of it configurable in the admin portal — hosts, hours, meeting lengths, buffers, notice. Nothing hardcoded; seed data is starting data, not defaults. |
| ~~D7~~ | ~~Google, Microsoft, or both?~~ **Answered:** both, connected per person. Busy read from every connected calendar, consultations written to one nominated calendar. See §12.6. |
| ~~D8~~ | ~~Automatic Meet/Teams link?~~ **Answered:** yes, and configurable — `blnCreateVideoLink` per consultation type. Created on confirmation, not on request. |
| D6 | **Is the itinerary feature live, and what is the "itinerary table" meant to show?** Seven tables, three admin screens and a Jasper kitchen report exist; every table is empty. Either the kitchen has never used it — in which case the question is whether it is wanted at all before anything is added — or it is used somewhere the development database does not see. If a table is wanted, what does it list: prep steps per dish for one event, or totals across a day's events, and who reads it — the kitchen, or the person planning the week? |
| D5 | **Should customers pick external suppliers?** Five are seeded and the admin manages them, but the picker is commented out and the step now shows notes and terms instead. If suppliers are not returning, the step should be renamed for what it does. |

---

---

## 14. The capacity rule, and making it unforgettable

**Status:** done. Was A3/A3b in §10.

### The rule

Two events on an ordinary day. Three on a Sunday, unless the Monday after it is
used, in which case two. A Monday is closed entirely if its Sunday has three. No
same-day booking. The business confirmed it is a **hard limit**, not a default
an administrator may override.

### Two separate failures, found together

**The race.** How many events a day holds is a *count*, and counting rules
cannot be a constraint — there is no row to collide with, only a total to
exceed. So the check is "count what is there, then insert", and between those
two steps a second request does the same thing. Both count one, both see room,
both insert. Fixed with an advisory lock on the day, taken inside
`canBookEvent` so all six call sites get it. Proved by racing two real
transactions and then removing the lock to watch both succeed.

**The unguarded path**, which is the one that had actually bitten.
`saveAndUpdate` wrote the event date and never called `canBookEvent`, where the
other three save paths did. Production carries the result: **three events on
Friday 1 May 2026**, on a day that holds two — created on the 22nd, 23rd and
25th of April, so days apart rather than in one race.

### Why a structural test rather than a note in the review

These save methods are hundreds of lines long, near-identical to one another,
and one of them carries a comment saying that any change made in it must be
copied by hand into its twin. The next one added will be a copy of one of these,
and whether it keeps the check is a matter of which one was copied.

So `EventDateCapacityIsCheckedEverywhereTest` asserts the rule structurally: if
a method writes an event date — directly or through the mapper — it must also
call `canBookEvent`. Adding a sixth save path without the check fails the build
rather than reaching production and being found in the data months later. It was
verified by taking the new check out and watching the test name the method.

### What was deliberately not done

A database trigger would cover every write path, including any future one that
bypasses the service entirely, and was the first instinct. It was rejected for
now on two grounds: it would duplicate a rule with Sunday/Monday coupling into
SQL, where it would drift from the Java; and it would refuse writes to the
existing over-capacity rows unless they were grandfathered or corrected first.
The structural test buys most of the same protection without either problem.

### The existing over-capacity day: decided

**Grandfathered. Nothing in the data was touched, and nothing should be.**

Three events on Friday 1 May 2026, on a day that holds two. The reasoning, in
the order it mattered:

1. **Those are three commitments to three families.** Correcting them is a
   conversation with customers, not a data fix. An engineer who "tidied" that
   row would be cancelling somebody's wedding to make a number look right.
2. **It is already in the past.** Checked against the restored dump: today is
   20 August 2026, so the day has been and gone. There is nothing to staff,
   move or resource. Editing it now would be rewriting the record of what was
   delivered, for no operational benefit at all.
3. **It is the only one.** There are bookings out to August 2029 and not one of
   them breaches the rule. This is a single historical exception, not a pattern.

So the standard shape for introducing a constraint over legacy data applies:
**grandfather what exists, enforce forward, surface the exceptions.**

- *Grandfathered* — and proved, not assumed. Two integration tests build an
  over-capacity day and check the team can still open and save each booking on
  it, while a fourth is refused and nothing can be moved onto it. That works
  because of three details that are easy to break separately: the count excludes
  the event being edited, it is only incremented when the date actually changes,
  and the comparison is strictly greater.
- *Enforced forward* — `canBookEvent` on every path, with a structural test that
  fails the build if a new one forgets.
- *Surfaced* — `/eventMaster/daysOverCapacity` reports upcoming days holding more
  than the rule allows, and it is on the dashboard, above the charts. Verified
  against the restored production dump: it correctly returns nothing today, and
  correctly reports a planted future breach.

  Two decisions in that panel are worth more than the panel. It **says nothing
  when there is nothing to say** — a warning that reports "0 problems" every
  morning is one people stop reading, and then it is worth nothing on the
  morning it matters. And it **says so loudly when it could not check**, because
  a swallowed error renders as "nothing to report", which reads as a clear diary:
  the one thing it must never imply. Both are tested.

Past days are deliberately left out of that report. They are history, and a list
nobody can act on is a list people learn to ignore.

---

## 15. Booking above Event, and the road to a REST API

**Status:** stages 1 and 2 done, 3–5 designed. This is B1 and B2 in §10, written
down in full before any code moved, because both are staged changes across three
repositories and a live database and the staging is most of the work.

### 15.1 What is actually wrong

`EventMaster` is doing two jobs at once.

It is the **event** — a date, a venue, a running order, a guest count, a menu,
a decor scheme. And it is the **booking** — the customer, the contact person,
the budget, the payments, the consultation, the documents, the quote.

For a single-event customer the two coincide and nothing looks wrong. The
problem appears in the case the business actually trades on: a wedding is a
mehndi on the Friday, a nikkah on the Saturday and a walima on the Sunday. That
is one family, one negotiation, one deposit, one conversation — and three
`EventMaster` rows that know nothing about each other.

What follows from that today:

- **The customer re-enters everything three times.** Contact details, the
  couple's names, the venue, the consultation. The journey has no notion of "the
  same wedding, the next day".
- **There is no total.** `EventBudget` hangs off `EventMaster`, so the family
  gets three budgets and nobody can answer "what does the weekend cost".
- **Payments are per event.** A single deposit against a three-event wedding has
  no row to live on. In practice it is recorded against one of them, which makes
  that event's figures wrong and the other two's incomplete.
- **The capacity rule counts events, correctly, but the diary reads as three
  unrelated jobs.** Staffing a weekend means noticing the pattern by eye.
- **Cancelling "the wedding" is three cancellations**, and nothing stops two of
  them going through and the third being missed.

None of this is hypothetical: it is the shape of the data in production.

### 15.2 The target

Two aggregates, with the booking on top.

```
Booking                     one negotiation with one family
├── customer, contact person
├── budget, payments, deposit
├── consultation
├── documents, quote, status
└── Event (1..n)            one thing that happens on one day
    ├── date, venue, hall
    ├── running order, guest count, tables
    ├── menu selections
    ├── decor, extras, services
    └── itinerary
```

Everything that is *negotiated once* moves up. Everything that is *delivered on
a day* stays down. That test settles most of the field-by-field questions, and
where it does not — the couple's names, which belong to the booking but are
printed on each event's documents — the field lives on the booking and the event
reads through.

The capacity rule is unaffected: it counts **events on a day**, which is what it
already counts, and what the kitchen and the venue actually experience.

### 15.3 How to get there without a flag day

The constraint is that production is live, two frontends read this API, and a
booking in progress must not break mid-journey. So the move is additive first,
and nothing is deleted until both frontends have stopped reading the old shape.

**Stage 1 — the table, alongside. ✅ Done, V11.** `booking`, and
`event_master.ser_booking_id` nullable. One booking backfilled per existing
event — deleted ones included, so the invariant has no exceptions to remember;
an exception is what makes a later join quietly drop rows. Verified on the
development database: 405 events, 405 bookings, none parentless, no duplicates,
no mismatched customer or reference.

Nothing reads the column. The mapping on `EventMaster` is a plain id, and
`updatable = false` is the important part rather than a detail: Hibernate writes
every updatable column on every save, so a mapping nothing populates would write
NULL over the backfill on the first save of each event — undoing the migration
one booking at a time, silently, starting with the events people touch most. A
test reproduces that through the path that actually does it, an entity built
from a DTO, and it was checked by making the column writable and watching it
fail. (The first version of that test reloaded the entity and passed either way,
which proved nothing.)

The column stays nullable, and events created between this stage and the next
have no booking. That is expected rather than an oversight: NOT NULL here would
reject every new booking the moment it deployed. This stage is reversible by
dropping a column.

**Stage 2 — every new event gets one too. ✅ Done.** The four create branches in
`ServiceEventMasterImpl` now make a booking and attach the event to it, so the
invariant V11 established — every event has a parent — stays true instead of
decaying with every sale. Without this, stage 3 would have had to begin by
inventing parents for whatever accumulated in between.

Four, not three. The plan for this change accounted for three; writing the
structural test found a fourth — the admin portal's, which builds its entity
through a different mapper and so does not match on the obvious pattern. That is
the third time a rule has been found missing from one of these four
near-identical methods, and `EveryNewEventGetsABookingTest` now guards this one
the way `EventDateCapacityIsCheckedEverywhereTest` guards the capacity rule.

Attaching it needs two mechanisms, because the four paths disagree about when
the event row appears. `saveAndUpdate` builds the whole event and inserts it
once, at the end, so its own insert carries the booking id. The other three
insert a bare row immediately after the capacity check — associations
deliberately nulled — and fill it in afterwards, so by the time there is a
customer and a reference code to make a booking from, the insert that would have
carried the id has already happened. Those get an explicit
`attachToBooking(eventId, bookingId)`: a native statement carrying `AND
ser_booking_id IS NULL`, so it can only fill an empty column and never move an
event from one booking to another. The first version of this change had only the
first mechanism and quietly did nothing at all down three of the four paths
while passing every structural check, which is why a behavioural test now drives
two of them for real.

Still one booking per event. Several events sharing one — the mehndi, the nikkah
and the walima of one wedding — is stage 4, and it needs the journey to ask "is
this another day of a booking you already have?", which nothing does yet.
Guessing at it here by matching on a customer and a nearby date would silently
merge two unrelated bookings for the same family, and there would be no way to
tell afterwards which money belonged to which.

**Deliberate deviation from the plan below.** The original stage 2 also added
`/booking` REST endpoints. They are not here, because nothing calls them yet.
Adding endpoints in anticipation of a caller is the same speculative pattern
that produced the five orphan entity classes deleted in B3, and the argument
against it does not weaken because this time the plan was mine. They arrive in
stage 3, with the screens that read them. §15.4's rule — new endpoints are REST
— is unaffected.

**Known cost, accepted.** These four methods wrap everything in a `try/catch`
that logs at debug and returns `"Failure"`, and `jakarta.transaction.Transactional`
commits on a normal return. So a save that fails after the booking is made
leaves a booking with no events. That is a wasted row rather than wrong data —
nothing points at it, and stage 3 moves money by event — and the real fault is
the swallow-and-commit, which is an item of its own rather than one to take on
inside a migration.

**Stage 3 — move what is negotiated once.** Budget, payments, consultation and
contact details move to hang off `booking`. Each is a separate migration with
its own backfill, and each keeps a read-through on the event so existing screens
keep working. Do these one at a time, in production, a week apart. Payments
first — that is where the wrong figures are today.

**Stage 4 — the journey learns about multiple events.** "Add another day to
this wedding" appears in the customer journey. This is the stage the business
actually asked for, and it is only safe once 1–3 are done.

**Stage 5 — remove the read-throughs**, once neither frontend uses them. This is
the only stage that deletes anything.

The order matters: every stage before 5 can be abandoned without a rollback, and
stage 4 is the one that pays for the rest.

### 15.4 B2: what to do about 340 POST endpoints

Every endpoint is `POST`, including reads. It is not merely unfashionable — it
costs real things: nothing is cacheable, a read cannot be retried safely by any
intermediary, and "which of these 340 changes data" is unanswerable without
reading each one.

Rewriting them is not the move. A big-bang REST migration means changing 340
endpoints and every call site in two frontends at once, with no way to test the
halves separately, in exchange for tidiness. That trade is bad.

**Do it as a by-product of §15.3 instead.** The `/booking` endpoints are new
code with no callers, so they can be REST-shaped from the first line: `GET
/booking/{id}`, `POST /booking`, `PATCH /booking/{id}`, `GET
/booking?customer=…&page=…`. Every stage above adds a few more properly shaped
endpoints, and the old surface shrinks as screens move across.

Two rules make that work rather than producing a third convention:

1. **New endpoints are REST.** No exceptions, including "just this one to match
   its neighbour".
2. **A screen that moves to the new shape stops calling the old one**, and the
   old endpoint is deleted in the same release. Otherwise both live for ever.

What should *not* wait for any of this: **pagination**. It is independent of the
verb, it is where the actual harm is (see A1b and A8b, both measured in hundreds
of kilobytes per page load), and it can be added endpoint by endpoint with no
coordination at all.

### 15.5 What this is not

It is not a rewrite. `EventMaster` keeps its table, its id, its columns and most
of its behaviour throughout; what changes is what hangs off it. Nothing here
requires the customer journey to be rebuilt, and no stage requires both
frontends to ship on the same day.

---

## 16. What the build cannot currently do

**Not a code problem, and worth writing down so the next person does not spend
an afternoon on it.**

Maven cannot resolve this project's dependency tree in a sandboxed environment
without access to `jaspersoft.jfrog.io`. `net.sf.jasperreports:jasperreports`
depends on `com.github.librepdf:openpdf:1.3.30.jaspersoft.3`, a fork published
only to Jaspersoft's own repository. Maven Central answers 404 for that
version, so with the host blocked the build fails at dependency collection —
before compiling anything.

What that costs, concretely: **no new dependency can be added or verified**.
That is why C5 was answered with a generated inventory rather than
springdoc-openapi, and why C6 was built from the exception handler and a
controller rather than an error-reporting SDK. Both decisions are defensible on
their own merits, but neither was a free choice.

The work still gets compiled and tested — the classpath can be assembled from
the previously built application jar plus the local Maven cache, and JUnit
driven through the platform launcher directly. Compilation, test compilation
and test execution are all real; only dependency *resolution* is bypassed.

**To lift this**, either allow `jaspersoft.jfrog.io` through the egress policy,
or mirror that one artifact into an internal repository. It is a single POM and
JAR.

---

## 17. The food menu, and why one dish is five rows

**Status:** designed from the production data, staged below. Approved 27 August
2026 after the analysis in §17.1.

Everything in this section is measured against a restored copy of the live
database — 436 menu items, 893 chosen dishes across 109 events — rather than
inferred from the code. Where a number appears it came from a query.

### 17.1 What is actually wrong

The catalogue is a tree: `menu_item`, with `parent_menu_item_id`, a rigid three
level ladder of roles (CATEGORY → SUBCATEGORY → ITEM), and prices on the leaves.
That shape is not the problem. Five other things are.

**One dish is five rows.** 368 selectable rows hold only **238 distinct
dishes**; 48 names exist more than once and 130 rows are copies. Desserts is the
clearest case: twenty-one dishes — Chocolate Brownie, Cheesecake, Churros,
Tiramisu, Eton Mess, Cake Pops — exist **five times each**, once under Served To
The Table, Trio Dessert, Classic Desserts, Dessert Buffet and Dessert Stand.

That is not carelessness. The model gives an item exactly one parent, so
offering a brownie four ways requires four brownies. The structure left nobody
any alternative.

What it costs: renaming or repricing a dish is five edits, and the copies have
already drifted — `Trifle` is `PER_GUEST` in three places and unset in a fourth,
`Flavoured Mousse` and `Peach Cobbler` likewise. No report can answer "how many
brownies for Saturday", because five ids are five different dishes.

**A composite editor nobody could use.** Sections *are* modelled, and modelled
well: `menu_component` links a composite to its children, with a component role
naming the section and a sequence order. `Reception Displays` uses it exactly as
intended — ten items under "Selections Include", four under "Dips &
Accompaniments". `Waffle Station` has six under "Include".

But **only 2 of the 19 items flagged composite have any components at all**. The
other seventeen are marked composite and empty, and their names say why:

- "Irn-bru, Cola, Water & Diet on request"
- "Strawberry Daquiri, Virgin Mojito, Mixed Fruit Juice"
- "Samosa Chaat, Mini Burgers, Firecracker Chicken Shots"

The section contents were typed into the item's **name**. When a screen is hard
enough to use, people find a free-text field and use that instead. The data
model is not what failed here.

**Three representations of one tree, and they disagree.** `parent_menu_item_id`
is what the code walks. `txt_path` is an `ltree` that nothing maintains and
**14 rows have wrong** — eight raitas and chutneys claim `MI_1007.SUB_023.*`
while their parent says `MI_1006`. `menu_component` is a third edge, and
"Artisan Cheeses" is both a tree child and a component. The `ltree` is
`NOT NULL`, decorative, and misleading.

**Four dishes nobody can order.** Sweet & Sour Soup, Chicken Noodle Soup,
Chicken & Corn Soup and Vegetable Soup are parented to *Gajar Ka Halwa*, a
dessert. The reader stops at the third level, so children of an item are never
fetched. Chosen zero times in 109 events.

**A silent pricing default.** `getMenuWithPrices` multiplies by guest count, and
when `enm_price_multiplier_type` is null it defaults to `PER_GUEST` — 238 of the
selectable items are null, 130 say `PER_GUEST` explicitly. A £2.00 item becomes
£600 at a three hundred guest wedding, and nothing on any screen says which rule
was applied.

**A price list built and never adopted.** `price_version`, `price_entry` and
`menu_item_price` are **completely empty** in production — roughly 1,100 lines
of service code that has never run — while only 21 of 368 items carry a
catalogue price. Staff type prices into the admin form per booking instead.

**Dead weight.** `metadata` is `{}` on all 341 rows that have it.
`num_default_servings_per_guest` is null on all 436. `bln_is_catering_item` is
true on 416 of 436, so it separates almost nothing. `ingredient`,
`menu_item_ingredient`, `menu_item_itinerary_map` and `event_menu_itinerary` are
empty.

### 17.2 The target

Three ideas, and the first is most of the value.

**A dish exists once. It is *offered* in as many places as you like.** The
offering carries the price, the per-guest rule, the position and the selection
limits; the dish carries the name, the description and what it is. 368 rows
become 238 dishes and a list of offerings. Rename once, reprice once, and the
kitchen can count brownies.

This is deliberately *not* arbitrary depth. Category → subcategory → item is how
the business sells and how a customer chooses, and it makes a better screen than
a tree of unknown shape. What changes is that a dish is no longer imprisoned in
one branch of it.

**Composites keep the model they already have, and gain a screen.** Sections
come from component roles with a sequence order and their own minimum and
maximum. The seventeen free-text names migrate into real sections.

**The price rule is stated, never assumed.** Every priced offering declares
`PER_GUEST` or `FLAT`. There is no default, and an offering that has not said is
an error the menu screen shows rather than a multiplication nobody sees.

Selection limits gain a **minimum**. `menu_item` has only a maximum — used on
four subcategories: Served To The Table 2, Trio Dessert 3, Dessert Buffet 8,
Dessert Stand 5 — while `menu_component` already carries both and uses neither.
"Choose between three and five" is a thing the business says out loud, so it
should be a thing the model can hold.

### 17.3 How to get there

Same discipline as §15.3: additive first, nothing deleted until nothing reads
it, and every stage before the last is undone by dropping a table.

**M1 — delete what is provably dead. ✅ Done.** `ingredient` and
`menu_item_ingredient` are gone: two entities, two DTOs, two repositories, two
service interfaces, two implementations, two mappers and two controllers,
fourteen files and eighteen endpoints. Tables left in place. Zero rows in
production, no caller in either frontend — the admin form's "Ingredient" field
is a plain text box bound to `txt_description` and has nothing to do with the
entity. Same reasoning as B3, and the same reason it goes first: it shrinks the
surface everything after has to be read against.

`menu_item_itinerary_map` and `event_menu_itinerary` were on this list and are
**deliberately still here**. They are empty too, but unlike the ingredient
tables they are wired into the itinerary feature — `ControllerItinerary` and
`ServiceEventItinerarySummaryImpl` both depend on them — and that feature is an
open business question (D6), not a menu one. Deleting them would answer D6 by
the back door. They go with whatever D6 decides.

**M2 — the offering, alongside.** A `menu_offering` table: which dish, in which
section, at what price, under which rule, in what position, with what limits.
Backfilled by merging duplicate rows on name — the 130 copies collapse into
offerings of the 238 originals. Nothing reads it yet. Reversible by dropping a
table.

**M3 — the reads move across.** One query over offerings replaces the four
hand-written three-level walks in `ServiceMenuSelectionImpl` (352 lines, N+1 on
every level). The price rule becomes explicit here. The four soups move to a
real subcategory, and the fourteen wrong `txt_path` values are repaired or the
column is dropped — it earns its keep or it goes.

**M4 — the screens.** The reason any of this happened. The menu editor is
rebuilt around offerings: a dish edited in one place, sections as first-class
things rather than a naming convention, limits and the price rule visible where
the price is typed. The seventeen free-text composite names migrate into real
sections as part of it.

**M5 — versioned prices.** `price_version` and `price_entry` become real, so a
booking remembers what a dish cost when it was quoted, and `menu_item.num_price`
goes. Last, because it depends on offerings existing and because the honest
first step is that 94% of the catalogue has no price at all.

### 17.4 What this is not

It is not a rewrite. `menu_item` keeps its table, its ids and its meaning
throughout; what changes is that where a dish appears stops being a property of
the dish. Nothing here requires the customer journey to be rebuilt, and no stage
requires both frontends to ship on the same day.

---

## 13. The schema, and who owns it

**Status:** done, 19 August 2026. Was A7/A7b in §10.

### What was actually wrong

Flyway did not own this schema. There was no `V1` at all: `baseline-version=1`
stamped whatever Hibernate had built as "version 1" and started at `V2`. On a
database built by the migrations alone, **9 tables existed out of the 68 the
entities need** — the other 59, including `event_master`, `customer_master`,
`user_master` and the whole menu and decor trees, came from
`ddl-auto=update` at startup.

The consequence was sharper than the drift, and it is the part worth
remembering. Every statement in `V2` is guarded on its table existing — it has
to be, or an empty database cannot start — and **Flyway runs before Hibernate**.
So on a *fresh* database `V2` found nothing, applied none of its integrity
repairs, and reported nothing. Every new environment came up without the
constraints `V2` was written to add. `V5`'s `ux_event_master_code`, the index
that stops two events sharing a reference, was among them.

### The fix

`V1__baseline_schema.sql`, captured from a production `pg_dump --schema-only`.
`ddl-auto` moved to `validate`.

**From production rather than from the entities**, deliberately. Hibernate can
export what the entities describe and that was the tempting shortcut, but
`ddl-auto=update` never drops and never narrows, so production holds things the
entities no longer mention: `decor_event_extras` and `detail_seq` map to no
entity at all, and there are four views the Java code knows nothing about. An
entity export would have silently omitted every one of them, and the first fresh
environment would have differed from production in ways nobody would notice
until something failed.

### Three shapes of database, all verified against the real dump

| | What happens | Verified |
|---|---|---|
| **Empty** | Runs `V1`, then `V2`–`V9`; ends up matching production | ✅ 71 tables, starts under `validate` |
| **Production** | No Flyway history at all, so `baseline-on-migrate` adopts the schema at version 1 and **skips `V1`** — running it would try to create tables holding live data | ✅ Simulated by restoring the dump into an empty database: baselined at 1, applied 8 migrations, started under `validate` |
| **Made during this branch** | History starts at `2`; Flyway refuses to start — *"Detected resolved migration not applied to database: 1"* | ✅ Reproduced the failure, then fixed it with `FlywayBaselineStamp` |

That third case is why `FlywayBaselineStamp` exists. The alternatives were a
line in a release note plus a hard startup failure for anybody who did not read
it, or `ignore-migration-patterns=*:ignored` — which would have silenced this
and also silenced a genuinely forgotten migration for ever afterwards. That last
one was the option worth refusing. The stamp is tightly scoped, self-disabling,
and deletable once every database has started once.

### What this also proved

Production's schema **already matches the entities**. `validate` passes against
it untouched, which means `ddl-auto=update` had nothing left to do — it was
carrying risk without doing work.

---

## 12. Consultations — replacing Calendly

**Status:** specified, build in progress. Tracked as E1–E5 in §10.

At the end of the booking journey the customer is offered a consultation. That
is currently a Calendly widget (`react-calendly`), pointed at what used to be
two hardcoded personal Calendly links. It is being replaced with a first-party
scheduling system that reads and writes the team's own Google and Microsoft
calendars.

### 12.1 What it has to do

| | |
|---|---|
| **Customer** | Sees real available slots at the end of the journey and books one. Gets a confirmation, a calendar invitation, and a way to cancel or move it. |
| **Team** | Their existing calendar is respected — anything already in it blocks a slot. A booked consultation appears in their own calendar, not only in this system. |
| **Admin** | Connects and disconnects calendar accounts, sets who takes consultations and when, sees what is booked, and adds or reschedules a meeting by hand. |

### 12.2 Design decisions

Taken up front, because they are the ones that are expensive to change later.

**Times are stored in UTC** (`timestamptz`) and rendered in the reader's zone.
The business is in the UK, so hosts see Europe/London and the clocks change
twice a year under them; a customer may be anywhere. Storing local time and a
zone name means every query has to convert before it can compare, and the hour
that repeats each October is genuinely ambiguous. UTC in the database is the
only representation with no ambiguous values in it.

**Double booking is prevented by the database, not by the application.** Two
customers pressing the same slot at the same moment is the defining failure of
a booking system, and a check-then-insert in application code cannot prevent
it — between the check and the insert is exactly where the other booking
lands. PostgreSQL can express the constraint directly:

```sql
EXCLUDE USING gist (
    ser_host_id WITH =,
    tstzrange(dte_starts_at, dte_ends_at) WITH &&
) WHERE (txt_status = 'BOOKED')
```

Needs `btree_gist`, which joins `ltree` in the extensions migration. The
application still checks first, so the customer gets a civil "that slot has
just gone" rather than a constraint violation — but the constraint is what
makes it true.

**One port, two providers.** Google Calendar and Microsoft Graph differ in
their APIs and agree on everything that matters here: list busy periods,
create an event, update it, delete it. A `CalendarProvider` interface with an
adapter each keeps the scheduling logic free of either, and means the domain
can be built and tested before any provider credentials exist.

**Availability is rules, not slots.** Storing generated slots means
regenerating them whenever anything changes and having stale rows the moment a
host adds a meeting. Slots are computed on request from recurring rules minus
exceptions, existing bookings, and imported busy periods.

**Refresh tokens are encrypted at rest.** A token in the database in plaintext
is a standing grant to read and write the team's calendars for anyone who
reaches the database or a backup of it.

### 12.3 Domain

| Entity | What it is |
|---|---|
| `ConsultationHost` | A team member who takes consultations. Linked to `UserMaster`. |
| `CalendarConnection` | One host's connected Google or Microsoft account: provider, account email, selected calendar, encrypted tokens, sync state. |
| `AvailabilityRule` | Recurring weekly availability — day, start, end, per host. |
| `AvailabilityException` | A one-off: a bank holiday closed, a Saturday opened. |
| `ConsultationType` | Duration, the interval start times are offered on, buffer before and after, minimum notice, how far ahead bookings are allowed, where it happens, whether it needs confirming, and whether confirming makes a video link. |
| `ConsultationBooking` | The meeting. Host, customer, optional `EventMaster`, start and end in UTC, status, the external calendar event it created, and a single-use token for the customer's cancel/reschedule link. |
| `ExternalBusyBlock` | Busy periods imported from a connected calendar, so a host's own meetings block slots without this system reading what those meetings are. |

### 12.4 Edge cases

The list is the point of writing this down. Anything unticked is unbuilt.

| Edge case | Handling |
|---|---|
| Two customers book the same slot at once | ✅ Constraint proven against a real database, and two threads racing through the service prove exactly one wins; loser is told the slot has gone and shown fresh ones |
| Host's own calendar gains a meeting after slots were displayed | ✅ Re-checked at the moment of booking, not only when listing |
| Provider unreachable when confirming | ✅ Booking stands, failure recorded as `SYNC_FAILED` on the row; proven by a test |
| Provider unreachable when listing slots | ✅ Last imported busy periods are kept rather than cleared — erring towards the host looking busier, because the other way double-books them |
| Refresh token expired or access revoked | ✅ Marked `NEEDS_RECONNECT`, taken out of the retry sweep and shown on the Connected calendars tab |
| A booking is deleted in Google by the host | ⬜ Not reconciled — the consultation stays booked here. Needs a decision: silently cancelling on the strength of a calendar edit is a strong reading of a delete |
| Host disconnects a calendar with future bookings | 🟡 Allowed, with the consequence stated in the confirmation. Removing the *host* is still refused, which is the case that leaves a customer with nobody |
| Clocks change between listing and the meeting | ✅ Four tests: BST, GMT, the 25-hour day and the 23-hour day |
| Customer is in another timezone | ✅ Slots rendered in their browser's zone, with the zone named on screen |
| Customer books five minutes from now | ✅ Minimum notice on the consultation type |
| Customer books three years out | ✅ Maximum advance on the consultation type |
| Back-to-back meetings with no gap | ✅ Buffers before and after, counted as busy |
| Bank holidays | ✅ Availability exceptions, and a closure beats a weekly rule |
| Customer already has a consultation for this booking | ⬜ Offered the existing one to move, rather than a second |
| A request is left unanswered | ✅ The hold lapses on a five-minute sweep, the slot returns, the customer is told, and confirming late is refused. **The sweep had no caller until E5** — see §12.6 |
| Two customers request the same slot before either is confirmed | ✅ `PENDING` holds the slot under the same constraint as `BOOKED` |
| No slots available at all | ✅ Says so plainly and offers the phone number — the venue-capacity lesson |
| Admin manually books over a customer slot | ✅ Manual booking goes through the same service, so the same exclusion constraint applies; proven by a test |
| Customer cancels | ✅ Single-use link, slot released, both sides emailed. Calendar event removal comes with E4 |
| A mail server is down or has no credentials | ✅ The booking still succeeds — sending is after commit and swallows its own failures. This is the *ordinary* case in development, where there are no SMTP credentials at all |
| An email scanner follows the cancel link | ✅ The link only opens a page; cancelling takes a press. Outlook Safe Links and similar fetch every URL in an email, so a page that acted on load would cancel meetings by itself |
| An OAuth callback arrives with a forged or stale state | ✅ Refused before anything is done — HMAC-signed, ten-minute expiry, constant-time comparison. It is the only thing protecting a necessarily public endpoint |
| The cancel link is used twice, or forwarded | ✅ The token is spent on use; the second attempt says the meeting is already cancelled rather than reading as a fault |
| Customer is in another timezone when the email arrives | ✅ Every time in every email is written in the zone they booked from, and the host's copy carries both clocks |
| Event booking is cancelled after the consultation is set | ⬜ Consultation flagged for the team, not silently cancelled |
| Two hosts, one customer | ✅ Round-robin by least-recently-booked, skipping anyone not actually free; a named host can be requested |

### 12.5 Delivery, and what I cannot verify here

Staged, because Google and Microsoft credentials are needed and I cannot obtain
them — the same constraint as Apple sign-in.

| Stage | | Verifiable here |
|---|---|---|
| **E1** | Domain, availability rules, slot generation, booking with the exclusion constraint | ✅ Fully |
| **E2** | Customer-facing booking at the end of the journey, replacing the Calendly widget | ✅ Fully, end to end |
| **E3** | Admin: hosts, availability, meeting types, request queue, manual booking | ✅ Fully |
| **E4a** | `CalendarProvider` port, write-target rule, video link on confirmation | ✅ Fully, against a stand-in provider |
| **E4b** | The Google and Microsoft adapters, and encryption of the stored tokens | 🟡 Everything on this side of the socket; the round trip needs credentials |
| **E5** | Admin: connect and disconnect, choose the write target, sync health, busy import | 🟡 Everything but the click-through on a real consent screen |

E1–E3 give a working consultation system with no external dependency at all.
E4–E5 make the team's existing calendars part of it. Built in that order so
there is something working before anything depends on a third party.

#### What E3 actually built

Four screens under **Consultations** in the admin portal, ordered by how often
they are used rather than by how the data is shaped:

| Screen | What it is for | Who opens it |
|---|---|---|
| **Diary** | Every consultation, filtered by person, status and date. Paged on the server — it is the only table here that grows without limit. Cancelling from it releases the slot. | Daily |
| **Requests** | Requests waiting on somebody, with how long the hold has left. Confirm or decline, with a reason. | Daily |
| **Who takes them** | Add and remove people, set which zone they work in, and open their working hours. | On a change |
| **Kinds of meeting** | Length, interval, buffers, notice, how far ahead, whether it needs confirming, how long a request is held, and whether confirming makes a video link. | On a change |

Everything the business asked to be configurable is on those last two screens.
Nothing about consultations is a constant in the code any more.

Removing somebody with meetings in the diary is refused rather than cascaded,
and the refusal says how many — customers would otherwise hold an appointment
with nobody and find out on the day. Booking by hand goes through the same
service as a customer booking, so the double-booking constraint applies to it
too; an admin path that bypassed it would have been the one way to double-book.

#### Three bugs E3's tests found

Worth recording because none of them would have shown up in ordinary use until
somebody depended on them.

1. **A one-off opening did nothing for a host with no weekly hours.** The slot
   finder gave up the instant the rules list was empty, so "we are not normally
   available, but we are open this Saturday for the wedding fair" saved
   correctly and produced an empty calendar, with nothing anywhere saying why.
   Every existing test of openings happened to pass a weekly rule as well,
   which is what hid it.

2. **The diary query failed outright on PostgreSQL.** Absent date filters were
   written `:from IS NULL`; a parameter that appears only inside a null check
   has no type to infer, so PostgreSQL answered "could not determine data type
   of parameter $5" and the screen 500'd. The id and status filters get away
   with the same shape because their types come from the columns they are
   compared against. Absent bounds are now widened to ones that exclude nothing.

3. **The interval between offered times was a constant.** `SLOT_STEP_MINUTES =
   30` meant an hour-long meeting was always offered on the hour and the half
   hour. It is a column on the meeting type now. It is deliberately *not* the
   same as the duration: a 60-minute meeting on a 30-minute interval is offered
   at 09:00, 09:30, 10:00 — overlapping candidates, of which booking one
   removes its neighbours. That is why an empty three-hour morning offers five
   hour-long starts and not three, and it is the thing that confuses everybody
   the first time, so the portal spells it out on the field.

#### The emails

The loop the business described — a customer requests, the team confirms, and
the confirmation carries the link — is complete. Five messages:

| What happened | Customer gets | Host gets |
|---|---|---|
| Booked outright | "Your consultation is booked", joining link, cancel link | The booking, with both clocks |
| Requested | "We have your request" — says plainly it is **not a booking yet**, and when the hold runs out | "A request is waiting for you" |
| Confirmed | "Your consultation is confirmed", joining link, cancel link | — |
| Declined | The reason, and an invitation to pick another time | — |
| Cancelled | A receipt if they cancelled; an apology and another time if the team did | Told, either way |

Three decisions worth recording:

1. **Nothing an email does can undo a booking.** Sending happens after the
   transaction commits, through `UtilTransaction.afterCommit`, and every send
   swallows its own failure. The mail server is somebody else's machine, reached
   over the network, and in development it has no credentials at all — so it
   failing is not an edge case, it is what happens on every developer laptop
   every time. A test proves a booking still succeeds when the mail server
   throws.

2. **Every time is written in the customer's own zone**, and labelled with an
   offset rather than an abbreviation. The JDK's short zone names are not
   dependable — on this JVM `Asia/Dubai` formats as "GTS", which nobody uses
   (the real abbreviation is GST), and `America/New_York` formats as
   "GMT-04:00", so the shape is not even consistent. The label exists so the
   reader can catch a mistake, and one they do not recognise cannot do that.
   An offset needs no locale data to be right. That is what the
   `txtCustomerTimeZone` column has been for. An email telling somebody in Dubai
   their consultation is at 10:00, meaning 10:00 in London, is a missed meeting
   and a customer who believes they were stood up. The host's copy carries both,
   because ringing somebody at their midnight is the other half of the same
   mistake.

3. **The cancel link opens a page; it does not cancel.** Mail clients, corporate
   security gateways and link scanners fetch the URLs in an email before a person
   sees them — Outlook's Safe Links does it as a matter of course. A page that
   acted on load would have meetings cancelled by a spam filter, and the customer
   would find out by turning up to nothing. Four end-to-end tests cover this,
   including one that was checked by making the page cancel on load and watching
   three of them fail.

The page lives at `/consultation/manage?token=…` in the customer journey,
deliberately outside `ProtectedRoute`: the customer may not be signed in, may
not have an account, and may be opening it weeks later on a different device.
The unguessable single-use token is what authorises it, and the server spends
it on use.

#### Access, and how it is proved

Everything under `/admin/consultation` is administrator-only because the
security policy is default-deny. `/admin` is **not** a protected prefix in
itself — several menu reads live under `/admin/menu` and are on the customer
allowlist — so what keeps these closed is being absent from that list, not the
path they sit on.

That is asserted twice, deliberately. `PortalEndpointPolicyTest` checks the
rule; `ConsultationAccessIT` drives all sixteen endpoints over HTTP with real
signed tokens and checks the filter chain enforces it: 401 anonymous, 403 for a
signed-in customer, 200 for an administrator. The middle one is the case worth
the test — a customer holds a perfectly valid token, so authentication settles
nothing, and a slip there means reading every other customer's consultations.

### 12.5a Requested, or booked outright

Asked for by the business on 19 August: a customer requests a meeting, the team
confirms it, and the confirmation email carries the link.

**It is a good approach, and it is a setting rather than the only mode.** Both
Calendly and Cal.com have exactly this — "requires confirmation" — and both
default it off. Three reasons the default matters:

- A customer who has just finished a fourteen-step journey is at the point of
  most commitment. "Somebody will confirm this later" is where that goes.
- Somebody has to act. A request made on a Friday evening waits until Monday.
- **The slot question.** While a request is pending, is the slot held? If not,
  the team can confirm a meeting into a time somebody else has since taken. If
  it is, one request nobody answers takes a slot off sale for good.

So: per consultation type, off by default; a pending request **does** hold its
slot — the exclusion constraint covers `PENDING` as well as `BOOKED` — and the
hold **lapses** after a configurable window, which puts the slot back on sale.
Confirming after the hold has lapsed is refused rather than granted, because by
then somebody else may have the time.

`DECLINED` is separate from `CANCELLED`. The team saying no and the customer
pulling out are different events and should not read as the same one in a list.

The video link is created **on confirmation**, which suits both modes: instant
bookings confirm immediately, requested ones when somebody agrees. A link for a
meeting nobody has said yes to is a link to nothing.

| Setting (per consultation type) | Default |
|---|---|
| `blnRequiresConfirmation` | off — book outright |
| `numConfirmationWindowHours` | 48 |
| `blnCreateVideoLink` | on |

#### What E4a built, and the trap it fell into

The port is in and everything above it is finished: which calendar gets written
to, when the write happens, whether a joining link is asked for, and what
happens when the provider fails. Only the two adapters are left, and they are
the part that cannot be written without credentials.

Four rules the tests pin down:

- **A provider that is down cannot cost a customer their booking.** Google being
  unreachable, a revoked token and an expired refresh token all land in the same
  place, and none of them is a reason to tell somebody their meeting did not
  happen. The failure is recorded on the row as `SYNC_FAILED` for a person to
  chase.
- **A host with nothing connected still takes consultations.** That is the state
  every installation is in before anybody connects anything, and a small team may
  stay in it for ever.
- **Read from every connected calendar, write to exactly one.** Enforced by a
  partial unique index rather than by remembering to clear the old target.
- **A request is not put in anybody's diary until it is confirmed.** Otherwise a
  host turns down other work for something they may well decline.

**The trap.** Publishing runs from `afterCommit`, and a `save()` there quietly
does nothing: the EntityManager is still bound to the thread so there is no
error, but its transaction has already finished and the flush never reaches the
database. The symptom is nastier than a failure would be — the provider is
called, the calendar entry really is created, the joining link really is
returned, and the column stays null. So the customer's email goes out without
the link, and a later retry creates a *second* calendar entry because the row
still looks unwritten.

`REQUIRES_NEW` on the two public methods fixes it. It was checked by taking the
annotation off and watching the test fail, because an annotation that turns out
to be decoration is worse than none.

### 12.6 Calendar providers — the approach, and why

Answering "Google, Microsoft, or both?" and "should it sync with both or only
one?". Decided 19 August 2026.

**Both providers, connected per person, not per company.**

A team does not all use the same thing, and the person who joins next may not
use what everyone else does. Every scheduling product of consequence — Calendly,
Cal.com, SavvyCal — connects each individual's own account for that reason. It
also avoids the alternative: a domain-wide installation that can read every
mailbox in the company whether or not its owner agreed. That needs an
administrator's blessing, is a far larger thing to be responsible for, and buys
nothing here.

**Read busy from every connected calendar. Write consultations to exactly one.**

This is the part that is easy to get wrong, and it is the real answer to
"both or only one":

| Direction | Which calendars | Why |
|---|---|---|
| **Read** — when is this person busy? | *All* of them | Someone with work in Outlook and personal life in Google is genuinely unavailable for both. Reading only one produces a system that books meetings over their dentist. |
| **Write** — where does this consultation go? | *One*, nominated per host | Writing to several means the same meeting exists two or three times, and every later edit has to find and match all the copies. |

Put another way: their calendars are the authority on when they are busy; this
system is the authority on consultations. Nothing is authoritative for the same
fact in two places, which is where sync loops and duplicated meetings come from.

**Busy times only, never event contents.** Both providers have an API for
exactly this — Google's `freeBusy.query`, Microsoft's `getSchedule` — which
returns periods and nothing else. Asking for less means the team's private
meetings never enter this database, there is nothing sensitive to leak, and the
permission being requested is one a person can reasonably agree to.

**The narrowest scopes that do the job.**

| | Read availability | Write the consultation |
|---|---|---|
| Google | `calendar.freebusy` | `calendar.app.created` — a calendar this app makes and only it can touch |
| Microsoft | `Calendars.ReadBasic` | `Calendars.ReadWrite`, delegated |

`calendar.app.created` is worth the specific mention: Google added it so a
scheduling app need not ask for access to everything in someone's calendar, and
it keeps this out of the heavier verification review that the broad `calendar`
scope now attracts.

**Push notifications, with polling underneath.** Google's watch channels and
Microsoft's Graph subscriptions both tell us when something changes, which
beats asking every few minutes. Both need a public HTTPS endpoint to call, so
polling stays as the fallback for development and for when a subscription
lapses.

#### E4b: the adapters, and what "needs credentials" actually means

Both adapters are written. What cannot be done here is run them against the
real Google and Microsoft — that needs an account, a consent screen and a
person to click through it. Everything on this side of the socket is tested:
the request that would go out, and the reading of a response shaped like
theirs.

That is not a token gesture, because most of what goes wrong with these
integrations is on this side. Each of the following is a real failure of this
kind and each is now pinned by a test:

- **`conferenceDataVersion=1` missing** — the call succeeds, the event appears,
  and there is simply no Meet link. A silent no-op that reads as Google ignoring
  the request.
- **A time sent to Graph without its zone** — interpreted in the mailbox's own
  zone, moving every consultation by that offset, silently.
- **A rotated Microsoft refresh token discarded** — everything works for a
  fortnight, then every connection fails together when the original expires,
  long after the change that caused it.
- **A Google per-calendar error inside a 200** — a deleted or unshared calendar
  comes back as an `errors` array, not an HTTP failure. Read as an empty busy
  list it means "free all week", and the system starts offering times the host
  is committed to.

**Token encryption.** The schema had said `txt_refresh_token_encrypted` since
V6; nothing made that true until now. It matters more than encryption at rest
usually does: a refresh token is not a password that expires or a session that
ends, it is a standing grant to read and write the whole team's calendars until
somebody revokes it. AES-GCM, a fresh IV per value, and — deliberately — **no
key means refusing to store rather than storing in the clear**. The tempting
fallback turns a missing setting into a silent permanent leak that nothing ever
reports.

**One bug the tests caught.** `@ConditionalOnProperty` was the obvious way to
register an adapter only when configured, and it was wrong: it matches a
property that *exists*, and an empty one exists, because
`application.properties` declares every calendar setting with an empty default
so the application starts without them. Both adapters registered on every
installation — present and failing on use, where the entire design is that an
unconfigured provider is *absent*. It surfaced as two adapters both claiming to
be Google; without that collision it would have reached production as
consultations failing to sync on a system nobody had connected anything to.

#### E5: connecting a calendar, and the bug underneath it

A fifth admin tab, **Connected calendars**: connect Google or Microsoft per
person, disconnect, choose which one consultations are written to, and see
whether syncing is working. Busy times are imported on a ten-minute timer.

**The callback is the security-critical part.** It has to be publicly reachable
— the provider redirects the administrator's *browser* to it, so it arrives as
a plain GET with no bearer token and no way to ask for one. That makes the
signed `state` parameter the only thing standing between it and anyone on the
internet.

Without it the attack is quiet and complete: someone finishes an OAuth flow
against *their own* Google account, then sends an administrator a link to the
callback carrying that code and a chosen `serHostId`. The system connects the
attacker's calendar to a member of the team, and from then on every consultation
booked with that person — name, email, phone, time — is written into a calendar
the attacker controls, automatically. The state is HMAC-signed, carries the host
and provider it was issued for, expires in ten minutes, and is compared in
constant time. Eight unit tests and one over HTTP.

**A bug found on the way.** `releaseLapsedHolds()` was written in E1, tested,
documented — and **nothing ever called it**. So the half of "requested, then
confirmed" that makes the other half safe simply did not happen: every
unanswered request held its slot for ever, which is precisely the failure the
lapse window exists to prevent. It is on a timer now, and the customer is told
when their request lapses rather than being left expecting a meeting.

Worth recording as a category, not an incident. A scheduled job that was never
scheduled produces no error and no log line; the only symptom is slots that
quietly never come back. Nothing in the test suite could have caught it, because
every test called the method directly.

**Two deliberate choices in the import.** A failed sync *keeps* the busy times
it imported before, rather than clearing them — stale busy times block slots the
host probably still cannot take, whereas deleting them offers those times out
the moment a provider has a bad five minutes. Erring towards a host looking
busier than they are costs a missed booking; the other way costs somebody a
double-booked afternoon. And a revoked grant is marked `NEEDS_RECONNECT` rather
than `ERROR`, which takes it out of the retry sweep and puts it on the screen —
otherwise it is retried every ten minutes for ever while the slots stay wrong.

### 12.7 What this needs from the business

Nothing, to start with. E1–E3 are a complete consultation system with no
provider connected at all: hosts, hours, slots, bookings, admin management. A
calendar connection makes it better, and nothing waits on one.

When you want the sync, each provider needs an OAuth app registered once:

| | What to register | Redirect URI |
|---|---|---|
| Google | A project in Google Cloud Console with the Calendar API enabled | `{backend}/login/oauth2/code/google-calendar` |
| Microsoft | An app registration in Entra ID with delegated Graph permissions | `{backend}/login/oauth2/code/microsoft-calendar` |

Either can be done without the other, and either can be added later. Hosts
connect their own accounts from the admin screen; nobody needs to hand over a
password.

**Still open, and worth an answer before E4:**

- Should a consultation carry a video link — Google Meet or Teams — created
  automatically with the meeting? It is a small amount of extra work at the
  time and an awkward retrofit later. Assumed yes unless told otherwise.
- Should the customer receive a calendar invitation they can add to their own
  calendar? Assumed yes; it is an `.ics` attachment and needs no provider.

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
