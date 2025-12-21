package com.zbs.de.model;

import java.util.Date;
import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

	@Column(name = "txt_version_code", unique = true, length = 50)
	private String txtVersionCode; // Auto-generated: PV-1001

	@Column(name = "txt_name")
	private String txtName;

	@Column(name = "txt_description")
	private String txtDescription;

	@Column(name = "dte_effective_from")
	private Date dteEffectiveFrom;

	@Column(name = "dte_effective_to")
	private Date dteEffectiveTo;

	@Column(name = "bln_is_default")
	private Boolean blnIsDefault = false;

	@Column(name = "num_priority")
	private Integer numPriority = 1; // Higher = takes precedence

	@Enumerated(EnumType.STRING)
	@Column(name = "price_version_status")
	private EnmPriceVersionStatus priceVersionStatus; // ITEM|ROLE|BUNDLE|COMBINATION|STATION|TYPE

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "metadata", columnDefinition = "jsonb")
	private Map<String, Object> metadata;

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

	public String getTxtVersionCode() {
		return txtVersionCode;
	}

	public void setTxtVersionCode(String txtVersionCode) {
		this.txtVersionCode = txtVersionCode;
	}

	public String getTxtDescription() {
		return txtDescription;
	}

	public void setTxtDescription(String txtDescription) {
		this.txtDescription = txtDescription;
	}

	public Boolean getBlnIsDefault() {
		return blnIsDefault;
	}

	public void setBlnIsDefault(Boolean blnIsDefault) {
		this.blnIsDefault = blnIsDefault;
	}

	public Integer getNumPriority() {
		return numPriority;
	}

	public void setNumPriority(Integer numPriority) {
		this.numPriority = numPriority;
	}

	public Map<String, Object> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}

}
