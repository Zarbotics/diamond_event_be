package com.zbs.de.spec;

import org.springframework.data.jpa.domain.Specification;

import com.zbs.de.model.DecorCategoryMaster;
import com.zbs.de.model.DecorCategoryPropertyMaster;
import com.zbs.de.model.DecorCategoryPropertyValue;
import com.zbs.de.model.dto.DtoDecorCategorySearch;

import jakarta.persistence.criteria.*;

import java.util.ArrayList;
import java.util.List;

public final class SpecificationsDecorCategoryMaster {

	private SpecificationsDecorCategoryMaster() {
	}

	public static Specification<DecorCategoryMaster> fromDto(DtoDecorCategorySearch dto) {

		return (root, query, cb) -> {

			query.distinct(true);

			List<Predicate> predicates = new ArrayList<>();

			// soft delete guard
			predicates.add(cb.or(cb.isNull(root.get("blnIsDeleted")), cb.equal(root.get("blnIsDeleted"), false)));

			// ID
			if (dto.getSerDecorCategoryId() != null) {
				predicates.add(cb.equal(root.get("serDecorCategoryId"), dto.getSerDecorCategoryId()));
			}

			// Category name
			if (hasText(dto.getTxtDecorCategoryName())) {
				predicates.add(cb.like(cb.lower(root.get("txtDecorCategoryName")),
						"%" + dto.getTxtDecorCategoryName().toLowerCase() + "%"));
			}

			// Category code
			if (hasText(dto.getTxtDecorCategoryCode())) {
				predicates.add(cb.like(cb.lower(root.get("txtDecorCategoryCode")),
						"%" + dto.getTxtDecorCategoryCode().toLowerCase() + "%"));
			}

			// Active
			if (dto.getBlnIsActive() != null) {
				predicates.add(cb.equal(root.get("blnIsActive"), dto.getBlnIsActive()));
			}

			// join property
			Join<DecorCategoryMaster, DecorCategoryPropertyMaster> propertyJoin = root.join("categoryProperties",
					JoinType.LEFT);

			if (hasText(dto.getTxtPropertyName())) {
				predicates.add(cb.like(cb.lower(propertyJoin.get("txtPropertyName")),
						"%" + dto.getTxtPropertyName().toLowerCase() + "%"));
			}

			if (hasText(dto.getTxtInputType())) {
				predicates.add(cb.equal(propertyJoin.get("txtInputType"), dto.getTxtInputType()));
			}

			// join property values
			Join<DecorCategoryPropertyMaster, DecorCategoryPropertyValue> valueJoin = propertyJoin
					.join("propertyValues", JoinType.LEFT);

			if (hasText(dto.getTxtPropertyValue())) {
				predicates.add(cb.like(cb.lower(valueJoin.get("txtPropertyValue")),
						"%" + dto.getTxtPropertyValue().toLowerCase() + "%"));
			}

			// global search
			if (hasText(dto.getQ())) {

				String q = "%" + dto.getQ().toLowerCase() + "%";

				Predicate p1 = cb.like(cb.lower(root.get("txtDecorCategoryName")), q);
				Predicate p2 = cb.like(cb.lower(root.get("txtDecorCategoryCode")), q);
				Predicate p3 = cb.like(cb.lower(propertyJoin.get("txtPropertyName")), q);
				Predicate p4 = cb.like(cb.lower(valueJoin.get("txtPropertyValue")), q);

				predicates.add(cb.or(p1, p2, p3, p4));
			}

			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}

	private static boolean hasText(String s) {
		return s != null && !s.trim().isEmpty();
	}
}