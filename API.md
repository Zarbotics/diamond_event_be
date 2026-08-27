# API inventory

**Generated.** Do not edit by hand — `ApiInventoryTest` regenerates this file
with `-Dapi.docs.write=true` and fails the build when it drifts from the
controllers.

Base address `http://localhost:8080/diamond`. Paths below are relative to it.

## The shape of this API

| | |
|---|---|
| Endpoints | 328 |
| Controllers | 43 |
| Named like a read (`get…`, `search…`, `is…`) | 155 |

Almost every endpoint is `POST`, including the reads. Nothing is cacheable,
no intermediary can safely retry a read, and which of these change data is
answerable only by reading each one. §15.4 of PLATFORM.md sets out how that
is being unwound — as a by-product of the booking model rather than as a
migration of its own.

## Who can call what

Authorisation is **default-deny**: anything absent from the lists below is
administrator-only. That is the rule `PortalEndpoints` encodes, and it is why
most of this inventory carries no audience marker.

- 🌍 **Public** — no authentication at all.
- 👤 **Signed in** — any customer or member of staff.
- *(unmarked)* — administrator only.

### AuthController

| Verb | Path | Who |
|---|---|---|
| `GET` | `/auth/checkin` | 🌍 |
| `GET` | `/auth/confirm` | 🌍 |
| `POST` | `/auth/exchange` | 🌍 |
| `POST` | `/auth/handoff` | 👤 |
| `POST` | `/auth/login` | 🌍 |
| `POST` | `/auth/logout` | 🌍 |
| `GET` | `/auth/me` | 🌍 |
| `POST` | `/auth/refresh-token` | 🌍 |
| `POST` | `/auth/signup` | 🌍 |
| `GET` | `/auth/status` | 🌍 |

### ControllerCalendarOAuth

| Verb | Path | Who |
|---|---|---|
| `GET` | `/calendar/oauth/callback` | 🌍 |

### ControllerCateringDeliveryBooking

| Verb | Path | Who |
|---|---|---|
| `POST` | `/cateringDelivery/deleteById` |  |
| `POST` | `/cateringDelivery/generateCode` |  |
| `POST` | `/cateringDelivery/getAll` |  |
| `POST` | `/cateringDelivery/getAllAdminPortal` |  |
| `POST` | `/cateringDelivery/getAlreadyBookedDates` |  |
| `POST` | `/cateringDelivery/getByCustomerId` | 👤 |
| `POST` | `/cateringDelivery/getById` |  |
| `POST` | `/cateringDelivery/getByIdCP` |  |
| `POST` | `/cateringDelivery/isDateAlreadyBooked` |  |
| `POST` | `/cateringDelivery/saveOrUpdate` | 👤 |
| `POST` | `/cateringDelivery/saveOrUpdateAdminPortal` |  |
| `POST` | `/cateringDelivery/search` |  |
| `POST` | `/cateringDelivery/searchInCateringAndEventBudget` |  |

### ControllerCateringPayment

| Verb | Path | Who |
|---|---|---|
| `POST` | `/cateringPayment/deleteById` |  |
| `POST` | `/cateringPayment/getByCateringDeliveryBookingId` |  |
| `POST` | `/cateringPayment/saveOrUpdate` |  |
| `POST` | `/cateringPayment/saveOrUpdate/WithDocs` |  |

### ControllerCityMaster

| Verb | Path | Who |
|---|---|---|
| `POST` | `/cityMaster/deleteById` |  |
| `POST` | `/cityMaster/getAllActive` |  |
| `POST` | `/cityMaster/getAllData` |  |
| `POST` | `/cityMaster/getById` |  |
| `POST` | `/cityMaster/saveOrUpdate` |  |

### ControllerClientError

| Verb | Path | Who |
|---|---|---|
| `POST` | `/clientError` | 🌍 |

### ControllerConsultation

| Verb | Path | Who |
|---|---|---|
| `POST` | `/consultation/book` | 👤 |
| `POST` | `/consultation/cancel` | 🌍 |
| `POST` | `/consultation/forEvent` | 👤 |
| `POST` | `/consultation/slots` | 👤 |
| `POST` | `/consultation/types` | 👤 |

### ControllerConsultationAdmin

