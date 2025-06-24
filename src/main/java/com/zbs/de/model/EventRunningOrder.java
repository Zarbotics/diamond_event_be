package com.zbs.de.model;

import java.io.Serializable;
import java.util.Date;

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
@Table(name = "event_rnning_order")
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

	@Column(name = "dte_guest_arrival")
	private Date dteGuestArrival;

	@Column(name = "dte_barat_arrival")
	private Date dteBaratArrival;

	@Column(name = "dte_nikah")
	private Date dteNikah;

	@Column(name = "dte_bride_entrance")
	private Date dteBrideEntrance;

	@Column(name = "dte_meal")
	private Date dte_Meal;

	@Column(name = "dte_end_of_night")
	private Date dteEndOfNight;

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

	public Date getDteGuestArrival() {
		return dteGuestArrival;
	}

	public void setDteGuestArrival(Date dteGuestArrival) {
		this.dteGuestArrival = dteGuestArrival;
	}

	public Date getDteBaratArrival() {
		return dteBaratArrival;
	}

	public void setDteBaratArrival(Date dteBaratArrival) {
		this.dteBaratArrival = dteBaratArrival;
	}

	public Date getDteNikah() {
		return dteNikah;
	}

	public void setDteNikah(Date dteNikah) {
		this.dteNikah = dteNikah;
	}

	public Date getDteBrideEntrance() {
		return dteBrideEntrance;
	}

	public void setDteBrideEntrance(Date dteBrideEntrance) {
		this.dteBrideEntrance = dteBrideEntrance;
	}

	public Date getDte_Meal() {
		return dte_Meal;
	}

	public void setDte_Meal(Date dte_Meal) {
		this.dte_Meal = dte_Meal;
	}

	public Date getDteEndOfNight() {
		return dteEndOfNight;
	}

	public void setDteEndOfNight(Date dteEndOfNight) {
		this.dteEndOfNight = dteEndOfNight;
	}

}
