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
@Table(name = "decor_color_master")
@NamedQuery(name = "DecorColorMaster.findAll", query = "SELECT a FROM DecorColorMaster a")
public class DecorColorMaster extends BaseEntity implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_decore_color_id")
	private Integer serDecorColorId;

	@Column(name = "txt_color_name")
	private String txtColorName;
	
	@Column(name = "txt_color_code")
	private String txtColorCode;

	@Column(name = "txt_color_hex")
	private String txtColorHex;

	public Integer getSerDecorColorId() {
		return serDecorColorId;
	}

	public void setSerDecorColorId(Integer serDecorColorId) {
		this.serDecorColorId = serDecorColorId;
	}

	public String getTxtColorName() {
		return txtColorName;
	}

	public void setTxtColorName(String txtColorName) {
		this.txtColorName = txtColorName;
	}

	public String getTxtColorHex() {
		return txtColorHex;
	}

	public void setTxtColorHex(String txtColorHex) {
		this.txtColorHex = txtColorHex;
	}


	public String getTxtColorCode() {
		return txtColorCode;
	}

	public void setTxtColorCode(String txtColorCode) {
		this.txtColorCode = txtColorCode;
	}
	
	

}