| Verb | Path | Who |
|---|---|---|
| `POST` | `/admin/consultation/availability` |  |
| `POST` | `/admin/consultation/availability/deleteException` |  |
| `POST` | `/admin/consultation/availability/deleteRule` |  |
| `POST` | `/admin/consultation/availability/saveException` |  |
| `POST` | `/admin/consultation/availability/saveRule` |  |
| `POST` | `/admin/consultation/bookings` |  |
| `POST` | `/admin/consultation/bookings/add` |  |
| `POST` | `/admin/consultation/bookings/cancel` |  |
| `POST` | `/admin/consultation/bookings/confirm` |  |
| `POST` | `/admin/consultation/bookings/decline` |  |
| `POST` | `/admin/consultation/bookings/pending` |  |
| `POST` | `/admin/consultation/calendars` |  |
| `POST` | `/admin/consultation/calendars/connect` |  |
| `POST` | `/admin/consultation/calendars/disconnect` |  |
| `POST` | `/admin/consultation/calendars/writeTo` |  |
| `POST` | `/admin/consultation/hosts` |  |
| `POST` | `/admin/consultation/hosts/delete` |  |
| `POST` | `/admin/consultation/hosts/save` |  |
| `POST` | `/admin/consultation/types` |  |
| `POST` | `/admin/consultation/types/save` |  |

### ControllerCountryMaster

| Verb | Path | Who |
|---|---|---|
| `POST` | `/countryMaster/deleteById` |  |
| `POST` | `/countryMaster/getAllData` |  |
| `POST` | `/countryMaster/getById` |  |
| `POST` | `/countryMaster/saveOrUpdate` |  |

### ControllerCustomerMaster

| Verb | Path | Who |
|---|---|---|
| `POST` | `/customerMaster/deleteById` |  |
| `POST` | `/customerMaster/generateCustomerCode` |  |
| `POST` | `/customerMaster/getAllActiveDropDown` |  |
| `POST` | `/customerMaster/getAllData` |  |
| `POST` | `/customerMaster/getByEmail` | 👤 |
| `POST` | `/customerMaster/getById` |  |
| `POST` | `/customerMaster/getDashboardStats` |  |
| `POST` | `/customerMaster/saveOrUpdate` | 👤 |
| `POST` | `/customerMaster/search` |  |

### ControllerDashboard

| Verb | Path | Who |
|---|---|---|
| `GET` | `/analytics/summary` |  |

### ControllerDecorCategoryMaster

| Verb | Path | Who |
|---|---|---|
| `POST` | `/decorCategoryMaster/deleteById` |  |
| `POST` | `/decorCategoryMaster/generateDecorCategoryMasterCode` |  |
| `POST` | `/decorCategoryMaster/getAllActiveDecorMasterData` | 👤 |
| `POST` | `/decorCategoryMaster/getAllData` |  |
| `POST` | `/decorCategoryMaster/getAllDecorMasterData` |  |
| `POST` | `/decorCategoryMaster/getAllDecorMasterDataWithPrice` |  |
| `POST` | `/decorCategoryMaster/getById` |  |
| `POST` | `/decorCategoryMaster/saveOrUpdate` |  |
| `POST` | `/decorCategoryMaster/saveOrUpdateWithFiles` |  |
| `POST` | `/decorCategoryMaster/searchDecorCategory` |  |

### ControllerDecorCategoryPropertyMaster

| Verb | Path | Who |
|---|---|---|
| `POST` | `/decorCategoryPropertyMaster/deleteById` |  |
| `POST` | `/decorCategoryPropertyMaster/generateDecorCategoryPropertyMasterCode` |  |
| `POST` | `/decorCategoryPropertyMaster/getAllData` |  |
| `POST` | `/decorCategoryPropertyMaster/getById` |  |
| `POST` | `/decorCategoryPropertyMaster/saveOrUpdate` |  |
| `POST` | `/decorCategoryPropertyMaster/saveWithListProperties` |  |

### ControllerDecorCategoryPropertyValue

| Verb | Path | Who |
|---|---|---|
| `POST` | `/decorCategoryPropertyValue/deleteById` |  |
| `POST` | `/decorCategoryPropertyValue/getAllData` |  |
| `POST` | `/decorCategoryPropertyValue/getById` |  |
| `POST` | `/decorCategoryPropertyValue/getByPropertyId` |  |
| `POST` | `/decorCategoryPropertyValue/saveOrUpdate` |  |
| `POST` | `/decorCategoryPropertyValue/saveValuesWithDocuments` |  |
| `POST` | `/decorCategoryPropertyValue/saveWithListValues` |  |

### ControllerDecorCategoryReferenceDocument

