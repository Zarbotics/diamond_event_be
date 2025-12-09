package com.zbs.de.repository;

import com.zbs.de.model.PriceVersion;
import com.zbs.de.util.enums.EnmPriceVersionStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository("repositoryPriceVersion")
public interface RepositoryPriceVersion extends JpaRepository<PriceVersion, Long> {

	Optional<PriceVersion> findByPriceVersionStatus(EnmPriceVersionStatus status);

	List<PriceVersion> findByPriceVersionStatusNot(EnmPriceVersionStatus status);
}
