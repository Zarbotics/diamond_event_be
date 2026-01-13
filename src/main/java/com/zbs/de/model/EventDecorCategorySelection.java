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
import java.math.BigDecimal;
import java.util.List;

import org.hibernate.annotations.DynamicInsert;

@Entity
@DynamicInsert
@Data
@Table(name = "event_decor_category_selection")
@NamedQuery(name = "EventDecorCategorySelection.findAll", query = "SELECT a FROM EventDecorCategorySelection a")
public class EventDecorCategorySelection extends BaseEntity implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_event_decor_category_selection_id")
	private Integer serEventDecorCategorySelectionId;

	@Column(name = "txt_event_decor_category_selection_code")
	private String txtEventDecorCategorySelectionCode;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_event_master_id", nullable = false)
	private EventMaster eventMaster;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_decor_category_id", nullable = false)
	private DecorCategoryMaster decorCategory;

	@Column(name = "txt_remarks")
	private String txtRemarks;

	@Column(name = "num_price")
	private BigDecimal numPrice = BigDecimal.ZERO;

	@OneToMany(mappedBy = "eventDecorCategorySelection", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<EventDecorPropertySelection> selectedProperties;

	@OneToMany(mappedBy = "eventDecorCategorySelection", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<EventDecorReferenceDocument> userUploadedDocuments;

	public EventMaster getEventMaster() {
		return eventMaster;
	}

	public void setEventMaster(EventMaster eventMaster) {
		this.eventMaster = eventMaster;
	}

	public DecorCategoryMaster getDecorCategory() {
		return decorCategory;
	}

	public void setDecorCategory(DecorCategoryMaster decorCategory) {
		this.decorCategory = decorCategory;
	}

	public String getTxtRemarks() {
		return txtRemarks;
	}

	public void setTxtRemarks(String txtRemarks) {
		this.txtRemarks = txtRemarks;
	}

	public List<EventDecorPropertySelection> getSelectedProperties() {
		return selectedProperties;
	}

	public void setSelectedProperties(List<EventDecorPropertySelection> selectedProperties) {
		this.selectedProperties = selectedProperties;
	}

	public List<EventDecorReferenceDocument> getUserUploadedDocuments() {
		return userUploadedDocuments;
	}

	public void setUserUploadedDocuments(List<EventDecorReferenceDocument> userUploadedDocuments) {
		this.userUploadedDocuments = userUploadedDocuments;
	}

	public Integer getSerEventDecorCategorySelectionId() {
		return serEventDecorCategorySelectionId;
	}

	public void setSerEventDecorCategorySelectionId(Integer serEventDecorCategorySelectionId) {
		this.serEventDecorCategorySelectionId = serEventDecorCategorySelectionId;
	}

	public String getTxtEventDecorCategorySelectionCode() {
		return txtEventDecorCategorySelectionCode;
	}

	public void setTxtEventDecorCategorySelectionCode(String txtEventDecorCategorySelectionCode) {
		this.txtEventDecorCategorySelectionCode = txtEventDecorCategorySelectionCode;
	}

	public BigDecimal getNumPrice() {
		return numPrice;
	}

	public void setNumPrice(BigDecimal numPrice) {
		this.numPrice = numPrice;
	}

}