| Verb | Path | Who |
|---|---|---|
| `POST` | `/decorCategoryReferenceDocument/deleteById` |  |
| `POST` | `/decorCategoryReferenceDocument/getByCategoryId` |  |
| `POST` | `/decorCategoryReferenceDocument/saveOrUpdate` |  |

### ControllerDecorExtrasMaster

| Verb | Path | Who |
|---|---|---|
| `POST` | `/decorExtras/deleteById` |  |
| `POST` | `/decorExtras/deleteExtrasOptionById` |  |
| `POST` | `/decorExtras/generateExtrasCode` |  |
| `POST` | `/decorExtras/getAllActiveData` |  |
| `POST` | `/decorExtras/getAllActiveExtrasData` | 👤 |
| `POST` | `/decorExtras/getAllActiveServicesData` | 👤 |
| `POST` | `/decorExtras/getAllData` |  |
| `POST` | `/decorExtras/getAllExtrasData` |  |
| `POST` | `/decorExtras/getAllServicesData` |  |
| `POST` | `/decorExtras/saveAndUpdate` |  |
| `POST` | `/decorExtras/saveWithDocs` |  |

### ControllerDecorExtrasOption

| Verb | Path | Who |
|---|---|---|
| `POST` | `/extrasOption/getAllData` |  |
| `POST` | `/extrasOption/saveWithDocs` |  |

### ControllerEventBudget

| Verb | Path | Who |
|---|---|---|
| `POST` | `/eventBudget/deleteById` |  |
| `POST` | `/eventBudget/getAllData` |  |
| `POST` | `/eventBudget/getByEventId` |  |
| `POST` | `/eventBudget/monthlyProfitByEventType` |  |
| `POST` | `/eventBudget/monthlySales` |  |
| `POST` | `/eventBudget/saveOrUpdate` |  |
| `POST` | `/eventBudget/summary` |  |

### ControllerEventItinerarySummary

| Verb | Path | Who |
|---|---|---|
| `POST` | `/eventItinerary/calulate` |  |
| `POST` | `/eventItinerary/getPerMenuItemByEventId` |  |
| `POST` | `/eventItinerary/getSummaryByEventId` |  |

### ControllerEventMaster

| Verb | Path | Who |
|---|---|---|
| `POST` | `/eventMaster/calendarEntries` |  |
| `POST` | `/eventMaster/daysOverCapacity` |  |
| `POST` | `/eventMaster/deleteById` |  |
| `POST` | `/eventMaster/generateEventCode` | 👤 |
| `POST` | `/eventMaster/getAllData` |  |
| `POST` | `/eventMaster/getAllDataAdminPortal` |  |
| `POST` | `/eventMaster/getAllTableView` |  |
| `POST` | `/eventMaster/getAlreadyBookedDates` | 👤 |
| `POST` | `/eventMaster/getByCustomerId` | 👤 |
| `POST` | `/eventMaster/getByEventIdAndCustomerId` | 👤 |
| `POST` | `/eventMaster/getEventById` | 👤 |
| `POST` | `/eventMaster/getEventStats` |  |
| `POST` | `/eventMaster/getSummariesByCustomerId` | 👤 |
| `POST` | `/eventMaster/isDateAlreadyBooked` | 👤 |
| `POST` | `/eventMaster/saveOrUpdate` |  |
| `POST` | `/eventMaster/saveWithDocs` | 👤 |
| `POST` | `/eventMaster/saveWithDocsAdminPortal` |  |
| `POST` | `/eventMaster/saveWithDocsCE` |  |
| `POST` | `/eventMaster/search` |  |
| `POST` | `/eventMaster/searchByBudgetStatus` |  |
| `POST` | `/eventMaster/searchEntity` |  |
| `POST` | `/eventMaster/searchInEntityAndEventBudget` |  |

### ControllerEventPayment

| Verb | Path | Who |
|---|---|---|
| `POST` | `/eventPayment/deleteById` |  |
| `POST` | `/eventPayment/getAllPaymentMethods` |  |
| `POST` | `/eventPayment/getAllPaymentStatuses` |  |
| `POST` | `/eventPayment/getByBudgetId` |  |
| `POST` | `/eventPayment/getByEventMasterId` |  |
| `POST` | `/eventPayment/saveOrUpdate` |  |
| `POST` | `/eventPayment/saveOrUpdate/WithDocs` |  |

### ControllerEventType

