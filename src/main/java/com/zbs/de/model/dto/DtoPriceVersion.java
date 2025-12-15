package com.zbs.de.model.dto;

public class DtoPriceVersion {
	private Long serPriceVersionId;
	private String txtName;
	private String dteEffectiveFrom;
	private String dteEffectiveTo;
	private String txtStatus;

	public DtoPriceVersion() {
		super();
		// TODO Auto-generated constructor stub
	}

	public DtoPriceVersion(Long idserPriceVersionId, String txtName, String dteEffectiveFrom, String dteEffectiveTo,
			String txtStatus) {
		super();
		this.serPriceVersionId = idserPriceVersionId;
		this.txtName = txtName;
		this.dteEffectiveFrom = dteEffectiveFrom;
		this.dteEffectiveTo = dteEffectiveTo;
		this.txtStatus = txtStatus;
	}

	public Long getSerPriceVersionId() {
		return serPriceVersionId;
	}

	public void setSerPriceVersionId(Long idserPriceVersionId) {
		this.serPriceVersionId = idserPriceVersionId;
	}

	public String getTxtName() {
		return txtName;
	}

	public void setTxtName(String txtName) {
		this.txtName = txtName;
	}

	public String getDteEffectiveFrom() {
		return dteEffectiveFrom;
	}

	public void setDteEffectiveFrom(String dteEffectiveFrom) {
		this.dteEffectiveFrom = dteEffectiveFrom;
	}

	public String getDteEffectiveTo() {
		return dteEffectiveTo;
	}

	public void setDteEffectiveTo(String dteEffectiveTo) {
		this.dteEffectiveTo = dteEffectiveTo;
	}

	public String getTxtStatus() {
		return txtStatus;
	}

	public void setTxtStatus(String txtStatus) {
		this.txtStatus = txtStatus;
	}

}
