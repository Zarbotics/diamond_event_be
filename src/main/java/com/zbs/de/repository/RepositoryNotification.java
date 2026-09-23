package com.zbs.de.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.Notification;

@Repository("repositoryNotification")
public interface RepositoryNotification extends JpaRepository<Notification, Long> {

	/**
	 * One person's bell, newest first.
	 *
	 * <p>
	 * Dismissed ones are excluded rather than filtered out afterwards, because
	 * over time they will far outnumber the live ones and this query runs on
	 * every page load in the portal.
	 */
	@Query("SELECT n FROM Notification n WHERE n.serUserId = :userId "
			+ "AND n.dteDismissedOn IS NULL AND n.blnIsDeleted = false "
			+ "ORDER BY n.createdDate DESC")
	List<Notification> inboxFor(@Param("userId") Long userId, Pageable pageable);

	/**
	 * How many this person has not read.
	 *
	 * <p>
	 * LOW is excluded. A badge counting things that were never meant to
	 * interrupt is a badge that is permanently non-zero, and a badge that is
	 * always on is one nobody looks at.
	 */
	@Query("SELECT COUNT(n) FROM Notification n WHERE n.serUserId = :userId "
			+ "AND n.dteReadOn IS NULL AND n.dteDismissedOn IS NULL "
			+ "AND n.blnIsDeleted = false AND n.txtPriority <> com.zbs.de.util.enums.EnmNotificationPriority.LOW")
	long unreadCountFor(@Param("userId") Long userId);

	/**
	 * The open notification for a repeating thing, if there is one.
	 *
	 * <p>
	 * Twelve payments against one booking in an afternoon should be one line
	 * saying twelve, not twelve lines. This is how the twelfth finds the first.
	 */
	@Query("SELECT n FROM Notification n WHERE n.serUserId = :userId "
			+ "AND n.txtGroupKey = :groupKey AND n.dteDismissedOn IS NULL "
			+ "AND n.blnIsDeleted = false ORDER BY n.createdDate DESC")
	List<Notification> openInGroup(@Param("userId") Long userId, @Param("groupKey") String groupKey);

	Optional<Notification> findBySerNotificationIdAndSerUserId(Long id, Long userId);

	/**
	 * Marks everything this person has not read as read.
	 *
	 * <p>
	 * A bulk update rather than a read-modify-write loop: "mark all as read" on
	 * a list of two hundred should be one statement, and loading two hundred
	 * entities to set one column on each is how a bell becomes slow.
	 */
	@Modifying
	@Query("UPDATE Notification n SET n.dteReadOn = :now WHERE n.serUserId = :userId "
			+ "AND n.dteReadOn IS NULL AND n.dteDismissedOn IS NULL AND n.blnIsDeleted = false")
	int markAllReadFor(@Param("userId") Long userId, @Param("now") Date now);
}
