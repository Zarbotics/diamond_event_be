package com.zbs.de.model;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

import java.io.Serializable;

import org.hibernate.annotations.DynamicInsert;

@Entity
@DynamicInsert
@Data
@Table(name = "decor_category_property_value")
@NamedQuery(name = "DecorCategoryPropertyValue.findAll", query = "SELECT a FROM DecorCategoryPropertyValue a")
public class DecorCategoryPropertyValue extends BaseEntity implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_property_value_id")
	private Integer serPropertyValueId;

	@Column(name = "txt_property_value")
	private String txtPropertyValue;

	@Column(name = "bln_is_document")
	private Boolean blnIsDocument;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_property_id")
	private DecorCategoryPropertyMaster decorCategoryProperty;

	@OneToOne
	@JoinColumn(name = "ser_document_id")
	private DecorCategoryPropertyValueDocument decorCategoryPropertyValueDocument;

	public Integer getSerPropertyValueId() {
		return serPropertyValueId;
	}

	public void setSerPropertyValueId(Integer serPropertyValueId) {
		this.serPropertyValueId = serPropertyValueId;
	}

	public String getTxtPropertyValue() {
		return txtPropertyValue;
	}

	public void setTxtPropertyValue(String txtPropertyValue) {
		this.txtPropertyValue = txtPropertyValue;
	}

	public DecorCategoryPropertyMaster getDecorCategoryProperty() {
		return decorCategoryProperty;
	}

	public void setDecorCategoryProperty(DecorCategoryPropertyMaster decorCategoryProperty) {
		this.decorCategoryProperty = decorCategoryProperty;
	}

	public DecorCategoryPropertyValueDocument getDecorCategoryPropertyValueDocument() {
		return decorCategoryPropertyValueDocument;
	}

	public void setDecorCategoryPropertyValueDocument(
			DecorCategoryPropertyValueDocument decorCategoryPropertyValueDocument) {
		this.decorCategoryPropertyValueDocument = decorCategoryPropertyValueDocument;
	}

	public Boolean getBlnIsDocument() {
		return blnIsDocument;
	}

	public void setBlnIsDocument(Boolean blnIsDocument) {
		this.blnIsDocument = blnIsDocument;
	}

}
