package com.zbs.de.model.dto;

import java.util.List;

/**
 * Which moments a kind of event's running order asks for.
 *
 * <p>
 * The whole set, not a change to it. Working out which to add and which to
 * remove is two chances to get it wrong for no gain — the screen knows what it
 * wants the answer to be, so it says so.
 */
public class DtoEventTypeRunningOrder {

	private Integer serEventTypeId;

	/** Field names on `event_running_order`, e.g. "txtNikah". */
	private List<String> txtRunningOrderMoments;

	public Integer getSerEventTypeId() {
		return serEventTypeId;
	}

	public void setSerEventTypeId(Integer serEventTypeId) {
		this.serEventTypeId = serEventTypeId;
	}

	public List<String> getTxtRunningOrderMoments() {
		return txtRunningOrderMoments;
	}

	public void setTxtRunningOrderMoments(List<String> txtRunningOrderMoments) {
		this.txtRunningOrderMoments = txtRunningOrderMoments;
	}
}
