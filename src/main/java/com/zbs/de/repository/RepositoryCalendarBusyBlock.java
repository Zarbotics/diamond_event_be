package com.zbs.de.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.CalendarBusyBlock;

@Repository
public interface RepositoryCalendarBusyBlock extends JpaRepository<CalendarBusyBlock, Integer> {

	/** Anything overlapping the window, which is not the same as starting inside it. */
	@Query("""
			SELECT b FROM CalendarBusyBlock b
			WHERE b.serHostId = :hostId
			  AND b.dteStartsAt < :windowEnd
			  AND b.dteEndsAt   > :windowStart
			""")
	List<CalendarBusyBlock> overlapping(@Param("hostId") Integer hostId,
			@Param("windowStart") Instant windowStart, @Param("windowEnd") Instant windowEnd);

	void deleteBySerCalendarConnectionId(Integer serCalendarConnectionId);
}
