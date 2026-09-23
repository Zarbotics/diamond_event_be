package com.zbs.de.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.ConsultationHost;

@Repository
public interface RepositoryConsultationHost extends JpaRepository<ConsultationHost, Integer> {

	List<ConsultationHost> findByBlnIsActiveTrueAndBlnIsDeletedFalseOrderBySerHostIdAsc();

	Optional<ConsultationHost> findBySerHostIdAndBlnIsDeletedFalse(Integer serHostId);
}
