package com.zbs.de.model;

import java.io.Serializable;
import java.math.BigDecimal;

import org.hibernate.annotations.DynamicInsert;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
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

	@Column(name = "txt_description")
	private String txtDescription;

	@Column(name = "num_price")
	private BigDecimal numPrice;

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

	public String getTxtDescription() {
		return txtDescription;
	}

	public void setTxtDescription(String txtDescription) {
		this.txtDescription = txtDescription;
	}

	public BigDecimal getNumPrice() {
		return numPrice;
	}

	public void setNumPrice(BigDecimal numPrice) {
		this.numPrice = numPrice;
	}

}
