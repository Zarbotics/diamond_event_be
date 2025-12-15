package com.zbs.de.repository;

import com.zbs.de.model.PricingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("repositoryPricingRule")
public interface RepositoryPricingRule extends JpaRepository<PricingRule, Long> {
	List<PricingRule> findByPriceVersion_SerPriceVersionIdOrderByNumPriorityAsc(Long priceVersionId);
}
