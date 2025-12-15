package com.zbs.de.repository;

import com.zbs.de.model.PriceEntry;
import com.zbs.de.util.enums.EnmApplyScope;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("repositoryPriceEntry")
public interface RepositoryPriceEntry extends JpaRepository<PriceEntry, Long> {
	List<PriceEntry> findByPriceVersionSerPriceVersionId(Long priceVersionId);

	List<PriceEntry> findByPriceVersionSerPriceVersionIdAndApplyScope(Long priceVersionId, EnmApplyScope applyScope);

	List<PriceEntry> findByPriceVersionSerPriceVersionIdAndApplyScopeAndNumTargetId(Long priceVersionId,
			EnmApplyScope applyScope, Long targetId);
}
