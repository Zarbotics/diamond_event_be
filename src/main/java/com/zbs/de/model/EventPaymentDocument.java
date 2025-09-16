package com.zbs.de.model;

import java.io.Serializable;

import jakarta.persistence.*;

@Entity
@Table(name = "event_payment_document")
public class EventPaymentDocument extends BaseEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_event_payment_document_id")
	private Integer serEventPaymentDocumentId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_event_payment_id", nullable = false)
	private EventPayment eventPayment;

	@Column(name = "txt_file_name")
	private String txtFileName;

	@Column(name = "txt_file_path")
	private String txtFilePath;

	@Column(name = "txt_content_type")
	private String txtContentType;

	@Column(name = "num_file_size")
	private Long numFileSize;

	public Integer getSerEventPaymentDocumentId() {
		return serEventPaymentDocumentId;
	}

	public void setSerEventPaymentDocumentId(Integer serEventPaymentDocumentId) {
		this.serEventPaymentDocumentId = serEventPaymentDocumentId;
	}

	public EventPayment getEventPayment() {
		return eventPayment;
	}

	public void setEventPayment(EventPayment eventPayment) {
		this.eventPayment = eventPayment;
	}

	public String getTxtFileName() {
		return txtFileName;
	}

	public void setTxtFileName(String txtFileName) {
		this.txtFileName = txtFileName;
	}

	public String getTxtFilePath() {
		return txtFilePath;
	}

	public void setTxtFilePath(String txtFilePath) {
		this.txtFilePath = txtFilePath;
	}

	public String getTxtContentType() {
		return txtContentType;
	}

	public void setTxtContentType(String txtContentType) {
		this.txtContentType = txtContentType;
	}

	public Long getNumFileSize() {
		return numFileSize;
	}

	public void setNumFileSize(Long numFileSize) {
		this.numFileSize = numFileSize;
	}
}