| Verb | Path | Who |
|---|---|---|
| `POST` | `/eventType/deleteById` |  |
| `POST` | `/eventType/generateEventCode` |  |
| `POST` | `/eventType/getAllActiveEventTypesWithSubEvents` | 👤 |
| `POST` | `/eventType/getAllActiveSubEventsOnlyCP` | 👤 |
| `POST` | `/eventType/getAllData` |  |
| `POST` | `/eventType/getAllEventsWithSubEvents` |  |
| `POST` | `/eventType/getById` |  |
| `POST` | `/eventType/saveEventType` |  |
| `POST` | `/eventType/saveOrUpdate` |  |

### ControllerFileServe

| Verb | Path | Who |
|---|---|---|
| `GET` | `/deimg/{category}/{filename:.+}` | 🌍 |

### ControllerGeneralDashbord

| Verb | Path | Who |
|---|---|---|
| `POST` | `/dashboardStats/getCustomerAndEventYearReport` |  |

### ControllerItinerary

| Verb | Path | Who |
|---|---|---|
| `POST` | `/admin/itinerary` |  |
| `POST` | `/admin/itinerary/item/generateCode` |  |
| `POST` | `/admin/itinerary/item/getAll` |  |
| `POST` | `/admin/itinerary/item/getAllActive` |  |
| `POST` | `/admin/itinerary/item/getAllActiveByType` |  |
| `POST` | `/admin/itinerary/item/getAllByType` |  |
| `POST` | `/admin/itinerary/item/getById` |  |
| `POST` | `/admin/itinerary/item/save` |  |
| `POST` | `/admin/itinerary/item/update` |  |
| `GET` | `/admin/itinerary/map/by-menu/{menuItemId}` |  |
| `POST` | `/admin/itinerary/map/create` |  |
| `DELETE` | `/admin/itinerary/map/delete/{id}` |  |

### ControllerItineraryAssignment

| Verb | Path | Who |
|---|---|---|
| `POST` | `/itinerary/assignment/activate` |  |
| `POST` | `/itinerary/assignment/approve` |  |
| `POST` | `/itinerary/assignment/autoAssignItineraryItems` |  |
| `POST` | `/itinerary/assignment/bulkAssign` |  |
| `POST` | `/itinerary/assignment/create` |  |
| `POST` | `/itinerary/assignment/deactivate` |  |
| `POST` | `/itinerary/assignment/deleteAssignmentByItineraryAssignmentId` |  |
| `POST` | `/itinerary/assignment/generateCode` |  |
| `POST` | `/itinerary/assignment/getAll` |  |
| `POST` | `/itinerary/assignment/getAllActive` |  |
| `POST` | `/itinerary/assignment/getAllItineraryUnits` |  |
| `POST` | `/itinerary/assignment/getById` |  |
| `POST` | `/itinerary/assignment/getByMenuItemId` |  |
| `POST` | `/itinerary/assignment/getByMenuItemIds` |  |
| `POST` | `/itinerary/assignment/getEntityById` |  |
| `POST` | `/itinerary/assignment/removeAllItemsByAssignmentId` |  |
| `POST` | `/itinerary/assignment/removeItems` |  |
| `POST` | `/itinerary/assignment/update` |  |

### ControllerItineraryItemType

| Verb | Path | Who |
|---|---|---|
| `POST` | `/itinerary/item-type/generateCode` |  |
| `POST` | `/itinerary/item-type/getAll` |  |
| `POST` | `/itinerary/item-type/getAllActive` |  |
| `POST` | `/itinerary/item-type/getById` |  |
| `POST` | `/itinerary/item-type/save` |  |
| `POST` | `/itinerary/item-type/update` |  |

### ControllerMenuAdmin

| Verb | Path | Who |
|---|---|---|
| `POST` | `/admin/menu/getAllRoles` |  |
| `POST` | `/admin/menu/getAllTypes` |  |
| `POST` | `/admin/menu/getBundleItems` |  |
| `POST` | `/admin/menu/getByRole` |  |
| `POST` | `/admin/menu/getByType` |  |
| `POST` | `/admin/menu/getCateringMenu` | 👤 |
| `POST` | `/admin/menu/getCateringMenuWithPricing` | 👤 |
| `POST` | `/admin/menu/getDescendantsByPath` |  |
| `POST` | `/admin/menu/getMenu` | 👤 |
| `POST` | `/admin/menu/getMenuWithPrices` | 👤 |
| `POST` | `/admin/menu/getSelectableChildren` |  |

