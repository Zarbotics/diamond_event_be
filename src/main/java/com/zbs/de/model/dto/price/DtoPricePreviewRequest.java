package com.zbs.de.model.dto.price;

import java.util.List;

public class DtoPricePreviewRequest {
	private Long priceVersionId;
	private Integer guestCount;
	private List<DtoPreviewSelection> selections;
	public List<DtoPricePreviewLine> lines;
	public Double total;

	public DtoPricePreviewRequest() {
		super();
		// TODO Auto-generated constructor stub
	}

	public DtoPricePreviewRequest(Long priceVersionId, Integer guestCount, List<DtoPreviewSelection> selections) {
		super();
		this.priceVersionId = priceVersionId;
		this.guestCount = guestCount;
		this.selections = selections;
	}

	public DtoPricePreviewRequest(Long priceVersionId, Integer guestCount, List<DtoPreviewSelection> selections,
			List<DtoPricePreviewLine> lines, Double total) {
		super();
		this.priceVersionId = priceVersionId;
		this.guestCount = guestCount;
		this.selections = selections;
		this.lines = lines;
		this.total = total;
	}

	public Long getPriceVersionId() {
		return priceVersionId;
	}

	public void setPriceVersionId(Long priceVersionId) {
		this.priceVersionId = priceVersionId;
	}

	public Integer getGuestCount() {
		return guestCount;
	}

	public void setGuestCount(Integer guestCount) {
		this.guestCount = guestCount;
	}

	public List<DtoPreviewSelection> getSelections() {
		return selections;
	}

	public void setSelections(List<DtoPreviewSelection> selections) {
		this.selections = selections;
	}

	public List<DtoPricePreviewLine> getLines() {
		return lines;
	}

	public void setLines(List<DtoPricePreviewLine> lines) {
		this.lines = lines;
	}

	public Double getTotal() {
		return total;
	}

	public void setTotal(Double total) {
		this.total = total;
	}

}