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

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_property_id")
	private DecorCategoryPropertyMaster decorCategoryProperty;

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

}