### ControllerMenuComponent

| Verb | Path | Who |
|---|---|---|
| `POST` | `/menu/component/deleteGroup` |  |
| `POST` | `/menu/component/getAll` |  |
| `POST` | `/menu/component/getAllActive` |  |
| `POST` | `/menu/component/getComponentRoles` |  |
| `POST` | `/menu/component/getCompositeWithComponents` |  |
| `POST` | `/menu/component/getGroupsByParent` |  |
| `POST` | `/menu/component/getUsedRoles` |  |
| `POST` | `/menu/component/saveOrUpdateBulk` |  |
| `POST` | `/menu/component/validateGroup` |  |

### ControllerMenuFoodMaster

| Verb | Path | Who |
|---|---|---|
| `POST` | `/menuFoodMaster/deleteById` |  |
| `POST` | `/menuFoodMaster/generateEventCode` |  |
| `POST` | `/menuFoodMaster/getAllActiveFoodsByType` | 👤 |
| `POST` | `/menuFoodMaster/getAllData` |  |
| `POST` | `/menuFoodMaster/getAllFoodsByType` |  |
| `POST` | `/menuFoodMaster/getById` |  |
| `POST` | `/menuFoodMaster/getFoodByType` |  |
| `POST` | `/menuFoodMaster/saveOrUpdate` |  |

### ControllerMenuItem

| Verb | Path | Who |
|---|---|---|
| `POST` | `/menu/item` |  |
| `POST` | `/menu/item` |  |
| `POST` | `/menu/item` |  |
| `POST` | `/menu/item/deleteById` |  |
| `POST` | `/menu/item/generateCode` |  |
| `POST` | `/menu/item/getAll` |  |
| `POST` | `/menu/item/getAllActive` |  |
| `POST` | `/menu/item/getAllActiveCompositeItems` |  |
| `POST` | `/menu/item/getAllActiveItemsOfOtherSubCategory` |  |
| `POST` | `/menu/item/getAllByRoleId` |  |
| `POST` | `/menu/item/getAllPriceUnitTypes` |  |
| `POST` | `/menu/item/getAllRoles` |  |
| `POST` | `/menu/item/getById` |  |
| `POST` | `/menu/item/getValidParentsByRole` |  |
| `POST` | `/menu/item/getValidParentsByRoleId` |  |
| `POST` | `/menu/item/save` |  |
| `POST` | `/menu/item/searchMenuItem` |  |
| `POST` | `/menu/item/searchMenuItems` |  |
| `POST` | `/menu/item/tree` |  |
| `POST` | `/menu/item/update` |  |

### ControllerMenuItemPrice

| Verb | Path | Who |
|---|---|---|
| `POST` | `/api/menu-item-price/bulkSet` |  |
| `POST` | `/api/menu-item-price/calculateForMenuItem` |  |
| `POST` | `/api/menu-item-price/copy` |  |
| `POST` | `/api/menu-item-price/create` |  |
| `POST` | `/api/menu-item-price/delete` |  |
| `POST` | `/api/menu-item-price/getAll` |  |
| `POST` | `/api/menu-item-price/getAllActive` |  |
| `POST` | `/api/menu-item-price/getApplicablePrice` |  |
| `POST` | `/api/menu-item-price/getById` |  |
| `POST` | `/api/menu-item-price/getByMenuItem` |  |
| `POST` | `/api/menu-item-price/getByMenuItemAndVersion` |  |
| `POST` | `/api/menu-item-price/getByPriceVersion` |  |
| `POST` | `/api/menu-item-price/getDefaultByMenuItem` |  |
| `POST` | `/api/menu-item-price/update` |  |
| `POST` | `/api/menu-item-price/updateStatus` |  |
| `POST` | `/api/menu-item-price/validate` |  |

### ControllerMenuItemRole

| Verb | Path | Who |
|---|---|---|
| `POST` | `/menuItemRole/getAllActiveCompositionRoles` |  |
| `POST` | `/menuItemRole/getAllActiveMenuItemRoles` |  |
| `POST` | `/menuItemRole/getAllActiveRoles` |  |
| `POST` | `/menuItemRole/getAllActiveRolesByParentId` |  |
| `POST` | `/menuItemRole/getAllCompositionRoles` |  |
| `POST` | `/menuItemRole/getAllMenuItemRoles` |  |
| `POST` | `/menuItemRole/getAllRoles` |  |
| `POST` | `/menuItemRole/getAllRolesByParentId` |  |
| `POST` | `/menuItemRole/getById` |  |
| `POST` | `/menuItemRole/saveOrUpdate` |  |

