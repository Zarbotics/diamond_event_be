package com.zbs.de.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Id;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

import org.hibernate.annotations.DynamicInsert;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@DynamicInsert
@Data
@Table(name = "decor_category_master")
@NamedQuery(name = "DecorCategoryMaster.findAll", query = "SELECT a FROM DecorCategoryMaster a")
public class DecorCategoryMaster extends BaseEntity implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_decor_category_id")
	private Integer serDecorCategoryId;

	@Column(name = "txt_decor_category_code")
	private String txtDecorCategoryCode;

	@Column(name = "txt_decor_category_name")
	private String txtDecorCategoryName;

	@Column(name = "bln_is_active")
	private Boolean blnIsActive;

	@OneToMany(mappedBy = "decorCategory", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference
	private List<DecorCategoryPropertyMaster> categoryProperties;

	@OneToMany(mappedBy = "decorCategory", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<DecorCategoryReferenceDocument> referenceDocuments;

	public Integer getSerDecorCategoryId() {
		return serDecorCategoryId;
	}

	public void setSerDecorCategoryId(Integer serDecorCategoryId) {
		this.serDecorCategoryId = serDecorCategoryId;
	}

	public String getTxtDecorCategoryCode() {
		return txtDecorCategoryCode;
	}

	public void setTxtDecorCategoryCode(String txtDecorCategoryCode) {
		this.txtDecorCategoryCode = txtDecorCategoryCode;
	}

	public String getTxtDecorCategoryName() {
		return txtDecorCategoryName;
	}

	public void setTxtDecorCategoryName(String txtDecorCategoryName) {
		this.txtDecorCategoryName = txtDecorCategoryName;
	}

	public Boolean getBlnIsActive() {
		return blnIsActive;
	}

	public void setBlnIsActive(Boolean blnIsActive) {
		this.blnIsActive = blnIsActive;
	}

	public List<DecorCategoryPropertyMaster> getCategoryProperties() {
		return categoryProperties;
	}

	public void setCategoryProperties(List<DecorCategoryPropertyMaster> categoryProperties) {
		this.categoryProperties = categoryProperties;
	}

	public List<DecorCategoryReferenceDocument> getReferenceDocuments() {
		return referenceDocuments;
	}

	public void setReferenceDocuments(List<DecorCategoryReferenceDocument> referenceDocuments) {
		this.referenceDocuments = referenceDocuments;
	}

}