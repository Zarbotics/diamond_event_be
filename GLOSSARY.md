# The words this platform uses

A domain glossary for Diamond Events, covering the control panel, the customer
journey and the backend.

It exists because the three do not currently agree with one another, and
because several screen names describe the database rather than the business.
It is the source of truth for terminology, and it is meant to be argued with:
where the business meaning could not be established from the code, the entry
says so and asks, rather than guessing.

**How to read the confidence column**

| | |
|---|---|
| **High** | The meaning is established by the code, the data and the workflow together. Safe to apply. |
| **Medium** | The meaning is clear but the best word is a judgement call. Applied, and easy to change. |
| **Low** | The meaning itself is uncertain. Nothing has been renamed. See §7. |

A note on scope, from the brief: the database and the API keep their names.
`ItineraryAssignment` stays `ItineraryAssignment` in Java and in Postgres.
Only what a person reads changes.

---

## 0. What has been applied

Everything rated High or Medium below is now in the two clients, except where
an entry here says otherwise. The Low entries, and everything in §7, are still
open and still need the business.

| | |
|---|---|
| §1 The bookings list | **Applied.** Reference, Booking, Type, Date. The search also now spans every stage, which the screen had always claimed it did and did not — see below. |
| §2 Navigation | **Applied.** Services and Extras moved out of Décor into the Catalogue; Option values → Option choices; Event types → Types of event; Supplier categories → Supplier types. |
| §2 Dashboard → Statistics → Overview | **Overtaken.** That group had exactly one child, so the group went and Dashboard is a direct link. There is no longer a "Statistics" entry to rename. |
| §2 Calendar → Diary | **Not done, deliberately.** Rated Low, and §7.3 explains why renaming Calendar would make the collision worse rather than better. It needs the business. |
| §3 The Itinerary screens | **Applied.** Retired — see §7.1. |
| §4 Offered / Retired | **Applied** as Available / Withdrawn, across 18 screens and dialogs. |
| §4 Active / Deactivated | **Applied** as Active / Closed, on Customers. |
| §4 "Offered" meaning two things | **Applied.** The flag is "Available to customers"; the interval is "Slot every". |
| §6 The journey's spellings | **Applied.** "Decor" → "Décor" and "Service" → "Services" on the step rail and the two step headings. The review page already had both right. |

### One thing the audit found that was not a naming problem

The bookings screen says "the search looks through all of them", and its empty
state said "The search looks through every status". Neither was true: the stage
was sent with every request, so the search only ever looked inside the tab that
happened to be open. Measured against the running server, a confirmed booking's
reference searched from the Enquiry tab returned 0 results and the same search
from the Confirmed tab returned 1.

This is the case the brief describes as needing the workflow changed rather
than the label: the words were right and the behaviour was wrong. The search
now spans every stage and says so on screen, and the tab applies again as soon
as the search is cleared.

---

## 1. The core entities

| Now shown as | Proposed | Confidence | What it actually is |
|---|---|---|---|
| Booking / Event / Event Master | **Booking** | High | One event the business has been asked to cater. `EventMaster` in the code. Staff say booking; the customer journey never names it at all. |
| Event type / Kind of event | **Type of event** | High | Walima, Mehndi, corporate dinner. The vocabulary a booking is written in. |
| Customer | **Customer** | High | The person who books. Already consistent; keep it, and do not let "client" in. |
| Venue | **Venue** | High | Where it is held. Consistent across all three. |
| Consultation | **Consultation** | High | A meeting between a customer and a member of staff, booked from the journey. |

### The one to watch

The admin portal says **Booking**. The backend says **Event**. The customer
journey says neither — its route is `/booking/...` and its heading is the
customer's own event name.

That split is correct and deliberate: the office manages *bookings*, the
customer is planning *their event*. It becomes a problem only where admin
screens leak the word "Event" at staff — the bookings list still has columns
headed **Event Code**, **Event Name**, **Event Type** and **Event Date** on a
screen titled Bookings. Those four should read **Reference**, **Booking**,
**Type** and **Date**.

---

## 2. Navigation

