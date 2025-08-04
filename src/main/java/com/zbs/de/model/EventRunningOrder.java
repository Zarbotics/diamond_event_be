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

	@Column(name = "txt_groom_guest_arrival")
	private String txtGroomGuestArrival;

	@Column(name = "txt_bride_guest_arrival")
	private String txtBrideGuestArrival;

	@Column(name = "txt_barat_arrival")
	private String txtBaratArrival;

	@Column(name = "txt_nikah")
	private String txtNikah;

	@Column(name = "txt_bride_entrance")
	private String txtBrideEntrance;

	@Column(name = "txt_groom_entrance")
	private String txtGroomEntrance;

	@Column(name = "txt_couples_entrance")
	private String txtCouplesEntrance;

	@Column(name = "txt_dua")
	private String txtDua;

	@Column(name = "txt_cake_cutting")
	private String txtCakeCutting;

	@Column(name = "txt_ring_exchange")
	private String txtRingExchange;

	@Column(name = "txt_rams")
	private String txtRams;

	@Column(name = "txt_speeches")
	private String txtSpeeches;

	@Column(name = "txt_Dance")
	private String txtDance;

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

	public String getTxtGroomGuestArrival() {
		return txtGroomGuestArrival;
	}

	public void setTxtGroomGuestArrival(String txtGroomGuestArrival) {
		this.txtGroomGuestArrival = txtGroomGuestArrival;
	}

	public String getTxtBrideGuestArrival() {
		return txtBrideGuestArrival;
	}

	public void setTxtBrideGuestArrival(String txtBrideGuestArrival) {
		this.txtBrideGuestArrival = txtBrideGuestArrival;
	}

	public String getTxtGroomEntrance() {
		return txtGroomEntrance;
	}

	public void setTxtGroomEntrance(String txtGroomEntrance) {
		this.txtGroomEntrance = txtGroomEntrance;
	}

	public String getTxtCouplesEntrance() {
		return txtCouplesEntrance;
	}

	public void setTxtCouplesEntrance(String txtCouplesEntrance) {
		this.txtCouplesEntrance = txtCouplesEntrance;
	}

	public String getTxtDua() {
		return txtDua;
	}

	public void setTxtDua(String txtDua) {
		this.txtDua = txtDua;
	}

	public String getTxtCakeCutting() {
		return txtCakeCutting;
	}

	public void setTxtCakeCutting(String txtCakeCutting) {
		this.txtCakeCutting = txtCakeCutting;
	}

	public String getTxtRingExchange() {
		return txtRingExchange;
	}

	public void setTxtRingExchange(String txtRingExchange) {
		this.txtRingExchange = txtRingExchange;
	}

	public String getTxtRams() {
		return txtRams;
	}

	public void setTxtRams(String txtRams) {
		this.txtRams = txtRams;
	}

	public String getTxtSpeeches() {
		return txtSpeeches;
	}

	public void setTxtSpeeches(String txtSpeeches) {
		this.txtSpeeches = txtSpeeches;
	}

	public String getTxtDance() {
		return txtDance;
	}

	public void setTxtDance(String txtDance) {
		this.txtDance = txtDance;
	}

}
