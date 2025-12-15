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
	@Override
	@Transactional
	public void updatePathForSubtree(MenuItem node, String newPath) {
		// Fetch old path
		String oldPath = node.getTxtPath();
		if (oldPath == null)
			oldPath = node.getTxtPath(); // defensive
		// Update the node path and all descendants:
		// new_path || subpath(path, nlevel(old_path))
		String sql = "WITH old AS (SELECT txt_path FROM menu_item WHERE ser_menu_item_id = :id) " + "UPDATE menu_item "
				+ "SET txt_path = :newPath || subpath(menu_item.txt_path, nlevel(old.txt_path)) "
				+ "FROM old WHERE menu_item.txt_path <@ old.txt_path";
		Query q = em.createNativeQuery(sql);
		q.setParameter("id", node.getSerMenuItemId());
		q.setParameter("newPath", newPath);
		q.executeUpdate();
		// It's good to refresh JPA cache if needed
		repo.flush();
	}

	@Override
	public List<DtoMenuItem> buildTreeDto(List<MenuItem> flat) {
		Map<Long, DtoMenuItem> dtoById = flat.stream().map(MapperMenuItem::toDto)
				.collect(Collectors.toMap(d -> d.getSerMenuItemId(), d -> d));
		// Build children lists in DTO level
		Map<Long, List<DtoMenuItem>> childrenMap = new HashMap<>();
		for (DtoMenuItem dto : dtoById.values())
			childrenMap.put(dto.getSerMenuItemId(), new ArrayList<>());
		for (MenuItem m : flat) {
			Long parentId = m.getParent() == null ? null : m.getParent().getSerMenuItemId();
			if (parentId != null && dtoById.containsKey(parentId)) {
				childrenMap.get(parentId).add(dtoById.get(m.getSerMenuItemId()));
			}
		}
		return dtoById.values().stream().filter(d -> d.getParentId() == null).collect(Collectors.toList());
	}
}
