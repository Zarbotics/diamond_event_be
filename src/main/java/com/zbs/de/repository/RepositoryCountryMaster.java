package com.zbs.de.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.CountryMaster;

@Repository("repositoryCountryMaster")
public interface RepositoryCountryMaster extends JpaRepository<CountryMaster, Integer> {
	List<CountryMaster> findByBlnIsDeleted(Boolean blnIsDeleted);
}