| Now | Proposed | Confidence | Why |
|---|---|---|---|
| Dashboard → Statistics | **Overview** | Medium | "Statistics" is what the page is made of, not what it answers. |
| Bookings | **Bookings** | High | Correct. |
| Calendar | **Diary** | Low | "Calendar" is fine and understood. Raised only because the consultations area already has a tab called Diary, which is a different thing. See §7.3. |
| Consultations | **Consultations** | High | Correct. |
| Customers | **Customers** | High | Correct. |
| Catering deliveries | **Catering deliveries** | High | Correct — a delivery without an event attached. |
| **Décor** (group) | **Décor** | High | Correct for its first three children, wrong for the last two. See below. |
| Décor → Categories | **Décor categories** | High | |
| Décor → Options | **Décor options** | Medium | `DecorProperty` in the code. What varies within a category — colour, height, style. |
| Décor → Option values | **Option choices** | Medium | The actual answers a customer picks. "Value" is database language. |
| Décor → Services | **Services** — *moved out of Décor* | High | Its own description says "alongside the décor". It is not décor. |
| Décor → Extras | **Extras** — *moved out of Décor* | High | Its own description says "on top of the décor". Same. |
| Catalogue → Menu | **Menu** | High | |
| Catalogue → Venues | **Venues** | High | |
| Catalogue → Event types | **Types of event** | High | |
| Catalogue → Supplier categories | **Supplier types** | High | A photographer, a DJ, a videographer. "Category" adds nothing. |
| Catalogue → Equipment | **Equipment** | High | Settled: it is the model that replaced Itinerary. See §7.1. |
| Settings | **Settings** | High | |
| ~~Itinerary (group)~~ | *removed* | High | Retired. Equipment replaced it. See §7.1. |

### Proposed shape

Services and Extras leave Décor and join the Catalogue, which is where
everything else the business sells already lives:

```
Décor          Categories · Options · Option choices
Catalogue      Menu · Services · Extras · Venues · Types of event ·
               Supplier types · Equipment
```

---

## 3. The Itinerary screens

These three screens are gone — see §7.1 for why and for what replaced them.
The section is kept because the words are still in the database and in the
API, and anybody reading those needs to know what they meant.

| Screen | What the code says it is |
|---|---|
| Itinerary Type | "The kinds of thing that can go in a running order" — a code and a name. |
| Itinerary Item | "The things that can go in a running order", each with a type. |
| Itinerary Assignment | "What goes with a dish, and how much of it" — one per dish, counted per guest, per table, per portion or flat. |

The word "itinerary" says *schedule*. None of these is a schedule. The
backend is blunter than the UI: `ServiceEventItinerarySummaryImpl` calls the
output **"the kitchen's prep list"**, and the booking's own report button says
**Kitchen itinerary**.

So an "itinerary" here is what the kitchen has to send out with the food.

Meanwhile the booking has a section genuinely called **Running order** — the
sixteen moments of the evening, guest arrival through end of night. That is
the schedule. Two unrelated concepts, and the one named after a schedule was
the one that was not. The running order is unaffected and keeps its name.

---

## 4. Statuses

| Now | Proposed | Confidence | Where, and what it means |
|---|---|---|---|
| Enquiry | **Enquiry** | High | Booking stage. Somebody has asked. |
| Quoted | **Quoted** | High | Booking stage. A price has gone out. |
| Confirmed | **Confirmed** | High | Booking stage. They have said yes. |
| Offered / Retired | **Available** / **Withdrawn** | Medium | Catalogue items. "Offered" is good but collides — see below. |
| Active / Deactivated | **Active** / **Closed** | Medium | Customers. "Deactivated" is developer language for an account nobody uses any more. |
| Required | **Required** | High | A décor option the customer must answer. |

### "Offered" means two things

It labels `blnIsActive` — *available to customers* — on Equipment, Itinerary
items, Itinerary assignments and consultation types. It also labels
`numSlotIntervalMinutes` on consultation types, as **"Offered every"**, where
it means *how often a slot appears*.

Both are on the same screen. The interval field already carries a good
explanation; the label is what collides. Proposed: **"Available to customers"**
for the flag, **"Slot every"** for the interval.

---

## 5. Field labels

Applied where the meaning was established.

| Now | Proposed | Confidence | Why |
|---|---|---|---|
| Order | **Position** | High | `numDisplayOrder`. "Order" on a booking screen reads as a customer order. |
| Offered | **Available to customers** | High | Says who is affected. |
| Description | **What customers see** | Medium | Only where the text is genuinely customer-facing — verified per field, not applied blanket. |
| How many | **Counted** | Medium | The per-guest / per-table multiplier. "How many" asks for a number; this is a rule. |
| Goes with | **Dish** | High | On itinerary assignments, the row is named after the dish it belongs to. |
| Its code / Reference | **Reference** | High | One word for the same idea everywhere. |
| Name | **Name** | High | |
| Notes | **Notes** | High | |

---

## 6. Customer language against staff language

The two audiences should not read the same words, and mostly do not. Where
they differ by accident rather than by intent:

