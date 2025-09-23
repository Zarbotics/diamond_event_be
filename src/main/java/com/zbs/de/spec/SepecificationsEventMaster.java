package com.zbs.de.spec;

import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.*;
import com.zbs.de.model.EventMaster;
import com.zbs.de.model.CustomerMaster;
import com.zbs.de.model.EventBudget;
import com.zbs.de.model.EventType;
import com.zbs.de.model.VenueMaster;
import com.zbs.de.model.VendorMaster;
import com.zbs.de.model.dto.DtoEventMasterSearch;
import com.zbs.de.util.UtilDateAndTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class SepecificationsEventMaster {

	private SepecificationsEventMaster() {
	}

	public static Specification<EventMaster> fromDto(DtoEventMasterSearch dto) {
		return (root, query, cb) -> {
			query.distinct(true); // safe when using joins/fetches

			List<Predicate> predicates = new ArrayList<>();

			// soft-delete guard
			predicates.add(cb.or(cb.isNull(root.get("blnIsDeleted")), cb.equal(root.get("blnIsDeleted"), false)));

			// simple equals filters
			if (dto.getSerEventMasterId() != null) {
				predicates.add(cb.equal(root.get("serEventMasterId"), dto.getSerEventMasterId()));
			}
			if (dto.getSerCustId() != null) {
				Join<EventMaster, CustomerMaster> cj = root.join("customerMaster", JoinType.LEFT);
				predicates.add(cb.equal(cj.get("serCustId"), dto.getSerCustId()));
			}
			if (dto.getSerEventTypeId() != null) {
				Join<EventMaster, EventType> tj = root.join("eventType", JoinType.LEFT);
				predicates.add(cb.equal(tj.get("serEventTypeId"), dto.getSerEventTypeId()));
			}
			if (dto.getSerVenueMasterId() != null) {
				Join<EventMaster, VenueMaster> vj = root.join("venueMaster", JoinType.LEFT);
				predicates.add(cb.equal(vj.get("serVenueMasterId"), dto.getSerVenueMasterId()));
			}
			if (dto.getSerVendorId() != null) {
				Join<EventMaster, VendorMaster> vend = root.join("vendorMaster", JoinType.LEFT);
				predicates.add(cb.equal(vend.get("serVendorId"), dto.getSerVendorId()));
			}

			// boolean filters
			if (dto.getBlnIsCouple() != null) {
				predicates.add(cb.equal(root.get("blnIsCouple"), dto.getBlnIsCouple()));
			}
			if (dto.getBlnIsActive() != null) {
				predicates.add(cb.equal(root.get("blnIsActive"), dto.getBlnIsActive()));
			}

			// numeric ranges: guests
			if (dto.getNumNumberOfGuests() != null) {
				predicates.add(cb.equal(root.get("numNumberOfGuests"), dto.getNumNumberOfGuests()));
			} else {
				if (dto.getMinNumberOfGuests() != null) {
					predicates.add(cb.greaterThanOrEqualTo(root.get("numNumberOfGuests"), dto.getMinNumberOfGuests()));
				}
				if (dto.getMaxNumberOfGuests() != null) {
					predicates.add(cb.lessThanOrEqualTo(root.get("numNumberOfGuests"), dto.getMaxNumberOfGuests()));
				}
			}

			// date filters - use your util that converts dd-MM-yyyy dashed strings
			Date dFrom = parseDate(dto.getDteEventDateFrom());
			Date dTo = parseDate(dto.getDteEventDateTo());
			if (dFrom != null && dTo != null) {
				predicates.add(cb.between(root.get("dteEventDate"), dFrom, dTo));
			} else if (dFrom != null) {
				predicates.add(cb.greaterThanOrEqualTo(root.get("dteEventDate"), dFrom));
			} else if (dTo != null) {
				predicates.add(cb.lessThanOrEqualTo(root.get("dteEventDate"), dTo));
			} else {
				Date exact = parseDate(dto.getDteEventDate());
				if (exact != null) {
					predicates.add(cb.equal(root.get("dteEventDate"), exact));
				}
			}

			// text contains (case-insensitive)
			if (hasText(dto.getTxtEventMasterCode())) {
				predicates.add(cb.like(cb.lower(root.get("txtEventMasterCode")),
						"%" + dto.getTxtEventMasterCode().trim().toLowerCase() + "%"));
			}
			if (hasText(dto.getTxtEventMasterName())) {
				predicates.add(cb.like(cb.lower(root.get("txtEventMasterName")),
						"%" + dto.getTxtEventMasterName().trim().toLowerCase() + "%"));
			}
			if (hasText(dto.getTxtBrideName())) {
				predicates.add(cb.like(cb.lower(root.get("txtBrideName")),
						"%" + dto.getTxtBrideName().trim().toLowerCase() + "%"));
			}
			if (hasText(dto.getTxtGroomName())) {
				predicates.add(cb.like(cb.lower(root.get("txtGroomName")),
						"%" + dto.getTxtGroomName().trim().toLowerCase() + "%"));
			}
			if (hasText(dto.getTxtEventStatus())) {
				predicates.add(cb.like(cb.lower(root.get("txtEventStatus")),
						"%" + dto.getTxtEventStatus().trim().toLowerCase() + "%"));
			}

			// joined-entity text fields (customer, eventType, venue, vendor)
			if (hasText(dto.getTxtCustName())) {
				Join<EventMaster, CustomerMaster> cj = root.join("customerMaster", JoinType.LEFT);
				predicates.add(cb.like(cb.lower(cj.get("txtCustName")),
						"%" + dto.getTxtCustName().trim().toLowerCase() + "%"));
			}
			if (hasText(dto.getTxtEventTypeName())) {
				Join<EventMaster, EventType> tj = root.join("eventType", JoinType.LEFT);
				predicates.add(cb.like(cb.lower(tj.get("txtEventTypeName")),
						"%" + dto.getTxtEventTypeName().trim().toLowerCase() + "%"));
			}
			if (hasText(dto.getTxtVenueName())) {
				Join<EventMaster, VenueMaster> vj = root.join("venueMaster", JoinType.LEFT);
				predicates.add(cb.like(cb.lower(vj.get("txtVenueName")),
						"%" + dto.getTxtVenueName().trim().toLowerCase() + "%"));
			}
			if (hasText(dto.getTxtVendorName())) {
				Join<EventMaster, VendorMaster> vend = root.join("vendorMaster", JoinType.LEFT);
				predicates.add(cb.like(cb.lower(vend.get("txtVendorName")),
						"%" + dto.getTxtVendorName().trim().toLowerCase() + "%"));
			}
			if (hasText(dto.getTxtBudgetStatus())) {
				Join<EventMaster, EventBudget> budget = root.join("eventBudget", JoinType.LEFT);
				predicates.add(cb.like(cb.lower(budget.get("txtStatus")),
						"%" + dto.getTxtBudgetStatus().trim().toLowerCase() + "%"));
			}

			// global q across a few important columns (if provided)
			if (hasText(dto.getQ())) {
				String q = "%" + dto.getQ().trim().toLowerCase() + "%";
				Predicate p1 = cb.like(cb.lower(root.get("txtEventMasterName")), q);
				Predicate p2 = cb.like(cb.lower(root.get("txtEventMasterCode")), q);
				Predicate p3 = cb.like(cb.lower(root.get("txtEventRemarks")), q);
				predicates.add(cb.or(p1, p2, p3));
			}

			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}

	private static boolean hasText(String s) {
		return s != null && !s.trim().isEmpty();
	}

	private static Date parseDate(String s) {
		if (s == null)
			return null;
		try {
			// your codebase uses: UtilDateAndTime.ddMMyyyyDashedStringToDate(...)
			return UtilDateAndTime.ddMMyyyyDashedStringToDate(s);
		} catch (Exception ex) {
			return null;
		}
	}

	public static Specification<EventMaster> hasBudgetStatus(String status) {
		return (root, query, cb) -> {
			if (status == null || status.trim().isEmpty()) {
				return cb.conjunction();
			}
			Join<EventMaster, EventBudget> budget = root.join("eventBudget", JoinType.LEFT);
			return cb.like(cb.lower(budget.get("txtStatus")), "%" + status.trim().toLowerCase() + "%");
		};
	}
}
