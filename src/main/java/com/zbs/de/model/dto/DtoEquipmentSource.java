package com.zbs.de.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Why one line of an equipment list says the number it says.
 *
 * <p>
 * "Chicken Karahi — 1 per guest x 300 guests — 300". A number with no
 * explanation cannot be argued with; somebody who thinks it should be 280
 * needs to see which rule to change.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DtoEquipmentSource {

	/** The dish that asked, or "Every event". */
	private String txtSourceName;

	/** The arithmetic, in words. */
	private String txtReason;

	/** What this source contributed, before the line was rounded up. */
	private String numContributed;

	public String getTxtSourceName() {
		return txtSourceName;
	}

	public void setTxtSourceName(String txtSourceName) {
		this.txtSourceName = txtSourceName;
	}

	public String getTxtReason() {
		return txtReason;
	}

	public void setTxtReason(String txtReason) {
		this.txtReason = txtReason;
	}

	public String getNumContributed() {
		return numContributed;
	}

	public void setNumContributed(String numContributed) {
		this.numContributed = numContributed;
	}
}