| Concept | Customer journey | Control panel | Verdict |
|---|---|---|---|
| Décor | "Decor" | "Décor" | **Fix the journey.** UK English takes the accent. |
| Services | "Service" | "Services" | **Fix the journey.** Plural, as everywhere else. |
| Suppliers the customer arranges | "Suppliers" | "Their own suppliers" | Both correct for their audience. Keep. |
| The catalogue of those | — | "Supplier categories" | → **Supplier types**. |
| Food | "Food" | "Food" / "Menu" | Keep both: the journey chooses food, the office maintains a menu. |
| The event itself | (unnamed) | "Booking" | Correct and deliberate. |

---

## 7. Questions the code cannot answer

These need a decision from the business. Nothing has been changed for any of
them.

### 7.1 Equipment and Itinerary were the same idea, built twice ✅ DECIDED

**They are the same concept. Equipment is the one that works. The three
Itinerary screens have been retired from the control panel.**

Both answer "given the dishes sold and the numbers coming, what does this
event need and how many?" Equipment is a strict superset: it can express
everything the itinerary rules could, plus rules that apply to the whole event
rather than to a dish, stations, spares, and a line that says what asked for
it.

#### The evidence

Four independent findings, any one of which would settle it:

1. **Nothing called the calculation.** `/eventItinerary/calulate` is
   referenced by neither client — zero occurrences in the control panel, zero
   in the customer journey. Nor are its two read endpoints.

2. **The Kitchen itinerary report does not use it.** This was the reason it
   looked live. It does not: `master_kitchen_itinerary.jrxml` and its
   sub-reports read `event_master`, `customer_master`, `event_type`,
   `venue_master`, `event_running_order` and the three
   `event_menu_*_selection` tables. `event_itinerary_summary` and
   `event_menu_itinerary` appear nowhere in it. "Kitchen itinerary" there
   means the kitchen's *schedule and menu* — an itinerary in the ordinary
   sense — not this model.

3. **The calculation was one-fifth implemented.** `calculateQuantity` handles
   `PER_GUEST`. `PER_TABLE`, `PER_ITEM`, `PER_PORTION` and `FLAT` all fall
   through to returning the raw multiplier, so a rule marked "per table" was
   never multiplied by the table count.

4. **The database rejects a value the screen offers.** The check constraint on
   `itinerary_assignment_detail.enm_multiplier_type` permits `PER_GUEST`,
   `PER_ITEM`, `PER_PORTION` and `FLAT` only. The Java enum has `PER_TABLE`
   and the form offered it as "Per table — multiplied by the table count".
   Choosing it failed the save with a constraint violation. Nobody had ever
   hit that, which says as much as anything else about how used this was.

`numItineraryPrice` is the same story: carried between DTO and entity, never
computed, never priced.

#### What was done

The three screens, their routes and their service calls are gone from the
control panel. **The tables, the entities and the API are untouched** — no
configured data is destroyed, and restoring the routes would bring the screens
back exactly as they were.

`db/migration-aids/itinerary-to-equipment.sql` carries the configuration
across: an itinerary item becomes an equipment item, and each assignment
detail becomes an equipment rule against the same dish. It is idempotent, it
is not part of the Flyway chain, and it reports what came over. Proven on a
seeded case: two items and two rules migrated, with the ambiguous one flagged.

`PER_GUEST` and `FLAT` map exactly. `PER_ITEM` and `PER_PORTION` have no
equipment equivalent and are recorded as `PER_EVENT` — which is what the old
calculation did with them — each carrying a note saying so, so the business
can correct the ones that should be per guest or per station.

### 7.2 What is a "station"? 🟡

`PER_STATION` is in the Equipment vocabulary and described as "tongs and
boards on a grazing bar". The Itinerary vocabulary has no equivalent. If
stations are a real operational unit the business counts by, the word should
appear in the UI, which it currently does not anywhere.

### 7.3 "Diary" means two things 🟡

The consultations area has a tab called **Diary**, meaning a member of staff's
own availability. The main navigation has **Calendar**, meaning every booking.
Neither is wrong, but a new coordinator would not know which to open to answer
"what is on next Tuesday". Renaming Calendar to Diary would make it worse, not
better; the question is whether the consultations tab should be **Availability**.

### 7.4 "Catalogue" 🟢

It is a reasonable word for "the things we sell and the things we sell them
with", and no better one presented itself. Raised only so it is a decision
rather than an accident.

---

## 8. What is not being changed

Per the brief: Java classes, database tables, API properties and DTO field
names stay as they are. `serEventMasterId`, `txtEventMasterName`,
`blnIsActive`, `EnmItineraryUnitType` and the rest are internal, and renaming
them buys nothing a user would see while risking a great deal they would.

Presentation-layer terminology is where this glossary applies.
