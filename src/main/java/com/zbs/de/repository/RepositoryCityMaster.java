package com.zbs.de.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.CityMaster;

@Repository("repositoryCityMaster")
public interface RepositoryCityMaster extends JpaRepository<CityMaster, Integer> {
	List<CityMaster> findByBlnIsDeleted(Boolean blnIsDeleted);

	List<CityMaster> findByBlnIsActiveTrueAndBlnIsDeletedFalse();
	
	@Query("SELECT c FROM CityMaster c where c.blnIsDeleted = false and c.serCityId = :id")
	CityMaster getByCityId(@Param("id") Integer id);
	
}