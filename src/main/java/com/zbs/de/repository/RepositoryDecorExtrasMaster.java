package com.zbs.de.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.DecorExtrasMaster;

@Repository("repositoryDecorExtrasMaster")
public interface RepositoryDecorExtrasMaster extends JpaRepository<DecorExtrasMaster, Integer> {
	List<DecorExtrasMaster> findByBlnIsDeletedFalse();

	@Query("SELECT DISTINCT m FROM DecorExtrasMaster m " + "LEFT JOIN FETCH m.decorExtrasOptions o "
			+ "WHERE m.blnIsDeleted = false AND (o.blnIsDeleted = false OR o IS NULL)")
	List<DecorExtrasMaster> findAllWithOptionsWhereNotDeleted();
	
	@Query("SELECT DISTINCT m FROM DecorExtrasMaster m " + "LEFT JOIN FETCH m.decorExtrasOptions o "
			+ "WHERE m.blnIsDeleted = false AND (o.blnIsDeleted = false OR o IS NULL) ORDER BY numDisplayOrder asc")
	List<DecorExtrasMaster> findAllWithOptionsWhereNotDeletedByDisplayOrder();
	
	@Query("SELECT DISTINCT m FROM DecorExtrasMaster m " + "LEFT JOIN FETCH m.decorExtrasOptions o "
			+ "WHERE m.blnIsDeleted = false and m.blnIsService = false AND (o.blnIsDeleted = false OR o IS NULL) ORDER BY numDisplayOrder asc")
	List<DecorExtrasMaster> findAllExtrasWithOptionsWhereNotDeletedByDisplayOrder();
	
	@Query("SELECT DISTINCT m FROM DecorExtrasMaster m " + "LEFT JOIN FETCH m.decorExtrasOptions o "
			+ "WHERE m.blnIsDeleted = false and m.blnIsService = true AND (o.blnIsDeleted = false OR o IS NULL) ORDER BY numDisplayOrder asc")
	List<DecorExtrasMaster> findAllServicesWithOptionsWhereNotDeletedByDisplayOrder();
	
	@Query("SELECT e FROM DecorExtrasMaster e WHERE e.serExtrasId = :id AND e.blnIsDeleted = false")
	Optional<DecorExtrasMaster> findByIdAndNotDeleted(@Param("id") Integer id);
	
	/*
	 * The option predicate has to tolerate a null option.
	 *
	 * These three were written as LEFT JOIN FETCH with
	 * "(o.blnIsDeleted = false OR o IS NULL) AND o.blnIsActive = true" in the
	 * WHERE clause. That trailing condition cannot hold for a null o, so the
	 * outer join collapsed into an inner one and every extra or service with no
	 * options was silently dropped — the "OR o IS NULL" beside it shows the
	 * intent was the opposite.
	 *
	 * The effect on the catalogue: an administrator adds "Videography" with no
	 * sub-options and it never reaches a customer, with nothing anywhere to say
	 * why. Four of the five seeded services were invisible for this reason.
	 *
	 * The whole predicate is parenthesised under a single "o IS NULL OR", which
	 * is the only form that works here: Hibernate rejects an ON clause on a
	 * fetch join outright ("Fetch join has a 'with' clause").
	 */
	@Query("SELECT DISTINCT m FROM DecorExtrasMaster m "
			+ "LEFT JOIN FETCH m.decorExtrasOptions o "
			+ "WHERE m.blnIsDeleted = false AND m.blnIsActive = true "
			+ "AND (o IS NULL OR (o.blnIsDeleted = false AND o.blnIsActive = true)) "
			+ "ORDER BY m.txtExtrasName ASC")
	List<DecorExtrasMaster> findAllActiveWithOptionsWhereNotDeleted();
	
	@Query("SELECT DISTINCT m FROM DecorExtrasMaster m "
			+ "LEFT JOIN FETCH m.decorExtrasOptions o "
			+ "WHERE m.blnIsDeleted = false AND m.blnIsService = false AND m.blnIsActive = true "
			+ "AND (o IS NULL OR (o.blnIsDeleted = false AND o.blnIsActive = true)) "
			+ "ORDER BY m.txtExtrasName ASC")
	List<DecorExtrasMaster> findAllActiveExtrasWithOptionsWhereNotDeleted();
	
	@Query("SELECT DISTINCT m FROM DecorExtrasMaster m "
			+ "LEFT JOIN FETCH m.decorExtrasOptions o "
			+ "WHERE m.blnIsDeleted = false AND m.blnIsService = true AND m.blnIsActive = true "
			+ "AND (o IS NULL OR (o.blnIsDeleted = false AND o.blnIsActive = true)) "
			+ "ORDER BY m.txtExtrasName ASC")
	List<DecorExtrasMaster> findAllActiveServicesWithOptionsWhereNotDeleted();
	
	
	@Query("SELECT MAX(e.txtExtrasCode) FROM DecorExtrasMaster e")
	String findMaxExtrasCode();

}
