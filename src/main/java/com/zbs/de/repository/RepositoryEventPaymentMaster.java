package com.zbs.de.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.EventPayment;

@Repository("repositoryEventPaymentMaster")
public interface RepositoryEventPaymentMaster extends JpaRepository<EventPayment, Integer> {
	List<EventPayment> findByBlnIsDeleted(Boolean blnIsDeleted);
}