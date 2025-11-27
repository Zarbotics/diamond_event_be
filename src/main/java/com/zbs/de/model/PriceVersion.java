package com.zbs.de.model;

import java.util.Date;

import com.zbs.de.util.enums.EnmPriceVersionStatus;

import jakarta.persistence.*;

@Entity
@Table(name = "price_version")
@NamedQuery(name = "PriceVersion.findAll", query = "SELECT a FROM PriceVersion a")
public class PriceVersion extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_price_version_id")
	private Long serPriceVersionId;

	@Column(name = "txt_name")
	private String txtName;

	@Column(name = "dte_effective_from")
	private Date dteEffectiveFrom;

	@Column(name = "dte_effective_to")
	private Date dteEffectiveTo;

	@Enumerated(EnumType.STRING)
	@Column(name = "price_version_status")
	private EnmPriceVersionStatus priceVersionStatus; // ITEM|ROLE|BUNDLE|COMBINATION|STATION|TYPE

	public PriceVersion() {
		super();
	}

	public Long getSerPriceVersionId() {
		return serPriceVersionId;
	}

	public void setSerPriceVersionId(Long serPriceVersionId) {
		this.serPriceVersionId = serPriceVersionId;
	}

	public String getTxtName() {
		return txtName;
	}

	public void setTxtName(String txtName) {
		this.txtName = txtName;
	}

	public Date getDteEffectiveFrom() {
		return dteEffectiveFrom;
	}

	public void setDteEffectiveFrom(Date dteEffectiveFrom) {
		this.dteEffectiveFrom = dteEffectiveFrom;
	}

	public Date getDteEffectiveTo() {
		return dteEffectiveTo;
	}

	public void setDteEffectiveTo(Date dteEffectiveTo) {
		this.dteEffectiveTo = dteEffectiveTo;
	}

	public EnmPriceVersionStatus getPriceVersionStatus() {
		return priceVersionStatus;
	}

	public void setPriceVersionStatus(EnmPriceVersionStatus priceVersionStatus) {
		this.priceVersionStatus = priceVersionStatus;
	}

}
