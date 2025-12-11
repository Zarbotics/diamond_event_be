package com.zbs.de.repository;

import com.zbs.de.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RepositoryMenuItem extends JpaRepository<MenuItem, Long> {
	List<MenuItem> findByTxtRole(String role);

	List<MenuItem> findByTxtType(String txtType);

	@Query(value = "select * from menu_item where txt_path <@ ?1", nativeQuery = true)
	List<MenuItem> findDescendantsByTxtPath(String ltreePath);

	@Query(value = "select * from menu_item where parent_menu_item_id = ?1", nativeQuery = true)
	List<MenuItem> findByParentId(Long parentId);
	
	MenuItem findByTxtCode(String txtCode);

	@Query("""
			    SELECT m.txtCode
			    FROM MenuItem m
			    WHERE m.txtCode LIKE CONCAT(:prefix, '_%')
			    ORDER BY m.txtCode DESC
			    LIMIT 1
			""")
	String findMaxCodeByPrefix(@Param("prefix") String prefix);
	 
}
