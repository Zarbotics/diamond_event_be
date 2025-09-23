package com.zbs.de.repository.impl;

import org.springframework.stereotype.Repository;

import com.zbs.de.repository.RepositoryMenuFoodMasterCustom;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Repository
public class RepositoryMenuFoodMasterImpl implements RepositoryMenuFoodMasterCustom {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public int findMaxCodeNumberByPrefix(String prefix, int codeLength) {
		String sql = "SELECT MAX(CAST(SUBSTRING(txt_menu_food_code, :startIndex, :length) AS INT)) "
				+ "FROM menu_food_master WHERE txt_menu_food_code LIKE :prefixPattern";

		Query query = entityManager.createNativeQuery(sql);

		// SQL substring uses 1-based index
		int startIndex = prefix.length() + 1;

		query.setParameter("startIndex", startIndex);
		query.setParameter("length", codeLength);
		query.setParameter("prefixPattern", prefix + "%");

		Object result = query.getSingleResult();

		return result != null ? ((Number) result).intValue() : 0;
	}
}
