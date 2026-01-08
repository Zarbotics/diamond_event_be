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
import jakarta.persistence.Table;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

import org.hibernate.annotations.DynamicInsert;

@Entity
@DynamicInsert
@Data
@Table(name = "catering_delivery_item_details")
@NamedQuery(name = "CateringDeliveryItemDetail.findAll", query = "SELECT a FROM CateringDeliveryItemDetail a")
public class CateringDeliveryItemDetail extends BaseEntity implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_catering_delivery_detail_id")
	private Integer serCateringDeliveryDetailId;

	@Column(name = "txt_catering_delivery_detail_code")
	private String txtCateringDeliveryDetailCode;

	@Column(name = "num_quantity")
	private Integer numQuantity;

	@Column(name = "txt_notes", length = 500)
	private String txtNotes;

//	@ManyToOne(fetch = FetchType.LAZY)
//	@JoinColumn(name = "ser_menu_food_id")
//	private MenuFoodMaster menueFoodMaster;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_menu_item_id")
	private MenuItem menuItem;

	@Column(name = "num_price")
	private BigDecimal numPrice;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_delivery_booking_id")
	private CateringDeliveryBooking cateringDeliveryBooking;

	public Integer getSerCateringDeliveryDetailId() {
		return serCateringDeliveryDetailId;
	}

	public void setSerCateringDeliveryDetailId(Integer serCateringDeliveryDetailId) {
		this.serCateringDeliveryDetailId = serCateringDeliveryDetailId;
	}

	public String getTxtCateringDeliveryDetailCode() {
		return txtCateringDeliveryDetailCode;
	}

	public void setTxtCateringDeliveryDetailCode(String txtCateringDeliveryDetailCode) {
		this.txtCateringDeliveryDetailCode = txtCateringDeliveryDetailCode;
	}

	public CateringDeliveryBooking getCateringDeliveryBooking() {
		return cateringDeliveryBooking;
	}

	public void setCateringDeliveryBooking(CateringDeliveryBooking cateringDeliveryBooking) {
		this.cateringDeliveryBooking = cateringDeliveryBooking;
	}

	public Integer getNumQuantity() {
		return numQuantity;
	}

	public void setNumQuantity(Integer numQuantity) {
		this.numQuantity = numQuantity;
	}

	public String getTxtNotes() {
		return txtNotes;
	}

	public void setTxtNotes(String txtNotes) {
		this.txtNotes = txtNotes;
	}

	public MenuItem getMenuItem() {
		return menuItem;
	}

	public void setMenuItem(MenuItem menuItem) {
		this.menuItem = menuItem;
	}

	public BigDecimal getNumPrice() {
		return numPrice;
	}

	public void setNumPrice(BigDecimal numPrice) {
		this.numPrice = numPrice;
	}

}
