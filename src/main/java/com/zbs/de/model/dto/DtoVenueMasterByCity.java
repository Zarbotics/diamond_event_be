package com.zbs.de.model.dto;

import java.util.List;

public class DtoVenueMasterByCity {
	private Integer serCityId;
	private String txtCityName;
	private String txtCityCode;
	private List<DtoVenueMaster> venueMasters;

	public Integer getSerCityId() {
		return serCityId;
	}

	public void setSerCityId(Integer serCityId) {
		this.serCityId = serCityId;
	}

	public String getTxtCityName() {
		return txtCityName;
	}

	public void setTxtCityName(String txtCityName) {
		this.txtCityName = txtCityName;
	}

	public String getTxtCityCode() {
		return txtCityCode;
	}

	public void setTxtCityCode(String txtCityCode) {
		this.txtCityCode = txtCityCode;
	}

	public List<DtoVenueMaster> getVenueMasters() {
		return venueMasters;
	}

	public void setVenueMasters(List<DtoVenueMaster> venueMasters) {
		this.venueMasters = venueMasters;
	}

}
