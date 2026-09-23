package com.zbs.de.model.dto.menu;

import java.math.BigDecimal;

/**
 * One place a dish is offered, and what it costs there.
 *
 * <p>
 * Built in the query rather than mapped from the entity, so that asking "where
 * else is this dish on the menu" costs one statement and returns eight fields
 * instead of the whole item graph twice over. The section's path travels with
 * it because the answer a person needs is "Desserts › Dessert Buffet", not an
 * id.
 */
public class DtoMenuOffering {

	private Long serOfferingId;

	private Long serMenuItemId;
	private String txtDishName;

	private Long serSectionId;
	private String txtSectionName;

	/** The section's place in the menu, so the screen can say where that is. */
	private String txtSectionPath;

	private BigDecimal numPrice;

	/** {@code PER_GUEST}, {@code FLAT}, or null where nobody has said. */
	private String txtPriceRule;

	private Integer numPosition;

	public DtoMenuOffering(Long serOfferingId, Long serMenuItemId, String txtDishName, Long serSectionId,
			String txtSectionName, String txtSectionPath, BigDecimal numPrice, String txtPriceRule,
			Integer numPosition) {
		this.serOfferingId = serOfferingId;
		this.serMenuItemId = serMenuItemId;
		this.txtDishName = txtDishName;
		this.serSectionId = serSectionId;
		this.txtSectionName = txtSectionName;
		this.txtSectionPath = txtSectionPath;
		this.numPrice = numPrice;
		this.txtPriceRule = txtPriceRule;
		this.numPosition = numPosition;
	}

	public Long getSerOfferingId() {
		return serOfferingId;
	}

	public Long getSerMenuItemId() {
		return serMenuItemId;
	}

	public String getTxtDishName() {
		return txtDishName;
	}

	public Long getSerSectionId() {
		return serSectionId;
	}

	public String getTxtSectionName() {
		return txtSectionName;
	}

	public String getTxtSectionPath() {
		return txtSectionPath;
	}

	public BigDecimal getNumPrice() {
		return numPrice;
	}

	public String getTxtPriceRule() {
		return txtPriceRule;
	}

	public Integer getNumPosition() {
		return numPosition;
	}
}
