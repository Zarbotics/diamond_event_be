package com.zbs.de.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import org.hibernate.annotations.DynamicInsert;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@DynamicInsert
@Data
@Getter
@Setter
@Table(name = "decore_item_master")
@NamedQuery(name = "DecorItemMaster.findAll", query = "SELECT a FROM DecorItemMaster a")
public class DecorItemMaster extends BaseEntity implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_decor_item_id")
	private Integer serDecorItemId;

	@Column(name = "txt_decor_code")
	private String txtDecorCode;

	@Column(name = "txt_decor_name")
	private String txtDecorName;

	@Column(name = "bln_is_color_required")
	private Boolean blnIsColorRequired;

	@Column(name = "bln_is_image_required")
	private Boolean blnIsImageRequired;

	@Column(name = "bln_is_count_required")
	private Boolean blnIsCountRequired;

	@Column(name = "bln_is_active")
	private Boolean blnIsActive;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_decor_category_id")
	private DecorCategoryMaster decorCategoryMaster;

	@OneToMany(mappedBy = "decorItemMaster", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<DecorReferenceDocument> decorReferenceDocuments;

	public Integer getSerDecorItemId() {
		return serDecorItemId;
	}

	public void setSerDecorItemId(Integer serDecorItemId) {
		this.serDecorItemId = serDecorItemId;
	}

	public String getTxtDecorCode() {
		return txtDecorCode;
	}

	public void setTxtDecorCode(String txtDecorCode) {
		this.txtDecorCode = txtDecorCode;
	}

	public String getTxtDecorName() {
		return txtDecorName;
	}

	public void setTxtDecorName(String txtDecorName) {
		this.txtDecorName = txtDecorName;
	}

	public Boolean getBlnIsColorRequired() {
		return blnIsColorRequired;
	}

	public void setBlnIsColorRequired(Boolean blnIsColorRequired) {
		this.blnIsColorRequired = blnIsColorRequired;
	}

	public Boolean getBlnIsImageRequired() {
		return blnIsImageRequired;
	}

	public void setBlnIsImageRequired(Boolean blnIsImageRequired) {
		this.blnIsImageRequired = blnIsImageRequired;
	}

	public Boolean getBlnIsCountRequired() {
		return blnIsCountRequired;
	}

	public void setBlnIsCountRequired(Boolean blnIsCountRequired) {
		this.blnIsCountRequired = blnIsCountRequired;
	}

	public Boolean getBlnIsActive() {
		return blnIsActive;
	}

	public void setBlnIsActive(Boolean blnIsActive) {
		this.blnIsActive = blnIsActive;
	}

	public DecorCategoryMaster getDecorCategoryMaster() {
		return decorCategoryMaster;
	}

	public void setDecorCategoryMaster(DecorCategoryMaster decorCategoryMaster) {
		this.decorCategoryMaster = decorCategoryMaster;
	}

	public List<DecorReferenceDocument> getDecorReferenceDocuments() {
		return decorReferenceDocuments;
	}

	public void setDecorReferenceDocuments(List<DecorReferenceDocument> decorReferenceDocuments) {
		this.decorReferenceDocuments = decorReferenceDocuments;
	}

}
