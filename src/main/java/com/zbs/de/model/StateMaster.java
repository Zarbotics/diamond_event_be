package com.zbs.de.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.io.Serializable;
import java.util.List;

import org.hibernate.annotations.DynamicInsert;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@DynamicInsert
@Data
@Table(name = "state_master")
@NamedQuery(name = "StateMaster.findAll", query = "SELECT s FROM StateMaster s")
public class StateMaster extends BaseEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_state_id")
	private int serStateId;

	@Column(name = "txt_state_name")
	private String txtStateName;

	@Column(name = "txt_state_code")
	private String txtStateCode;

	// bi-directional many-to-one association to CityMaster
	@OneToMany(mappedBy = "stateMaster")
	@JsonManagedReference
	private List<CityMaster> cityMasters;

	// bi-directional many-to-one association to CountryMaster
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_country_id")
	@JsonBackReference
	private CountryMaster countryMaster;

	public int getSerStateId() {
		return serStateId;
	}

	public void setSerStateId(int serStateId) {
		this.serStateId = serStateId;
	}

	public String getTxtStateName() {
		return txtStateName;
	}

	public void setTxtStateName(String txtStateName) {
		this.txtStateName = txtStateName;
	}

	public String getTxtStateCode() {
		return txtStateCode;
	}

	public void setTxtStateCode(String txtStateCode) {
		this.txtStateCode = txtStateCode;
	}

	public List<CityMaster> getCityMasters() {
		return cityMasters;
	}

	public void setCityMasters(List<CityMaster> cityMasters) {
		this.cityMasters = cityMasters;
	}

	public CountryMaster getCountryMaster() {
		return countryMaster;
	}

	public void setCountryMaster(CountryMaster countryMaster) {
		this.countryMaster = countryMaster;
	}

}
