package com.zbs.de.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@Entity
@Table(name = "venue_master_detail_document")
@NamedQuery(name = "VenueMasterDetailDocument.findAll", query = "SELECT r FROM VenueMasterDetailDocument r")
public class VenueMasterDetailDocument implements Serializable{

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

	@Lob
	@Column(name = "payment_document", nullable = false)
	private byte[] documentFile;

	@JsonIgnoreProperties(value = { "candidateDocuments", "rcsCandidateVerificationData" })
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "ser_venue_master_detail_id")
	private VenueMasterDetail venueMasterDetail;

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

	public byte[] getDocumentFile() {
		return documentFile;
	}

	public void setDocumentFile(byte[] documentFile) {
		this.documentFile = documentFile;
	}

	public VenueMasterDetail getVenueMasterDetail() {
		return venueMasterDetail;
	}

	public void setVenueMasterDetail(VenueMasterDetail venueMasterDetail) {
		this.venueMasterDetail = venueMasterDetail;
	}

}
