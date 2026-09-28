package com.zbs.de.model.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * What a booking would come to, worked out without saving it.
 *
 * <h2>Why this exists</h2>
 *
 * So that there is one implementation of what anything costs.
 *
 * <p>
 * The control panel adds the booking up itself, in
 * {@code eventPayload.js}, so that the total pinned beside the form moves while
 * somebody is changing the menu — which is the reason it is pinned. That is a
 * second set of pricing rules, written at a different time from the server's,
 * and two sets of rules that both look right is the whole problem P1 to P3 have
 * been unwinding.
 *
 * <p>
 * The engine never needed a saved booking to price one: it walks the entity it
 * is handed. So the server can answer "what would this come to" from a payload,
 * write nothing, and the screen can stop doing arithmetic.
 *
 * <h2>Why the lines come back too</h2>
 *
 * A total nobody can take apart is a total nobody can defend to a customer who
 * queries it. Each line carries what it was, what it was charged at, what the
 * catalogue makes it, and the reason in words.
 */
public class DtoEventPricePreview {

	private BigDecimal numFood;
	private BigDecimal numDecor;
	private BigDecimal numExtras;
	private BigDecimal numServices;
	private BigDecimal numVat;
	private BigDecimal numSubtotal;
	private BigDecimal numDiscount;
	private BigDecimal numTotal;

	/**
	 * Why this figure must not be charged, where it must not.
	 *
	 * <p>
	 * A per-guest price multiplied by a guest count nobody has entered is zero,
	 * and a quote of £0.00 looks exactly like a quote. Empty means the figure
	 * is sound.
	 */
	private List<String> txtCannotPrice;

	private List<DtoEventPriceLine> lines;

	public BigDecimal getNumFood() {
		return numFood;
	}

	public void setNumFood(BigDecimal numFood) {
		this.numFood = numFood;
	}

	public BigDecimal getNumDecor() {
		return numDecor;
	}

	public void setNumDecor(BigDecimal numDecor) {
		this.numDecor = numDecor;
	}

	public BigDecimal getNumExtras() {
		return numExtras;
	}

	public void setNumExtras(BigDecimal numExtras) {
		this.numExtras = numExtras;
	}

	public BigDecimal getNumServices() {
		return numServices;
	}

	public void setNumServices(BigDecimal numServices) {
		this.numServices = numServices;
	}

	public BigDecimal getNumVat() {
		return numVat;
	}

	public void setNumVat(BigDecimal numVat) {
		this.numVat = numVat;
	}

	public BigDecimal getNumSubtotal() {
		return numSubtotal;
	}

	public void setNumSubtotal(BigDecimal numSubtotal) {
		this.numSubtotal = numSubtotal;
	}

	public BigDecimal getNumDiscount() {
		return numDiscount;
	}

	public void setNumDiscount(BigDecimal numDiscount) {
		this.numDiscount = numDiscount;
	}

	public BigDecimal getNumTotal() {
		return numTotal;
	}

	public void setNumTotal(BigDecimal numTotal) {
		this.numTotal = numTotal;
	}

	public List<String> getTxtCannotPrice() {
		return txtCannotPrice;
	}

	public void setTxtCannotPrice(List<String> txtCannotPrice) {
		this.txtCannotPrice = txtCannotPrice;
	}

	public List<DtoEventPriceLine> getLines() {
		return lines;
	}

	public void setLines(List<DtoEventPriceLine> lines) {
		this.lines = lines;
	}

	/** One line of the working. */
	public static class DtoEventPriceLine {

		private String txtSection;
		private String txtDescription;
		private BigDecimal numUnitPrice;
		private String enmBasis;
		private BigDecimal numQuantity;
		private BigDecimal numLineTotal;
		private BigDecimal numCatalogueTotal;
		private Boolean blnIsOverridden;
		private BigDecimal numVat;
		private String txtReason;

		public String getTxtSection() {
			return txtSection;
		}

		public void setTxtSection(String txtSection) {
			this.txtSection = txtSection;
		}

		public String getTxtDescription() {
			return txtDescription;
		}

		public void setTxtDescription(String txtDescription) {
			this.txtDescription = txtDescription;
		}

		public BigDecimal getNumUnitPrice() {
			return numUnitPrice;
		}

		public void setNumUnitPrice(BigDecimal numUnitPrice) {
			this.numUnitPrice = numUnitPrice;
		}

		public String getEnmBasis() {
			return enmBasis;
		}

		public void setEnmBasis(String enmBasis) {
			this.enmBasis = enmBasis;
		}

		public BigDecimal getNumQuantity() {
			return numQuantity;
		}

		public void setNumQuantity(BigDecimal numQuantity) {
			this.numQuantity = numQuantity;
		}

		public BigDecimal getNumLineTotal() {
			return numLineTotal;
		}

		public void setNumLineTotal(BigDecimal numLineTotal) {
			this.numLineTotal = numLineTotal;
		}

		public BigDecimal getNumCatalogueTotal() {
			return numCatalogueTotal;
		}

		public void setNumCatalogueTotal(BigDecimal numCatalogueTotal) {
			this.numCatalogueTotal = numCatalogueTotal;
		}

		public Boolean getBlnIsOverridden() {
			return blnIsOverridden;
		}

		public void setBlnIsOverridden(Boolean blnIsOverridden) {
			this.blnIsOverridden = blnIsOverridden;
		}

		public BigDecimal getNumVat() {
			return numVat;
		}

		public void setNumVat(BigDecimal numVat) {
			this.numVat = numVat;
		}

		public String getTxtReason() {
			return txtReason;
		}

		public void setTxtReason(String txtReason) {
			this.txtReason = txtReason;
		}
	}
}
