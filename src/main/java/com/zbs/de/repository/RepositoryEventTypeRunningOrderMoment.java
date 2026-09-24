package com.zbs.de.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.EventTypeRunningOrderMoment;

@Repository
public interface RepositoryEventTypeRunningOrderMoment extends JpaRepository<EventTypeRunningOrderMoment, Integer> {

	/**
	 * Every moment every type has, in the order the day runs.
	 *
	 * <p>
	 * One query rather than one per type: the control panel asks for all the
	 * event types at once to fill its dropdown, and the moments ride along with
	 * them.
	 */
	@Query("SELECT m FROM EventTypeRunningOrderMoment m"
			+ " WHERE m.blnIsDeleted = false AND m.blnIsActive = true"
			+ " ORDER BY m.eventType.serEventTypeId ASC, m.numDisplayOrder ASC")
	List<EventTypeRunningOrderMoment> findAllInDayOrder();
}
