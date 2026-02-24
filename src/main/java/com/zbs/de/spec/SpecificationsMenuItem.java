package com.zbs.de.spec;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.zbs.de.model.MenuItem;
import com.zbs.de.model.MenuItemRole;
import com.zbs.de.model.dto.menu.DtoMenuItemSearch;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

public final class SpecificationsMenuItem {
	private SpecificationsMenuItem() {
	}

	public static Specification<MenuItem> fromDto(DtoMenuItemSearch dto) {
		return (root, query, cb) -> {

			query.distinct(true);
			List<Predicate> predicates = new ArrayList<>();

			// soft delete
			predicates.add(cb.or(cb.isNull(root.get("blnIsDeleted")), cb.equal(root.get("blnIsDeleted"), false)));

			// ids
			if (dto.getSerMenuItemId() != null) {
				predicates.add(cb.equal(root.get("serMenuItemId"), dto.getSerMenuItemId()));
			}

			// text filters
			like(predicates, cb, root.get("txtCode"), dto.getTxtCode());
			like(predicates, cb, root.get("txtName"), dto.getTxtName());
			like(predicates, cb, root.get("txtDescription"), dto.getTxtDescription());

			// global search
			if (hasText(dto.getQ())) {
				String q = "%" + dto.getQ().trim().toLowerCase() + "%";

				Join<MenuItem, MenuItemRole> role = root.join("menuItemRole", JoinType.LEFT);

				predicates.add(cb.or(cb.like(cb.lower(root.get("txtCode")), q),
						cb.like(cb.lower(root.get("txtName")), q), cb.like(cb.lower(root.get("txtDescription")), q),
						cb.like(cb.lower(role.get("txtRoleCode")), q), cb.like(cb.lower(role.get("txtRoleName")), q)));
			}

			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}

	/* helpers */
	private static void like(List<Predicate> ps, CriteriaBuilder cb, Path<String> path, String value) {
		if (hasText(value)) {
			ps.add(cb.like(cb.lower(path), "%" + value.trim().toLowerCase() + "%"));
		}
	}

	private static boolean hasText(String s) {
		return s != null && !s.trim().isEmpty();
	}

}
