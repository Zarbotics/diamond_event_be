package com.zbs.de.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.ConsultationType;

@Repository
public interface RepositoryConsultationType extends JpaRepository<ConsultationType, Integer> {

	List<ConsultationType> findByBlnIsActiveTrueAndBlnIsDeletedFalseOrderBySerConsultationTypeIdAsc();

	Optional<ConsultationType> findBySerConsultationTypeIdAndBlnIsDeletedFalse(Integer id);
}
