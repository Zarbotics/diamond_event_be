package com.zbs.de.model.dto;

import java.math.BigDecimal;

public class DtoEventServicesMaster {
	private Integer serServiceId;
	private String txtServiceName;
	private BigDecimal numPrice;
	private Boolean blnIsAvtive;

	public Integer getSerServiceId() {
		return serServiceId;
	}

	public void setSerServiceId(Integer serServiceId) {
		this.serServiceId = serServiceId;
	}

	public String getTxtServiceName() {
		return txtServiceName;
	}

	public void setTxtServiceName(String txtServiceName) {
		this.txtServiceName = txtServiceName;
	}

	public BigDecimal getNumPrice() {
		return numPrice;
	}

	public void setNumPrice(BigDecimal numPrice) {
		this.numPrice = numPrice;
	}

	public Boolean getBlnIsAvtive() {
		return blnIsAvtive;
	}

	public void setBlnIsAvtive(Boolean blnIsAvtive) {
		this.blnIsAvtive = blnIsAvtive;
	}

}
