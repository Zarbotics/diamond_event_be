package com.zbs.de.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.MenuOffering;

/**
 * Where dishes are offered.
 *
 * <p>
 * Stage M2 of §17.3, so this is thin on purpose: enough to assert that the
 * table, the backfill and the entity agree with one another, and enough to
 * answer the two questions the later stages are built on. The queries a menu
 * screen will need are written in the stage that introduces them, against
 * requirements rather than in anticipation of them.
 */
@Repository("repositoryMenuOffering")
public interface RepositoryMenuOffering extends JpaRepository<MenuOffering, Long> {

	/** What is offered in one section, in the order it should be shown. */
	List<MenuOffering> findBySection_SerMenuItemIdOrderByNumPositionAsc(Long sectionId);

	/**
	 * Everywhere one dish is offered.
	 *
	 * <p>
	 * The question the whole stage exists to make answerable. Today it returns
	 * exactly one row for every dish, because the backfill is an identity
	 * mapping; after a merge it returns the several places the surviving dish
	 * appears, and a rename becomes one edit rather than five.
	 */
	List<MenuOffering> findByMenuItem_SerMenuItemId(Long menuItemId);

	/**
	 * Selectable dishes that no offering covers.
	 *
	 * <p>
	 * Expected to be zero after the backfill, and expected to stay zero: an item
	 * a customer can choose but which is offered nowhere is invisible, which is
	 * the fault that already hides four soups behind a dessert. Reported rather
	 * than enforced, because the answer is only meaningful once the create paths
	 * maintain offerings — which is M3, not this stage.
	 */
	@Query("""
			SELECT COUNT(i) FROM MenuItem i
			WHERE i.blnIsSelectable = true
			  AND i.parent IS NOT NULL
			  AND NOT EXISTS (SELECT 1 FROM MenuOffering o WHERE o.menuItem = i)
			""")
	long countSelectableItemsWithNoOffering();

	/**
	 * Dishes that share a name and are candidates for merging.
	 *
	 * <p>
	 * Deliberately a report and not an action. Deciding that two rows called
	 * "Kheer" are the same dish is a judgement — one may be a plated dessert and
	 * the other part of a set menu, priced differently on purpose — and a wrong
	 * guess silently changes what a customer is charged. So this hands the
	 * question to a person in the admin screen rather than answering it.
	 */
	@Query("""
			SELECT i.txtName, COUNT(i) FROM MenuItem i
			WHERE i.blnIsSelectable = true
			GROUP BY i.txtName
			HAVING COUNT(i) > 1
			ORDER BY COUNT(i) DESC, i.txtName ASC
			""")
	List<Object[]> findDuplicateDishNames();

	/**
	 * Priced offerings that have not said whether the price is per guest.
	 *
	 * <p>
	 * The number M3 has to drive to zero. Until it does,
	 * {@code getMenuWithPrices} multiplies by the guest count anyway — so every
	 * one of these is a figure somebody could be quoted without anyone having
	 * decided it.
	 */
	@Query("SELECT COUNT(o) FROM MenuOffering o WHERE o.txtPriceRule IS NULL AND o.numPrice IS NOT NULL")
	long countPricedOfferingsWithNoRule();
}
