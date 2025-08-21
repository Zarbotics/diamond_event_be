package com.zbs.de.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.EventDecorCategorySelection;

@Repository("repositoryEventDecorCategorySelection")
public interface RepositoryEventDecorCategorySelection extends JpaRepository<EventDecorCategorySelection, Integer> {
	void deleteByEventMaster_SerEventMasterId(Integer serEventMasterId);

	List<EventDecorCategorySelection> findByEventMaster_SerEventMasterId(Integer serEventMasterId);

	@Query("""
			    SELECT DISTINCT edcs
			    FROM EventDecorCategorySelection edcs
			    LEFT JOIN FETCH edcs.decorCategory dc
			    LEFT JOIN FETCH edcs.selectedProperties props
			    LEFT JOIN FETCH props.property propMaster
			    LEFT JOIN FETCH props.selectedValue selectedVal
			    WHERE edcs.eventMaster.id = :eventMasterId
			      AND (edcs.blnIsDeleted = false OR edcs.blnIsDeleted IS NULL)
			      AND (dc.blnIsDeleted = false OR dc.blnIsDeleted IS NULL)
			      AND (props.blnIsDeleted = false OR props.blnIsDeleted IS NULL)
			      AND (propMaster.blnIsDeleted = false OR propMaster.blnIsDeleted IS NULL)
			      AND (selectedVal.blnIsDeleted = false OR selectedVal.blnIsDeleted IS NULL)
			""")
	List<EventDecorCategorySelection> findByEventMasterWithProperties(@Param("eventMasterId") Integer eventMasterId);

	@Query("""
			    SELECT DISTINCT edcs
			    FROM EventDecorCategorySelection edcs
			    WHERE edcs.eventMaster.id = :eventMasterId
			      AND (edcs.blnIsDeleted = false OR edcs.blnIsDeleted IS NULL)
			""")
	List<EventDecorCategorySelection> findByEventMasterWithPropertiesWithOutJoin(
			@Param("eventMasterId") Integer eventMasterId);
}
