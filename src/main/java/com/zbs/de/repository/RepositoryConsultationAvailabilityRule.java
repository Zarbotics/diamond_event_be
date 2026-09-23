package com.zbs.de.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.ConsultationAvailabilityRule;

@Repository
public interface RepositoryConsultationAvailabilityRule
		extends JpaRepository<ConsultationAvailabilityRule, Integer> {

	List<ConsultationAvailabilityRule> findBySerHostIdAndBlnIsDeletedFalse(Integer serHostId);
}
