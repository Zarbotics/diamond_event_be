package com.zbs.de.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.ExternalSupplierCategory;

@Repository("repositoryExternalSupplierCategory")
public interface RepositoryExternalSupplierCategory extends JpaRepository<ExternalSupplierCategory, Long> {

	/**
	 * What the journey offers.
	 *
	 * <p>
	 * Active only. A retired category still has bookings pointing at it and
	 * still prints on their run sheets; it simply stops being something a new
	 * customer can choose.
	 *
	 * <p>
	 * Ordered by {@code numDisplayOrder} with nulls last, then by name, so a
	 * category added without one appears at the end of the list rather than at
	 * the top of it — which is where a null sorts by default in PostgreSQL for
	 * an ascending order, and is the opposite of what anybody wants for "Other".
	 */
	@Query("SELECT c FROM ExternalSupplierCategory c "
			+ "WHERE c.blnIsDeleted = false AND c.blnIsActive = true "
			+ "ORDER BY c.numDisplayOrder ASC NULLS LAST, c.txtName ASC")
	List<ExternalSupplierCategory> findOffered();

	/** Everything the office maintains, retired ones included. */
	@Query("SELECT c FROM ExternalSupplierCategory c WHERE c.blnIsDeleted = false "
			+ "ORDER BY c.numDisplayOrder ASC NULLS LAST, c.txtName ASC")
	List<ExternalSupplierCategory> findAllForOffice();

	/**
	 * A category with this name, ignoring case.
	 *
	 * <p>
	 * The unique index in V19 is on {@code LOWER(txt_name)}, so a save that did
	 * not check would be refused by the database with a constraint violation
	 * rather than a sentence anybody can act on.
	 *
	 * @param excludeId the row being edited, so renaming a category to its own
	 *                  name is not a clash with itself.
	 */
	@Query("SELECT c FROM ExternalSupplierCategory c WHERE c.blnIsDeleted = false "
			+ "AND LOWER(c.txtName) = LOWER(:name) "
			+ "AND (:excludeId IS NULL OR c.serSupplierCategoryId <> :excludeId)")
	Optional<ExternalSupplierCategory> findByName(@Param("name") String name,
			@Param("excludeId") Long excludeId);

	/** How many declared suppliers use each category, for the office's list. */
	@Query("SELECT s.supplierCategory.serSupplierCategoryId, COUNT(s) "
			+ "FROM EventExternalSupplier s WHERE s.supplierCategory IS NOT NULL "
			+ "AND s.blnIsDeleted = false GROUP BY s.supplierCategory.serSupplierCategoryId")
	List<Object[]> countSuppliersByCategory();
}
