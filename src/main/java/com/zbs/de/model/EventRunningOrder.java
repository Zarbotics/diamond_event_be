package com.zbs.de.model;

import java.io.Serializable;

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
@Table(name = "event_running_order")
@NamedQuery(name = "EventRunningOrder.findAll", query = "SELECT a FROM EventRunningOrder a")
public class EventRunningOrder extends BaseEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_event_running_order_id")
	private Integer serEventRunningOrderId;

	@Column(name = "txt_event_running_order_code")
	private String txtEventRunningOrderCode;

	@Column(name = "txt_guest_arrival")
	private String txtGuestArrival;

	@Column(name = "txt_barat_arrival")
	private String txtBaratArrival;

	@Column(name = "txt_nikah")
	private String txtNikah;

	@Column(name = "txt_bride_entrance")
	private String txtBrideEntrance;

	@Column(name = "txt_meal")
	private String txtMeal;

	@Column(name = "dte_end_of_night")
	private String txtEndOfNight;

	public Integer getSerEventRunningOrderId() {
		return serEventRunningOrderId;
	}

	public void setSerEventRunningOrderId(Integer serEventRunningOrderId) {
		this.serEventRunningOrderId = serEventRunningOrderId;
	}

	public String getTxtEventRunningOrderCode() {
		return txtEventRunningOrderCode;
	}

	public void setTxtEventRunningOrderCode(String txtEventRunningOrderCode) {
		this.txtEventRunningOrderCode = txtEventRunningOrderCode;
	}

	public String getTxtGuestArrival() {
		return txtGuestArrival;
	}

	public void setTxtGuestArrival(String txtGuestArrival) {
		this.txtGuestArrival = txtGuestArrival;
	}

	public String getTxtBaratArrival() {
		return txtBaratArrival;
	}

	public void setTxtBaratArrival(String txtBaratArrival) {
		this.txtBaratArrival = txtBaratArrival;
	}

	public String getTxtNikah() {
		return txtNikah;
	}

	public void setTxtNikah(String txtNikah) {
		this.txtNikah = txtNikah;
	}

	public String getTxtBrideEntrance() {
		return txtBrideEntrance;
	}

	public void setTxtBrideEntrance(String txtBrideEntrance) {
		this.txtBrideEntrance = txtBrideEntrance;
	}

	public String getTxtMeal() {
		return txtMeal;
	}

	public void setTxtMeal(String txtMeal) {
		this.txtMeal = txtMeal;
	}

	public String getTxtEndOfNight() {
		return txtEndOfNight;
	}

	public void setTxtEndOfNight(String txtEndOfNight) {
		this.txtEndOfNight = txtEndOfNight;
	}

}
