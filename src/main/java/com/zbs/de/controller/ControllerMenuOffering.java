package com.zbs.de.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zbs.de.model.dto.menu.DtoDuplicateDish;
import com.zbs.de.model.dto.menu.DtoMenuOffering;
import com.zbs.de.repository.RepositoryMenuOffering;

/**
 * Where dishes are offered, and what the menu still needs somebody to decide.
 *
 * <h2>Why these are GETs</h2>
 *
 * §15.4's rule: new endpoints are REST, no exceptions, including "just this one
 * to match its neighbour". Every one of these is a read, so every one is a
 * {@code GET} — cacheable, safely retried by anything in between, and
 * answerable from the method line alone. The three hundred and thirty POSTs
 * next door are what that rule exists to stop growing.
 *
 * <h2>What they are for</h2>
 *
 * The menu editor. Two of the three are not lists of data so much as lists of
 * decisions: which duplicated dishes are really the same dish, and which priced
 * items have never said whether the price is per head. Both were previously
 * invisible — the first because nothing could express a dish being offered
 * twice, the second because the code quietly assumed an answer.
 */
@RestController
@RequestMapping("/menu/offerings")
public class ControllerMenuOffering {

	@Autowired
	private RepositoryMenuOffering repositoryMenuOffering;

	/**
	 * Everywhere one dish appears.
	 *
	 * <p>
	 * The question the offering model exists to make askable. Before it, the
	 * answer was "in exactly one place, by construction" — which is why the
	 * catalogue grew five chocolate brownies.
	 */
	@GetMapping("/dish/{dishId}")
	public ResponseEntity<List<DtoMenuOffering>> offeringsOfDish(@PathVariable Long dishId) {
		return ResponseEntity.ok(repositoryMenuOffering.findOfferingsOfDish(dishId));
	}

	/**
	 * Dishes that share a name, with every placement and price beside them.
	 *
	 * <p>
	 * A report, deliberately, and not a merge. Which rows are really one dish is
	 * a judgement: four of the forty-eight duplicated names span two categories
	 * and almost certainly are one dish offered twice, while a Kheer plated and a
	 * Kheer in a set menu may be priced differently on purpose. Merging silently
	 * changes what somebody is charged and leaves no way to tell afterwards, so
	 * the decision goes to a person with the prices in front of them.
	 */
	@GetMapping("/duplicates")
	public ResponseEntity<List<DtoDuplicateDish>> duplicates() {
		List<String> names = repositoryMenuOffering.findDuplicateDishNames().stream()
				.map(row -> (String) row[0])
				.toList();

		if (names.isEmpty()) {
			return ResponseEntity.ok(List.of());
		}

		/*
		 * One query for every placement of every duplicated name, then grouped
		 * here. The alternative is a query per name, which on a catalogue with
		 * forty-eight of them is forty-eight round trips to render one screen.
		 */
		Map<String, List<DtoMenuOffering>> byName = new LinkedHashMap<>();
		names.forEach(name -> byName.put(name, new ArrayList<>()));

		repositoryMenuOffering.findOfferingsOfDishesNamed(names)
				.forEach(offering -> byName.get(offering.getTxtDishName()).add(offering));

		return ResponseEntity.ok(byName.entrySet().stream()
				.map(entry -> new DtoDuplicateDish(entry.getKey(), entry.getValue()))
				.toList());
	}

	/**
	 * Priced dishes that have never said whether the price is per head.
	 *
	 * <p>
	 * Twenty of them in the live catalogue, twelve carrying money. They are
	 * charged per guest regardless, because that is what the pricing code has
	 * always assumed — a Grazing Bar at £2.50 becoming £750 at three hundred
	 * guests. On the evidence those figures are right, which is exactly why this
	 * list matters: they are right by luck rather than by decision, and the first
	 * genuinely flat item priced at £250 becomes £75,000 the same silent way.
	 */
	@GetMapping("/unstated-price-rule")
	public ResponseEntity<List<DtoMenuOffering>> unstatedPriceRule() {
		return ResponseEntity.ok(repositoryMenuOffering.findPricedOfferingsWithNoRule());
	}
}
