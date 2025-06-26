package com.zbs.de.model;

import jakarta.persistence.Column;
import jakarta.persistence.Id;

import jakarta.persistence.Entity;
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

	@Column(name = "txt_option_name")
	private String txtOptionName;

	@Column(name = "bln_is_active")
	private Boolean blnIsActive;

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

	public Boolean getBlnIsActive() {
		return blnIsActive;
	}

	public void setBlnIsActive(Boolean blnIsActive) {
		this.blnIsActive = blnIsActive;
	}

}
