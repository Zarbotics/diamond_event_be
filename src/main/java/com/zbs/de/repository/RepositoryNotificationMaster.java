package com.zbs.de.repository;

import org.springframework.stereotype.Repository;

import com.zbs.de.model.NotificationMaster;
import com.zbs.de.model.UserMaster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@Repository("repositoryNotificationMaster")
public interface RepositoryNotificationMaster extends JpaRepository<NotificationMaster, Long> {
	Page<NotificationMaster> findByUserMasterOrderByCreatedAtDesc(UserMaster userMaster, Pageable pageable);

	List<NotificationMaster> findByUserMasterAndBlnIsReadFalseOrderByCreatedAtDesc(UserMaster userMaster);

	long countByUserMasterAndBlnIsReadFalse(UserMaster userMaster);
}