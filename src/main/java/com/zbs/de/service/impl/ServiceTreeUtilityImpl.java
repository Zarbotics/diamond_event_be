package com.zbs.de.service.impl;

import com.zbs.de.mapper.MapperMenuItem;
import com.zbs.de.model.MenuItem;
import com.zbs.de.model.dto.DtoMenuItem;
import com.zbs.de.repository.RepositoryMenuItem;
import com.zbs.de.service.ServiceTreeUtility;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service("serviceTreeUtilityImpl")
public class ServiceTreeUtilityImpl implements ServiceTreeUtility {

	private final RepositoryMenuItem repo;
	private final EntityManager em;

	public ServiceTreeUtilityImpl(RepositoryMenuItem repo, EntityManager em) {
		this.repo = repo;
		this.em = em;
	}

	@Override
	public String computeChildPath(String parentPath, String childCode) {
		if (parentPath == null || parentPath.isBlank())
			return sanitizeForLtree(childCode);
		return parentPath + "." + sanitizeForLtree(childCode);
	}

	@Override
	public String sanitizeForLtree(String s) {
		if (s == null)
			return "";
		return s.trim().replaceAll("[^a-zA-Z0-9_]", "_");
	}

	/**
	 * Update path for node and all descendants using ltree functions. native SQL
	 * performs the bulk update.
	 */
//	@Override
//	@Transactional
//	public void updatePathForSubtree(MenuItem node, String newPath) {
//		// Fetch old path
//		String oldPath = node.getTxtPath();
//		if (oldPath == null)
//			oldPath = node.getTxtPath(); // defensive
//		// Update the node path and all descendants:
//		// new_path || subpath(path, nlevel(old_path))
//		String sql = "WITH old AS (SELECT txt_path FROM menu_item WHERE ser_menu_item_id = :id) " + "UPDATE menu_item "
//				+ "SET txt_path = :newPath || subpath(menu_item.txt_path, nlevel(old.txt_path)) "
//				+ "FROM old WHERE menu_item.txt_path <@ old.txt_path";
//		Query q = em.createNativeQuery(sql);
//		q.setParameter("id", node.getSerMenuItemId());
//		q.setParameter("newPath", newPath);
//		q.executeUpdate();
//		// It's good to refresh JPA cache if needed
//		repo.flush();
//	}

	@Override
	@Transactional
	public void updatePathForSubtree(MenuItem node, String newPath) {

		if (node == null || node.getSerMenuItemId() == null) {
			throw new IllegalArgumentException("MenuItem or ID must not be null");
		}

		if (newPath == null || newPath.isBlank()) {
			throw new IllegalArgumentException("newPath must not be null or empty");
		}

		// Keep entity state in sync with DB update
		node.setTxtPath(newPath);

		String sql = "WITH old AS ( " + "   SELECT txt_path FROM menu_item WHERE ser_menu_item_id = :id " + ") "
				+ "UPDATE menu_item " + "SET txt_path = CASE " + "   WHEN menu_item.ser_menu_item_id = :id "
				+ "       THEN CAST(:newPath AS ltree) " + "   ELSE " + "       CAST(:newPath AS ltree) "
				+ "       || subpath(menu_item.txt_path, nlevel(old.txt_path)) " + "END " + "FROM old "
				+ "WHERE menu_item.txt_path <@ old.txt_path";

		Query query = em.createNativeQuery(sql);
		query.setParameter("id", node.getSerMenuItemId());
		query.setParameter("newPath", newPath);

		query.executeUpdate();

		// Force Hibernate to sync persistence context
		em.flush();
	}

	/**
	 * The menu as a tree: roots, each carrying what is under it.
	 *
	 * <h3>What was wrong</h3>
	 *
	 * This built {@code childrenMap} correctly and then returned the roots
	 * <em>without it</em>, so {@code /menu/item/tree} answered twelve categories
	 * with nothing under any of them — on every call since it was written.
	 * {@code DtoMenuItem} had no {@code children} field to attach them to, which
	 * is why nothing complained.
	 *
	 * <p>
	 * The screen built on the endpoint therefore showed twelve rows, and the flat
	 * 436-row list stayed the only way to see the menu at all. That is most of
	 * why the menu screens still felt unchanged after the work that was supposed
	 * to replace them.
	 *
	 * <h3>Deleted items are left out</h3>
	 *
	 * {@code findAll} returns them, and hanging a live dish under a deleted
	 * section would put it on a screen under a heading nobody can see. A dish
	 * whose section is gone is a real fault worth surfacing, but the place to
	 * surface it is a report, not silently in the middle of the catalogue.
	 *
	 * <h3>Order</h3>
	 *
	 * By display order, then by name, at every level. Without it the order is the
	 * map's, which is to say the ids' — so a category renamed to sort first would
	 * still appear wherever it was created.
	 */
	@Override
	public List<DtoMenuItem> buildTreeDto(List<MenuItem> flat) {

		List<MenuItem> live = flat.stream()
				.filter(m -> !Boolean.TRUE.equals(m.getBlnIsDeleted()))
				.collect(Collectors.toList());

		Map<Long, DtoMenuItem> dtoById = live.stream().map(MapperMenuItem::toDto)
				.collect(Collectors.toMap(DtoMenuItem::getSerMenuItemId, d -> d));

		for (MenuItem m : live) {
			Long parentId = m.getParent() == null ? null : m.getParent().getSerMenuItemId();
			DtoMenuItem parent = parentId == null ? null : dtoById.get(parentId);

			if (parent != null) {
				parent.getChildren().add(dtoById.get(m.getSerMenuItemId()));
			}
		}

		dtoById.values().forEach(dto -> dto.getChildren().sort(BY_POSITION_THEN_NAME));

		/*
		 * A root is one whose parent is not in this set — not merely one with no
		 * parent id. An item parented to a deleted section would otherwise vanish
		 * from the tree entirely rather than appear at the top where somebody can
		 * see it and put it right.
		 */
		List<DtoMenuItem> roots = live.stream()
				.filter(m -> m.getParent() == null || !dtoById.containsKey(m.getParent().getSerMenuItemId()))
				.map(m -> dtoById.get(m.getSerMenuItemId()))
				.collect(Collectors.toList());

		roots.sort(BY_POSITION_THEN_NAME);
		return roots;
	}

	/** Display order first, then name, with nulls last rather than first. */
	private static final Comparator<DtoMenuItem> BY_POSITION_THEN_NAME = Comparator
			.comparing(DtoMenuItem::getNumDisplayOrder, Comparator.nullsLast(Comparator.naturalOrder()))
			.thenComparing(DtoMenuItem::getTxtName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
}
