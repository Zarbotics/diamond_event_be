package com.zbs.de.model.dto;

public class DtoMenuPriceCalulationFields {
	private Integer numGuests;
	private Integer numTables;

	/**
	 * The day the event is on, {@code dd-MM-yyyy}.
	 *
	 * <h4>Why the menu needs to know</h4>
	 *
	 * Stage M5b of §17.3: prices belong to a dated list, and the list that
	 * applies is the one effective on the <em>event's</em> date rather than
	 * today's. A booking taken in March for a September wedding is quoted from
	 * September's prices if a September list exists, which is the entire point of
	 * preparing one in advance — and it is the question staff actually ask.
	 *
	 * <p>
	 * Absent is allowed and means today. The journey prices a menu before the
	 * customer has chosen a day, and showing no prices at all until they do would
	 * be a worse answer than showing this year's.
	 */
	private String dteEventDate;

	public String getDteEventDate() {
		return dteEventDate;
	}

	public void setDteEventDate(String dteEventDate) {
		this.dteEventDate = dteEventDate;
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

}
