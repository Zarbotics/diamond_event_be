package com.zbs.de.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.EventPaymentDocument;

@Repository("repositoryEventPaymentDocument")
public interface RepositoryEventPaymentDocument extends JpaRepository<EventPaymentDocument, Integer> {
	List<EventPaymentDocument> findByEventPayment_SerEventPaymentIdAndBlnIsDeleted(Integer paymentId,
			Boolean blnIsDeleted);
}
