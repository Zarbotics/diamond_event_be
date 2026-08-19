package com.zbs.de.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.ConsultationAvailabilityException;

@Repository
public interface RepositoryConsultationAvailabilityException
		extends JpaRepository<ConsultationAvailabilityException, Integer> {

	List<ConsultationAvailabilityException> findBySerHostIdAndDteOnDateBetweenAndBlnIsDeletedFalse(
			Integer serHostId, LocalDate from, LocalDate to);
}
