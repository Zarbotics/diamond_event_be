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
			+ "WHERE m.blnIsDeleted = false and m.blnIsService = false AND (o.blnIsDeleted = false OR o IS NULL) ORDER BY numDisplayOrder asc")
	List<DecorExtrasMaster> findAllServicesWithOptionsWhereNotDeletedByDisplayOrder();
	
	@Query("SELECT e FROM DecorExtrasMaster e WHERE e.serExtrasId = :id AND e.blnIsDeleted = false")
	Optional<DecorExtrasMaster> findByIdAndNotDeleted(@Param("id") Integer id);
	
	@Query("SELECT DISTINCT m FROM DecorExtrasMaster m " + "LEFT JOIN FETCH m.decorExtrasOptions o "
			+ "WHERE m.blnIsDeleted = false AND m.blnIsActive = true AND (o.blnIsDeleted = false OR o IS NULL) AND o.blnIsActive = true order by m.txtExtrasName, o.txtOptionName asc")
	List<DecorExtrasMaster> findAllActiveWithOptionsWhereNotDeleted();
	
	@Query("SELECT DISTINCT m FROM DecorExtrasMaster m " + "LEFT JOIN FETCH m.decorExtrasOptions o "
			+ "WHERE m.blnIsDeleted = false AND m.blnIsService = false AND m.blnIsActive = true AND (o.blnIsDeleted = false OR o IS NULL) AND o.blnIsActive = true order by m.txtExtrasName, o.txtOptionName asc")
	List<DecorExtrasMaster> findAllActiveExtrasWithOptionsWhereNotDeleted();
	
	@Query("SELECT DISTINCT m FROM DecorExtrasMaster m " + "LEFT JOIN FETCH m.decorExtrasOptions o "
			+ "WHERE m.blnIsDeleted = false AND m.blnIsService = true AND m.blnIsActive = true AND (o.blnIsDeleted = false OR o IS NULL) AND o.blnIsActive = true order by m.txtExtrasName, o.txtOptionName asc")
	List<DecorExtrasMaster> findAllActiveServicesWithOptionsWhereNotDeleted();
	
	
	@Query("SELECT MAX(e.txtExtrasCode) FROM DecorExtrasMaster e")
	String findMaxExtrasCode();

}
