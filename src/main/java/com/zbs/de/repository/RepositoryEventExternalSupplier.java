package com.zbs.de.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.EventExternalSupplier;

@Repository
public interface RepositoryEventExternalSupplier extends JpaRepository<EventExternalSupplier, Long> {

	/**
	 * The suppliers declared on one booking, in the order the customer entered
	 * them.
	 *
	 * <p>
	 * Ordered by the customer's own sequence and then by id, because a row
	 * saved before the ordering column existed has none and would otherwise
	 * float to an arbitrary place in the list each time it is read.
	 */
	List<EventExternalSupplier> findByEventMaster_SerEventMasterIdAndBlnIsDeletedFalseOrderByNumDisplayOrderAscSerEventExternalSupplierIdAsc(
			Integer serEventMasterId);

	List<EventExternalSupplier> findByEventMaster_SerEventMasterId(Integer serEventMasterId);
}
