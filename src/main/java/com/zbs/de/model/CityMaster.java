package com.zbs.de.model;

import java.io.Serializable;

import org.hibernate.annotations.DynamicInsert;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@DynamicInsert
@Data
@Table(name = "city_master")
@NamedQuery(name = "CityMaster.findAll", query = "SELECT c FROM CityMaster c")
public class CityMaster extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_city_id")
	private int serCityId;

	@Column(name = "txt_city_code")
	private String txtCityCode;

	@Column(name = "txt_city_name")
	private String txtCityName;

	// bi-directional many-to-one association to StateMaster
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_state_id")
	private StateMaster stateMaster;

	public int getSerCityId() {
		return serCityId;
	}

	public void setSerCityId(int serCityId) {
		this.serCityId = serCityId;
	}

	public String getTxtCityCode() {
		return txtCityCode;
	}

	public void setTxtCityCode(String txtCityCode) {
		this.txtCityCode = txtCityCode;
	}

	public String getTxtCityName() {
		return txtCityName;
	}

	public void setTxtCityName(String txtCityName) {
		this.txtCityName = txtCityName;
	}

	public StateMaster getStateMaster() {
		return stateMaster;
	}

	public void setStateMaster(StateMaster stateMaster) {
		this.stateMaster = stateMaster;
	}

}
