package com.zbs.de.model.dto;

import java.util.List;

public class DtoDecorExtrasMaster {

	private Integer serExtrasId;
	private String txtExtrasCode;
	private String txtExtrasName;
	private Boolean blnIsActive;
	private List<DtoDecorExtrasOption> decorExtrasOptions;

	public Integer getSerExtrasId() {
		return serExtrasId;
	}

	public void setSerExtrasId(Integer serExtrasId) {
		this.serExtrasId = serExtrasId;
	}

	public String getTxtExtrasCode() {
		return txtExtrasCode;
	}

	public void setTxtExtrasCode(String txtExtrasCode) {
		this.txtExtrasCode = txtExtrasCode;
	}

	public String getTxtExtrasName() {
		return txtExtrasName;
	}

	public void setTxtExtrasName(String txtExtrasName) {
		this.txtExtrasName = txtExtrasName;
	}

	public Boolean getBlnIsActive() {
		return blnIsActive;
	}

	public void setBlnIsActive(Boolean blnIsActive) {
		this.blnIsActive = blnIsActive;
	}

	public List<DtoDecorExtrasOption> getDecorExtrasOptions() {
		return decorExtrasOptions;
	}

	public void setDecorExtrasOptions(List<DtoDecorExtrasOption> decorExtrasOptions) {
		this.decorExtrasOptions = decorExtrasOptions;
	}

}
