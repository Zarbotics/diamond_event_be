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
| Catalogue → Equipment | **Equipment** | Low | Overlaps Itinerary. See §7.1 — do not rename until that is settled. |
| Settings | **Settings** | High | |
| **Itinerary** (group) | — | **Low** | See §7.1. Nothing renamed. |

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

The three screens under **Itinerary** are the largest terminology problem in
the portal, and renaming them is not the fix. What they are:

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
the schedule. Two unrelated concepts, and the one named after a schedule is
the one that is not.

**Nothing here is renamed yet**, because §7.1 may remove these screens
entirely and renaming a screen twice is worse than leaving it.

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

### 7.1 Equipment and Itinerary appear to be the same idea, built twice 🔴

**What we found.** Both answer "given what was sold, what does this event
need, and how many?"

- **Itinerary** — types, items, and one assignment per dish, counted
  `PER_GUEST, PER_TABLE, PER_ITEM, PER_PORTION, FLAT`.
- **Equipment** — items and rules, counted `PER_GUEST, PER_TABLE,
  PER_STATION, PER_EVENT`.

Equipment's own source says it replaced the other: *"The itinerary model this
replaces had two [vocabularies]... `PER_ITEM`, `PER_PORTION` and `PER_DISH`
are gone."*

**Why it is not settled.** The replacement was never finished.

- **Itinerary is live.** It feeds `ServiceEventItinerarySummaryImpl`, which
  produces the **Kitchen itinerary** report — a thing the kitchen actually
  prints and works from.
- **Equipment produces no report at all.** It feeds a calculation and a panel
  on the booking, and nothing else.

So the old model drives the paperwork and the new model drives the screen.

**What we need to know.** Is Equipment intended to replace Itinerary
completely — in which case the kitchen report has to be moved onto it before
the three Itinerary screens can go — or are they genuinely two things:
Itinerary being what leaves the kitchen *with a dish*, and Equipment being
what the venue puts out *for the room*?

If it is the second, they both stay and both need better names. If it is the
first, three screens and 261 rows of configuration disappear.

**This is why none of the Itinerary screens has been renamed.**

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
