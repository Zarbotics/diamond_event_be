package com.zbs.de.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;

import java.io.Serializable;
import java.util.List;

import org.hibernate.annotations.DynamicInsert;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@DynamicInsert
@Data
@Getter
@Setter
@Table(name = "country_master")
@NamedQuery(name = "CountryMaster.findAll", query = "SELECT c FROM CountryMaster c")
public class CountryMaster extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_country_id")
	private Integer serCountryId;

	@Column(name = "txt_country_code")
	private String txtCountryCode;

	@Column(name = "txt_country_name")
	private String txtCountryName;

	/**
	 * Legacy duplicate of {@code BaseEntity.blnIsActive}, kept because rows in the
	 * live database still carry it.
	 *
	 * <p>
	 * This was declared as a primitive {@code boolean} against a nullable column.
	 * Hibernate cannot represent NULL in a primitive, so it throws
	 * {@code PropertyAccessException: Null value was assigned to a property of
	 * primitive type} while hydrating the row — and because {@code CountryMaster}
	 * is reached through {@code StateMaster} from {@code CityMaster}, that took
	 * out the whole venue list with a 500, on a screen the customer cannot get
	 * past. Any country row inserted by anything that did not know about this
	 * second flag was enough to trigger it.
	 *
	 * <p>
	 * {@code Boolean} tolerates the NULL. {@link #isActive()} still answers a
	 * primitive so no caller has to change, treating "not stated" as inactive,
	 * which matches how the flag is read everywhere else.
	 */
	@Column(name = "is_active")
	private Boolean isActive;

	@Column(name = "short_name")
	private String shortName;

	@Column(name = "default_country")
	private Integer defaultCountry;

	// bi-directional many-to-one association to StateMaster
	@OneToMany(mappedBy = "countryMaster")
	@JsonManagedReference
	private List<StateMaster> stateMasters;

	public Integer getSerCountryId() {
		return serCountryId;
	}

	public void setSerCountryId(Integer serCountryId) {
		this.serCountryId = serCountryId;
	}

	public String getTxtCountryCode() {
		return txtCountryCode;
	}

	public void setTxtCountryCode(String txtCountryCode) {
		this.txtCountryCode = txtCountryCode;
	}

	public String getTxtCountryName() {
		return txtCountryName;
	}

	public void setTxtCountryName(String txtCountryName) {
		this.txtCountryName = txtCountryName;
	}

	/** NULL — the flag was never set on this row — reads as inactive. */
	public boolean isActive() {
		return Boolean.TRUE.equals(isActive);
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public Integer getDefaultCountry() {
		return defaultCountry;
	}

	public void setDefaultCountry(Integer defaultCountry) {
		this.defaultCountry = defaultCountry;
	}

	public List<StateMaster> getStateMasters() {
		return stateMasters;
	}

	public void setStateMasters(List<StateMaster> stateMasters) {
		this.stateMasters = stateMasters;
	}

}
