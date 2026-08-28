package com.zbs.de.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.MenuOfferingPrice;
import com.zbs.de.model.PriceVersion;

/**
 * Prices, by list.
 *
 * <p>
 * Stage M5 of §17.3. Thin on purpose: enough to prove the table, the backfill
 * and the entity agree, and enough to answer the one question M5b is built on —
 * which price list applies on a given day. The queries a price-list screen will
 * need are written in the stage that introduces it.
 */
@Repository("repositoryMenuOfferingPrice")
public interface RepositoryMenuOfferingPrice extends JpaRepository<MenuOfferingPrice, Long> {

	/** Every price on one list. */
	List<MenuOfferingPrice> findByPriceVersion_SerPriceVersionId(Long priceVersionId);

	/** What one offering has cost, across every list it appears on. */
	List<MenuOfferingPrice> findByOffering_SerOfferingId(Long offeringId);

	/**
	 * The price list that applies on a given day.
	 *
	 * <h4>How the choice is made, and why it is a query rather than a rule in
	 * Java</h4>
	 *
	 * Published lists only — a draft is next year's prices being prepared, and
	 * quoting from one would charge a customer a figure nobody has agreed.
	 * Effective on the day: started, and either open-ended or not yet finished.
	 * Highest priority wins where two overlap, which is what lets a short
	 * Christmas list sit on top of the standing one without either being edited.
	 *
	 * <p>
	 * The date is the <em>event's</em> date rather than today's. A booking taken
	 * in March for a wedding in September is quoted at September's prices if a
	 * September list exists, which is the whole point of preparing one in
	 * advance — and it is the question staff actually ask.
	 */
	@Query("""
			SELECT v FROM PriceVersion v
			WHERE v.priceVersionStatus = com.zbs.de.util.enums.EnmPriceVersionStatus.PUBLISHED
			  AND (v.blnIsDeleted IS NULL OR v.blnIsDeleted = false)
			  AND (v.dteEffectiveFrom IS NULL OR v.dteEffectiveFrom <= :on)
			  AND (v.dteEffectiveTo IS NULL OR v.dteEffectiveTo >= :on)
			ORDER BY v.numPriority DESC, v.dteEffectiveFrom DESC
			""")
	List<PriceVersion> findVersionsEffectiveOn(@Param("on") Date on);

	/**
	 * What one offering costs on the list effective that day.
	 *
	 * <p>
	 * Returns nothing rather than a guess when no list covers the day. A price
	 * invented from the nearest available version is the kind of helpfulness
	 * that shows up months later as a quote nobody can explain.
	 */
	@Query("""
			SELECT p FROM MenuOfferingPrice p
			WHERE p.offering.serOfferingId = :offeringId
			  AND p.priceVersion.serPriceVersionId = :versionId
			""")
	Optional<MenuOfferingPrice> findPriceOn(@Param("offeringId") Long offeringId,
			@Param("versionId") Long versionId);

	/**
	 * Offerings that no list prices at all.
	 *
	 * <p>
	 * Expected to be zero after the backfill and expected to stay zero: an
	 * offering absent from every published list is a dish that would silently
	 * come to nothing the moment the reads move onto versions in M5b. Reported
	 * rather than enforced, because a draft list under construction legitimately
	 * has gaps.
	 */
	@Query("""
			SELECT COUNT(o) FROM MenuOffering o
			WHERE NOT EXISTS (SELECT 1 FROM MenuOfferingPrice p WHERE p.offering = o)
			""")
	long countOfferingsOnNoPriceList();
}
