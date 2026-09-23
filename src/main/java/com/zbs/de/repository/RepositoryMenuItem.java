package com.zbs.de.repository;

import com.zbs.de.model.CateringDeliveryBooking;
import com.zbs.de.model.MenuItem;
import com.zbs.de.model.MenuItemRole;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RepositoryMenuItem extends JpaRepository<MenuItem, Long> {
	@Query("SELECT e FROM MenuItem e WHERE  e.serMenuItemId = :id AND e.blnIsDeleted = false")
	Optional<MenuItem> getByMenuItemId(@Param("id") Long id);

	List<MenuItem> findByTxtRole(String role);

	List<MenuItem> findByTxtType(String txtType);

	@Query(value = "select * from menu_item where txt_path <@ ?1", nativeQuery = true)
	List<MenuItem> findDescendantsByTxtPath(String ltreePath);

	/**
	 * The children of one section.
	 *
	 * <h4>Why bln_is_deleted is in here</h4>
	 *
	 * It was not, and deleting a stand emptied the customer's entire food menu.
	 * {@code ServiceMenuItemImpl.delete} is a soft delete — it sets
	 * {@code bln_is_deleted} and leaves {@code bln_is_active} alone — so a
	 * deleted item still came back as a child here. The menu walk then asked
	 * {@code getCompositeWithComponents} for it, that lookup <em>does</em> filter
	 * deleted rows, it threw "Menu item not found", and the controller answered
	 * the whole menu as an error. One removed stand, and every customer saw no
	 * food at all.
	 *
	 * <p>
	 * Its three siblings below are the same query with different orderings and
	 * filters, and two of them already had the clause. That is the shape of
	 * fault a set of near-identical queries produces: the rule is applied
	 * wherever somebody remembered it.
	 */
	@Query(value = "select * from menu_item where parent_menu_item_id =:parentId and bln_is_deleted = false and bln_is_active = true ORDER BY LOWER(txt_name) asc", nativeQuery = true)
	List<MenuItem> findByParentId(@Param("parentId")Long parentId);
	
	@Query(value = "select * from menu_item where bln_is_deleted = false and bln_is_active = true and  parent_menu_item_id =:parentId ORDER BY num_display_order asc", nativeQuery = true)
	List<MenuItem> findByParentIdByDisplayOrder(@Param("parentId")Long parentId);
	
	@Query(value = "select * from menu_item where  bln_is_deleted = false and bln_is_active = true and parent_menu_item_id =:parentId and bln_is_catering_item = true ORDER BY LOWER(txt_name) asc", nativeQuery = true)
	List<MenuItem> findCateringItemsByParentId(@Param("parentId")Long parentId);
	
	/** Deleted and inactive rows excluded here too — see findByParentId. */
	@Query(value = "select * from menu_item where parent_menu_item_id =:parentId and bln_is_deleted = false and bln_is_active = true and bln_is_catering_item = true ORDER BY num_display_order asc", nativeQuery = true)
	List<MenuItem> findCateringItemsByParentIdByDisplayOrder(@Param("parentId")Long parentId);

	MenuItem findByTxtCode(String txtCode);

	@Query("""
			    SELECT m.txtCode
			    FROM MenuItem m
			    WHERE m.txtCode LIKE CONCAT(:prefix, '_%')
			    ORDER BY m.txtCode DESC
			    LIMIT 1
			""")
	String findMaxCodeByPrefix(@Param("prefix") String prefix);

	/**
	 * The highest number already used under a code prefix.
	 *
	 * <h4>Why this replaces a string sort</h4>
	 *
	 * {@code findMaxCodeByPrefix} above orders codes as text and hands the winner
	 * to a parser that gives up on anything after the last dash. That works only
	 * while every code with a given prefix is that prefix, a dash and digits —
	 * true of production today by luck, and false the moment somebody types a
	 * code by hand, which the old menu screen let them do.
	 *
	 * <p>
	 * When it is false the parser fails, the generator restarts at 1, and the
	 * next item created gets a code that already exists. The insert then fails
	 * with a 500 and no explanation. It is exactly what happened as soon as a
	 * database held {@code MI-SET-STARTERS} alongside {@code MI-1417}: every call
	 * answered {@code MI-0001}, so the first new dish saved and the second did
	 * not.
	 *
	 * <p>
	 * Codes that are not the prefix plus digits are ignored rather than parsed —
	 * they carry no sequence number to continue from.
	 */
	@Query(value = """
			SELECT COALESCE(MAX(CAST(SUBSTRING(txt_code FROM '^' || :prefix || '-([0-9]+)$') AS INTEGER)), 0)
			FROM menu_item
			WHERE txt_code ~ ('^' || :prefix || '-[0-9]+$')
			""", nativeQuery = true)
	int findHighestNumberForPrefix(@Param("prefix") String prefix);

	List<MenuItem> findByTxtRoleAndBlnIsDeletedFalse(String txtRole);

	@Query("SELECT e FROM MenuItem e WHERE  e.menuItemRole.serMenuItemRoleId = :id AND e.blnIsDeleted = false")
	List<MenuItem> getAllItemsByRoleId(@Param("id") Integer id);
	

//	@Query("SELECT e FROM MenuItem e WHERE  e.menuItemRole.serMenuItemRoleId = :id AND LOWER(e.txtName) <> 'other' AND e.blnIsDeleted = false AND e.blnIsActive = true ORDER BY e.numDisplayOrder")
//	List<MenuItem> getAllActiveItemsByRoleId(@Param("id") Integer id);

	
	@Query("SELECT e FROM MenuItem e WHERE  e.menuItemRole.serMenuItemRoleId = :id AND LOWER(e.txtName) <> 'other' AND e.blnIsDeleted = false AND e.blnIsActive = true ORDER BY e.numDisplayOrder asc")
	List<MenuItem> getAllActiveItemsByRoleId(@Param("id") Integer id);
	
	@Query("SELECT e FROM MenuItem e WHERE  e.menuItemRole.serMenuItemRoleId = :id AND LOWER(e.txtName) <> 'other' AND e.blnIsDeleted = false AND e.blnIsActive = true and e.blnIsCateringItem = true ORDER BY e.numDisplayOrder asc")
	List<MenuItem> getAllActiveCateringItemsByRoleId(@Param("id") Integer id);

	@Query("SELECT mi FROM MenuItem mi WHERE mi.blnIsComposite = true AND mi.blnIsDeleted = false ORDER BY mi.txtName")
	List<MenuItem> findAllCompositeItems();

	@Query("SELECT mi FROM MenuItem mi WHERE mi.blnIsComposite = true AND mi.blnIsDeleted = false AND mi.blnIsActive = true ORDER BY mi.txtName")
	List<MenuItem> findAllActiveCompositeItems();

	@Query("SELECT mi FROM MenuItem mi WHERE mi.blnIsDeleted = false AND mi.blnIsActive = true ORDER BY mi.serMenuItemId desc")
	List<MenuItem> getAllActiveMenuItems();

	List<MenuItem> findByMenuItemRoleInAndBlnIsDeletedFalse(Collection<MenuItemRole> roles);

	@Query("SELECT DISTINCT m.txtType FROM MenuItem m WHERE m.txtType IS NOT NULL")
	List<String> findDistinctTxtTypes();

	List<MenuItem> findByTxtTypeAndBlnIsDeletedFalse(String txtType);

	@Query("""
			SELECT m
			FROM MenuItem m
			WHERE m.blnIsDeleted = false
			  AND m.blnIsActive = true
			  AND (
			       LOWER(m.txtName) LIKE LOWER(CONCAT('%', :query, '%'))
			    OR LOWER(m.txtCode) LIKE LOWER(CONCAT('%', :query, '%'))
			  )
			ORDER BY m.numDisplayOrder ASC
			""")
	List<MenuItem> searchByQuery(@Param("query") String query);

	@Query("SELECT mi FROM MenuItem mi WHERE mi.blnIsDeleted = false ORDER BY mi.serMenuItemId desc")
	List<MenuItem> getAllMenuItems();

	@Query("""
			    SELECT mi
			    FROM MenuItem mi
			    WHERE LOWER(mi.parent.txtName) = LOWER(:code)
			      AND mi.blnIsComposite = false
			      AND mi.blnIsDeleted = false
			      AND mi.blnIsActive = true
			      AND mi.menuItemRole.serMenuItemRoleId = 3
			    ORDER BY mi.txtName
			""")
	List<MenuItem> getAllNonCompositeActiveItemsByParentItemCode(@Param("code") String code);

	@EntityGraph(attributePaths = { "menuItemRole" })
	Page<MenuItem> findAll(Specification<MenuItem> spec, Pageable pageable);

}