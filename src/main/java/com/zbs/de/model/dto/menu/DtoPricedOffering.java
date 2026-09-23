package com.zbs.de.model.dto.menu;

import java.math.BigDecimal;

/**
 * What one dish costs in one section, on one price list.
 *
 * <p>
 * Stage M5b of §17.3. Built in the query rather than mapped from the entity,
 * because the only use for it is to be put in a map and looked up: pricing a
 * menu needs every offering's figure at once, and fetching four hundred
 * {@code MenuOfferingPrice} entities — each dragging its offering, its dish, its
 * section and its version behind it — to read two numbers is an expensive way to
 * build a lookup table.
 *
 * <p>
 * The dish and the section together are the key, not the offering id. That is
 * what the walk has in its hands when it needs a price: it is standing on an
 * item, inside a section, and does not know the offering exists.
 */
public class DtoPricedOffering {

	private final Long serMenuItemId;
	private final Long serSectionId;
	private final BigDecimal numPrice;

	/** {@code PER_GUEST}, {@code FLAT}, or null where nobody has said. */
	private final String txtPriceRule;

	public DtoPricedOffering(Long serMenuItemId, Long serSectionId, BigDecimal numPrice, String txtPriceRule) {
		this.serMenuItemId = serMenuItemId;
		this.serSectionId = serSectionId;
		this.numPrice = numPrice;
		this.txtPriceRule = txtPriceRule;
	}

	public Long getSerMenuItemId() {
		return serMenuItemId;
	}

	public Long getSerSectionId() {
		return serSectionId;
	}

	public BigDecimal getNumPrice() {
		return numPrice;
	}

	public String getTxtPriceRule() {
		return txtPriceRule;
	}
}
