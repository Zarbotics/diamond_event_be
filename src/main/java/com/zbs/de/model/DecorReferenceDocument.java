package com.zbs.de.model;

import jakarta.persistence.Column;
import jakarta.persistence.Id;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import lombok.Data;


import org.hibernate.annotations.DynamicInsert;

@Entity
@DynamicInsert
@Data
@Table(name = "decor_reference_image")
@NamedQuery(name = "DecorReferenceDocument.findAll", query = "SELECT a FROM DecorReferenceDocument a")
public class DecorReferenceDocument {
	@Id
	@GeneratedValue
	@Column(name = "ser_document_id")
	private Integer documentId;

	@Column(name = "txt_document_name")
	private String documentName;

	@Column(name = "txt_document_type")
	private String documentType;

	@Column(name = "txt_original_name")
	private String originalName;

	@Column(name = "txt_size")
	private String size;

	@Column(name = "file_path")
	private String filePath;

    // For referencing decor components
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ser_themed_stage_id")
    private DecorThemedStage themedStage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ser_chair_id")
    private DecorChair chair;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ser_centerpiece_id")
    private DecorTableCenterpiece tableCenterpiece;

	public Integer getDocumentId() {
		return documentId;
	}

	public void setDocumentId(Integer documentId) {
		this.documentId = documentId;
	}

	public String getDocumentName() {
		return documentName;
	}

	public void setDocumentName(String documentName) {
		this.documentName = documentName;
	}

	public String getDocumentType() {
		return documentType;
	}

	public void setDocumentType(String documentType) {
		this.documentType = documentType;
	}

	public String getOriginalName() {
		return originalName;
	}

	public void setOriginalName(String originalName) {
		this.originalName = originalName;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public DecorThemedStage getThemedStage() {
		return themedStage;
	}

	public void setThemedStage(DecorThemedStage themedStage) {
		this.themedStage = themedStage;
	}

	public DecorChair getChair() {
		return chair;
	}

	public void setChair(DecorChair chair) {
		this.chair = chair;
	}

	public DecorTableCenterpiece getTableCenterpiece() {
		return tableCenterpiece;
	}

	public void setTableCenterpiece(DecorTableCenterpiece tableCenterpiece) {
		this.tableCenterpiece = tableCenterpiece;
	}
    
    
}