### ControllerMenuOffering

| Verb | Path | Who |
|---|---|---|
| `GET` | `/menu/offerings/dish/{dishId}` |  |
| `GET` | `/menu/offerings/duplicates` |  |
| `GET` | `/menu/offerings/unstated-price-rule` |  |

### ControllerNotificationMaster

| Verb | Path | Who |
|---|---|---|
| `POST` | `/notifications/create` |  |
| `POST` | `/notifications/getAll` |  |
| `POST` | `/notifications/markAsRead` |  |
| `GET` | `/notifications/stream` |  |
| `POST` | `/notifications/unread` |  |
| `GET` | `/notifications/unreadCount` |  |

### ControllerPriceCalculator

| Verb | Path | Who |
|---|---|---|
| `POST` | `/api/price-calculator/calculate` |  |
| `POST` | `/api/price-calculator/quote` |  |

### ControllerPriceEntry

| Verb | Path | Who |
|---|---|---|
| `POST` | `/menu/price-entry/export` |  |
| `POST` | `/menu/price-entry/getEntriesByVersion` |  |
| `POST` | `/menu/price-entry/getFilters` |  |
| `POST` | `/menu/price-entry/getMenuTree` |  |
| `POST` | `/menu/price-entry/getQuantityBreaks` |  |
| `POST` | `/menu/price-entry/preview` |  |
| `POST` | `/menu/price-entry/{versionId}/bulk-update` |  |
| `POST` | `/menu/price-entry/{versionId}/bulkAssign` |  |
| `POST` | `/menu/price-entry/{versionId}/bulkDelete` |  |

### ControllerPriceVersion

| Verb | Path | Who |
|---|---|---|
| `POST` | `/menu/price-version/activate` |  |
| `POST` | `/menu/price-version/create` |  |
| `POST` | `/menu/price-version/deactivate` |  |
| `POST` | `/menu/price-version/delete` |  |
| `POST` | `/menu/price-version/duplicate` |  |
| `POST` | `/menu/price-version/generateCode` |  |
| `POST` | `/menu/price-version/getActiveForDate` |  |
| `POST` | `/menu/price-version/getAll` |  |
| `POST` | `/menu/price-version/getAllActive` |  |
| `POST` | `/menu/price-version/getByCode` |  |
| `POST` | `/menu/price-version/getById` |  |
| `POST` | `/menu/price-version/getDefault` |  |
| `POST` | `/menu/price-version/getStatusList` |  |
| `POST` | `/menu/price-version/setAsDefault` |  |
| `POST` | `/menu/price-version/update` |  |

### ControllerStateMaster

| Verb | Path | Who |
|---|---|---|
| `POST` | `/stateMaster/deleteById` |  |
| `POST` | `/stateMaster/getAllData` |  |
| `POST` | `/stateMaster/getById` |  |
| `POST` | `/stateMaster/saveOrUpdate` |  |

### ControllerUser

| Verb | Path | Who |
|---|---|---|
| `GET` | `/user` |  |

### ControllerVendorMaster

| Verb | Path | Who |
|---|---|---|
| `POST` | `/vendorMaster/deleteById` |  |
| `POST` | `/vendorMaster/generateVendorCode` |  |
| `POST` | `/vendorMaster/getAllActiveData` | 👤 |
| `POST` | `/vendorMaster/getAllData` |  |
| `POST` | `/vendorMaster/getById` |  |
| `POST` | `/vendorMaster/saveOrUpdate` |  |

### ControllerVenueMaster

| Verb | Path | Who |
|---|---|---|
| `POST` | `/venueMaster/deleteById` |  |
| `POST` | `/venueMaster/generateVenueMasterCode` |  |
| `POST` | `/venueMaster/getAll` |  |
| `POST` | `/venueMaster/getAllActiveVenuesGroupedByActiveCities` | 👤 |
| `POST` | `/venueMaster/getAllGroupedByCity` |  |
| `POST` | `/venueMaster/getByCityId` |  |
| `POST` | `/venueMaster/saveOrUpdate` |  |
| `POST` | `/venueMaster/saveVenue` |  |

### HomeController

| Verb | Path | Who |
|---|---|---|
| `GET` | `/` | 🌍 |
| `GET` | `/secured` |  |

