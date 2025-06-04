package com.zbs.de.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.CityMaster;

@Repository("repositoryCityMaster")
public interface RepositoryCityMaster extends JpaRepository<CityMaster, Integer> {
	List<CityMaster> findByBlnIsDeleted(Boolean blnIsDeleted);
}