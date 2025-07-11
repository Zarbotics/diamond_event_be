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

import java.io.Serializable;

import org.hibernate.annotations.DynamicInsert;

@Entity
@DynamicInsert
@Data
@Table(name = "decor_category_reference_document")
@NamedQuery(name = "DecorCategoryReferenceDocument.findAll", query = "SELECT a FROM DecorCategoryReferenceDocument a")
public class DecorCategoryReferenceDocument extends BaseEntity implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

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

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_decor_category_id")
	private DecorCategoryMaster decorCategoryMaster;

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

	public DecorCategoryMaster getDecorCategoryMaster() {
		return decorCategoryMaster;
	}

	public void setDecorCategoryMaster(DecorCategoryMaster decorCategoryMaster) {
		this.decorCategoryMaster = decorCategoryMaster;
	}

}
