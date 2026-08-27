package com.zbs.de.model.dto.menu;

import java.util.List;

/**
 * Several rows that look like one dish.
 *
 * <h2>Why this is a report and not an action</h2>
 *
 * 368 selectable rows in the live catalogue hold 238 distinct names; twenty-one
 * desserts exist five times each because a menu item may have only one parent.
 * Collapsing them is the point of the offering model — but <em>which</em> rows
 * are really the same dish is a judgement, not a fact.
 *
 * <p>
 * Four of the forty-eight duplicated names span two categories: Channa Papadi
 * Chaat is in Reception and in Appetisers, Fruit Punch is in Reception and in
 * Drinks. Those almost certainly are one dish offered twice. Others may not be —
 * a Kheer plated and a Kheer in a set menu can be priced differently on purpose,
 * and merging them silently changes what somebody is charged with no way to tell
 * afterwards.
 *
 * <p>
 * So this hands a person the question, with the prices beside it so they can
 * answer it, and the merge is theirs to make one dish at a time.
 */
public class DtoDuplicateDish {

	private String txtName;

	/** Every row carrying this name, with where each is offered and at what. */
	private List<DtoMenuOffering> offerings;

	public DtoDuplicateDish(String txtName, List<DtoMenuOffering> offerings) {
		this.txtName = txtName;
		this.offerings = offerings;
	}

	public String getTxtName() {
		return txtName;
	}

	public List<DtoMenuOffering> getOfferings() {
		return offerings;
	}

	public int getNumCopies() {
		return offerings == null ? 0 : offerings.size();
	}
}
