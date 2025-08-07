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
@Table(name = "decor_extras_option")
@NamedQuery(name = "DecorExtrasOption.findAll", query = "SELECT a FROM DecorExtrasOption a")
public class DecorExtrasOption extends BaseEntity implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_extra_option_id")
	private Integer serExtraOptionId;

	@Column(name = "txt_option_code")
	private String txtOptionCode;

	@Column(name = "txt_option_name")
	private String txtOptionName;

	@Column(name = "bln_is_document")
	private Boolean blnIsDocument;

	@OneToOne
	@JoinColumn(name = "ser_document_id")
	private DecorExtrasOptionDocument document;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_extras_id")
	private DecorExtrasMaster decorExtrasMaster;

	public Integer getSerExtraOptionId() {
		return serExtraOptionId;
	}

	public void setSerExtraOptionId(Integer serExtraOptionId) {
		this.serExtraOptionId = serExtraOptionId;
	}

	public String getTxtOptionName() {
		return txtOptionName;
	}

	public void setTxtOptionName(String txtOptionName) {
		this.txtOptionName = txtOptionName;
	}

	public String getTxtOptionCode() {
		return txtOptionCode;
	}

	public void setTxtOptionCode(String txtOptionCode) {
		this.txtOptionCode = txtOptionCode;
	}

	public DecorExtrasOptionDocument getDocument() {
		return document;
	}

	public void setDocument(DecorExtrasOptionDocument document) {
		this.document = document;
	}

	public Boolean getBlnIsDocument() {
		return blnIsDocument;
	}

	public void setBlnIsDocument(Boolean blnIsDocument) {
		this.blnIsDocument = blnIsDocument;
	}

	public DecorExtrasMaster getDecorExtrasMaster() {
		return decorExtrasMaster;
	}

	public void setDecorExtrasMaster(DecorExtrasMaster decorExtrasMaster) {
		this.decorExtrasMaster = decorExtrasMaster;
	}

}
