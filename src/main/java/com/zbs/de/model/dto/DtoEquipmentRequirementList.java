package com.zbs.de.model.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Everything one event needs on the day.
 *
 * <p>
 * Carries the guest and table counts it was worked out from, so the reader can
 * see what the arithmetic assumed without opening another screen — and an
 * explanation instead of an empty list when there is nothing to work out yet.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DtoEquipmentRequirementList {

	private Integer serEventMasterId;
	private Integer numGuests;
	private Integer numTables;

	/**
	 * Why the list is empty, when it is.
	 *
	 * <p>
	 * A sheet reading "0 dinner plates" looks like a calculation that ran and
	 * found nothing, which is a different and more alarming thing than a
	 * booking nobody has told how many people are coming.
	 */
	private String txtExplanation;

	private List<DtoEquipmentLine> lines = new ArrayList<>();

	public Integer getSerEventMasterId() {
		return serEventMasterId;
	}

	public void setSerEventMasterId(Integer serEventMasterId) {
		this.serEventMasterId = serEventMasterId;
	}

	public Integer getNumGuests() {
		return numGuests;
	}

	public void setNumGuests(Integer numGuests) {
		this.numGuests = numGuests;
	}

	public Integer getNumTables() {
		return numTables;
	}

	public void setNumTables(Integer numTables) {
		this.numTables = numTables;
	}

	public String getTxtExplanation() {
		return txtExplanation;
	}

	public void setTxtExplanation(String txtExplanation) {
		this.txtExplanation = txtExplanation;
	}

	public List<DtoEquipmentLine> getLines() {
		return lines;
	}

	public void setLines(List<DtoEquipmentLine> lines) {
		this.lines = lines;
	}
}
