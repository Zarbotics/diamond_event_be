package com.zbs.de.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.CalendarConnection;

@Repository
public interface RepositoryCalendarConnection extends JpaRepository<CalendarConnection, Integer> {

	List<CalendarConnection> findBySerHostIdAndBlnIsDeletedFalse(Integer serHostId);

	/**
	 * The one calendar a host's consultations are written to.
	 *
	 * <p>
	 * Optional because a host may have connected nothing at all, which is a
	 * perfectly ordinary state — consultations still work, they simply do not
	 * appear in anybody's Google or Outlook.
	 */
	@Query("""
			SELECT c FROM CalendarConnection c
			WHERE c.serHostId = :hostId
			  AND c.blnIsWriteTarget = true
			  AND c.blnIsDeleted = false
			""")
	Optional<CalendarConnection> writeTargetFor(@Param("hostId") Integer hostId);

	/** Everything worth syncing: connected, not deleted, not known broken. */
	@Query("""
			SELECT c FROM CalendarConnection c
			WHERE c.blnIsDeleted = false
			  AND c.txtSyncStatus <> 'NEEDS_RECONNECT'
			""")
	List<CalendarConnection> syncable();
}
