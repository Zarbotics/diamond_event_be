package com.zbs.de.spec;

import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.*;
import com.zbs.de.model.EventMaster;
import com.zbs.de.model.CateringDeliveryBooking;
import com.zbs.de.model.CustomerMaster;
import com.zbs.de.model.EventBudget;
import com.zbs.de.model.EventType;
import com.zbs.de.model.VenueMaster;
import com.zbs.de.model.VendorMaster;
import com.zbs.de.model.dto.DtoCateringDeliveryBookingSearch;
import com.zbs.de.model.dto.DtoEventMasterSearch;
import com.zbs.de.util.UtilDateAndTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class SpecificationsCateringDeliveryBooking {

    private SpecificationsCateringDeliveryBooking() {}

    public static Specification<CateringDeliveryBooking> fromDto(
            DtoCateringDeliveryBookingSearch dto
    ) {
        return (root, query, cb) -> {

            query.distinct(true);
            List<Predicate> predicates = new ArrayList<>();

            // soft delete
            predicates.add(cb.or(
                    cb.isNull(root.get("blnIsDeleted")),
                    cb.equal(root.get("blnIsDeleted"), false)
            ));

            // ids
            if (dto.getSerDeliveryBookingId() != null) {
                predicates.add(cb.equal(
                        root.get("serDeliveryBookingId"),
                        dto.getSerDeliveryBookingId()
                ));
            }

            if (dto.getSerCustId() != null) {
                Join<CateringDeliveryBooking, CustomerMaster> cust =
                        root.join("customerMaster", JoinType.LEFT);
                predicates.add(cb.equal(cust.get("serCustId"), dto.getSerCustId()));
            }

            if (dto.getSerEventTypeId() != null) {
                Join<CateringDeliveryBooking, EventType> et =
                        root.join("eventType", JoinType.LEFT);
                predicates.add(cb.equal(et.get("serEventTypeId"), dto.getSerEventTypeId()));
            }

            // booleans
            if (dto.getBlnBookingStatus() != null) {
                predicates.add(cb.equal(
                        root.get("blnBookingStatus"),
                        dto.getBlnBookingStatus()
                ));
            }

            // delivery date filters
            Date from = parseDate(dto.getDteDeliveryDateFrom());
            Date to   = parseDate(dto.getDteDeliveryDateTo());

            if (from != null && to != null) {
                predicates.add(cb.between(root.get("dteDeliveryDate"), from, to));
            } else if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("dteDeliveryDate"), from
                ));
            } else if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("dteDeliveryDate"), to
                ));
            } else {
                Date exact = parseDate(dto.getDteDeliveryDate());
                if (exact != null) {
                    predicates.add(cb.equal(root.get("dteDeliveryDate"), exact));
                }
            }

            // text filters
            like(predicates, cb, root.get("txtDeliveryBookingCode"), dto.getTxtDeliveryBookingCode());
            like(predicates, cb, root.get("txtDeliveryLocation"), dto.getTxtDeliveryLocation());
            like(predicates, cb, root.get("txtBookingStatus"), dto.getTxtBookingStatus());

            // budget status (via EventBudget)
            if (hasText(dto.getTxtBudgetStatus())) {
                Join<CateringDeliveryBooking, EventBudget> budget =
                        root.join("eventBudget", JoinType.LEFT);
                predicates.add(cb.like(
                        cb.lower(budget.get("txtStatus")),
                        "%" + dto.getTxtBudgetStatus().trim().toLowerCase() + "%"
                ));
            }

            // global search
            if (hasText(dto.getQ())) {
                String q = "%" + dto.getQ().trim().toLowerCase() + "%";

                Join<CateringDeliveryBooking, CustomerMaster> cust =
                        root.join("customerMaster", JoinType.LEFT);
                
                Join<CateringDeliveryBooking, EventType> event =  root.join("eventType", JoinType.LEFT);

                Expression<String> dateStr = cb.function(
                        "TO_CHAR",
                        String.class,
                        root.get("dteDeliveryDate"),
                        cb.literal("YYYY-MM-DD")
                );

                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("txtDeliveryBookingCode")), q),
                        cb.like(cb.lower(root.get("txtDeliveryLocation")), q),
                        cb.like(cb.lower(root.get("txtRemarks")), q),
                        cb.like(cb.lower(root.get("txtBookingStatus")), q),
                        cb.like(cb.lower(cust.get("txtCustName")), q),
                        cb.like(cb.lower(event.get("txtEventTypeName")), q),
                        cb.like(cb.lower(event.get("txtEventTypeCode")), q),
                        cb.like(cb.lower(dateStr), q)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /* helpers */
    private static void like(List<Predicate> ps, CriteriaBuilder cb,
                             Path<String> path, String value) {
        if (hasText(value)) {
            ps.add(cb.like(cb.lower(path),
                    "%" + value.trim().toLowerCase() + "%"));
        }
    }

    private static boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private static Date parseDate(String s) {
        try {
            return s == null ? null :
                    UtilDateAndTime.ddMMyyyyDashedStringToDate(s);
        } catch (Exception e) {
            return null;
        }
    }
}

