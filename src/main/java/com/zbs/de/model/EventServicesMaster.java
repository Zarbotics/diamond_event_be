package com.zbs.de.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.io.Serializable;
import java.math.BigDecimal;

import org.hibernate.annotations.DynamicInsert;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@DynamicInsert
@Data
@Table(name = "event_services_master")
@NamedQuery(name = "EventServicesMaster.findAll", query = "SELECT s FROM EventServicesMaster s")
public class EventServicesMaster extends BaseEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_service_id")
	private Integer serServiceId;

	@Column(name = "txt_service_name")
	private String txtServiceName;

	@Column(name = "num_price")
	private BigDecimal numPrice;

	public Integer getSerServiceId() {
		return serServiceId;
	}

	public void setSerServiceId(Integer serServiceId) {
		this.serServiceId = serServiceId;
	}

	public String getTxtServiceName() {
		return txtServiceName;
	}

	public void setTxtServiceName(String txtServiceName) {
		this.txtServiceName = txtServiceName;
	}

	public BigDecimal getNumPrice() {
		return numPrice;
	}

	public void setNumPrice(BigDecimal numPrice) {
		this.numPrice = numPrice;
	}

}
