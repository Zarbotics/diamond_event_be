package com.zbs.de.model.dto;

import java.math.BigDecimal;
import java.util.List;

public class DtoVenueMasterDetail {
	private Integer serVenueMasterDetailId;
	private String txtHallCode;
	private String txtHallName;
	private Integer numCapacity;
	private String txtCapacity;
	private BigDecimal numPrice;
	private Boolean blnIsActive;
	private List<DtoVenueMasterDetailDocument> documents;
	private List<String> txtDocuments;

	public Integer getSerVenueMasterDetailId() {
		return serVenueMasterDetailId;
	}

	public void setSerVenueMasterDetailId(Integer serVenueMasterDetailId) {
		this.serVenueMasterDetailId = serVenueMasterDetailId;
	}

	public String getTxtHallCode() {
		return txtHallCode;
	}

	public void setTxtHallCode(String txtHallCode) {
		this.txtHallCode = txtHallCode;
	}

	public String getTxtHallName() {
		return txtHallName;
	}

	public void setTxtHallName(String txtHallName) {
		this.txtHallName = txtHallName;
	}

	public Integer getNumCapacity() {
		return numCapacity;
	}

	public void setNumCapacity(Integer numCapacity) {
		this.numCapacity = numCapacity;
	}

	public String getTxtCapacity() {
		return txtCapacity;
	}

	public void setTxtCapacity(String txtCapacity) {
		this.txtCapacity = txtCapacity;
	}

	public BigDecimal getNumPrice() {
		return numPrice;
	}

	public void setNumPrice(BigDecimal numPrice) {
		this.numPrice = numPrice;
	}

	public List<DtoVenueMasterDetailDocument> getDocuments() {
		return documents;
	}

	public void setDocuments(List<DtoVenueMasterDetailDocument> documents) {
		this.documents = documents;
	}

	public Boolean getBlnIsActive() {
		return blnIsActive;
	}

	public void setBlnIsActive(Boolean blnIsActive) {
		this.blnIsActive = blnIsActive;
	}

	public List<String> getTxtDocuments() {
		return txtDocuments;
	}

	public void setTxtDocuments(List<String> txtDocuments) {
		this.txtDocuments = txtDocuments;
	}

}
