package com.zbs.de.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

import org.hibernate.annotations.DynamicInsert;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@DynamicInsert
@Data
@Table(name = "decor_category_property_master")
@NamedQuery(name = "DecorCategoryPropertyMaster.findAll", query = "SELECT a FROM DecorCategoryPropertyMaster a")
public class DecorCategoryPropertyMaster extends BaseEntity implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_property_id")
	private Integer serPropertyId;

	@Column(name = "txt_property_code")
	private String txtPropertyCode;

	@Column(name = "txt_property_name")
	private String txtPropertyName;

	@Column(name = "txt_input_type") // dropdown, color, text, etc
	private String txtInputType;

	@Column(name = "txt_remarks")
	private String txtRemarks;

	@Column(name = "bln_is_required")
	private Boolean blnIsRequired;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_decor_category_id")
	@JsonBackReference
	private DecorCategoryMaster decorCategoryMaster;

	@OneToMany(mappedBy = "decorCategoryProperty", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<DecorCategoryPropertyValue> propertyValues;

	public Integer getSerPropertyId() {
		return serPropertyId;
	}

	public void setSerPropertyId(Integer serPropertyId) {
		this.serPropertyId = serPropertyId;
	}

	public String getTxtPropertyName() {
		return txtPropertyName;
	}

	public void setTxtPropertyName(String txtPropertyName) {
		this.txtPropertyName = txtPropertyName;
	}

	public String getTxtInputType() {
		return txtInputType;
	}

	public void setTxtInputType(String txtInputType) {
		this.txtInputType = txtInputType;
	}

	public DecorCategoryMaster getDecorCategoryMaster() {
		return decorCategoryMaster;
	}

	public void setDecorCategoryMaster(DecorCategoryMaster decorCategoryMaster) {
		this.decorCategoryMaster = decorCategoryMaster;
	}

	public List<DecorCategoryPropertyValue> getPropertyValues() {
		return propertyValues;
	}

	public void setPropertyValues(List<DecorCategoryPropertyValue> propertyValues) {
		this.propertyValues = propertyValues;
	}

	public String getTxtRemarks() {
		return txtRemarks;
	}

	public void setTxtRemarks(String txtRemarks) {
		this.txtRemarks = txtRemarks;
	}

	public Boolean getBlnIsRequired() {
		return blnIsRequired;
	}

	public void setBlnIsRequired(Boolean blnIsRequired) {
		this.blnIsRequired = blnIsRequired;
	}

	public String getTxtPropertyCode() {
		return txtPropertyCode;
	}

	public void setTxtPropertyCode(String txtPropertyCode) {
		this.txtPropertyCode = txtPropertyCode;
	}

}
