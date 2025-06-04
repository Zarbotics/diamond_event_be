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
@Table(name = "menu_food_master")
@NamedQuery(name = "MenuFoodMaster.findAll", query = "SELECT a FROM MenuFoodMaster a")
public class MenuFoodMaster extends BaseEntity implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_menu_food_id")
	private Integer serMenuFoodId;

	@Column(name = "txt_menu_food_code")
	private String txtMenuFoodCode;

	@Column(name = "txt_menu_food_name")
	private String txtMenuFoodName;

	@Column(name = "bln_is_main_course")
	private Boolean blnIsMainCourse;

	@Column(name = "blb_is_appetiser")
	private Boolean blnIsAppetiser;

	@Column(name = "bln_is_starter")
	private Boolean blnIsStarter;

	@Column(name = "bln_is_salad_and_condiment")
	private Boolean blnIsSaladAndCondiment;

	@Column(name = "bln_is_dessert")
	private Boolean blnIsDessert;

	@Column(name = "bln_is_drink")
	private Boolean blnIsDrink;

	public Integer getSerMenuFoodId() {
		return serMenuFoodId;
	}

	public void setSerMenuFoodId(Integer serMenuFoodId) {
		this.serMenuFoodId = serMenuFoodId;
	}

	public String getTxtMenuFoodCode() {
		return txtMenuFoodCode;
	}

	public void setTxtMenuFoodCode(String txtMenuFoodCode) {
		this.txtMenuFoodCode = txtMenuFoodCode;
	}

	public String getTxtMenuFoodName() {
		return txtMenuFoodName;
	}

	public void setTxtMenuFoodName(String txtMenuFoodName) {
		this.txtMenuFoodName = txtMenuFoodName;
	}

	public Boolean getBlnIsMainCourse() {
		return blnIsMainCourse;
	}

	public void setBlnIsMainCourse(Boolean blnIsMainCourse) {
		this.blnIsMainCourse = blnIsMainCourse;
	}

	public Boolean getBlnIsAppetiser() {
		return blnIsAppetiser;
	}

	public void setBlnIsAppetiser(Boolean blnIsAppetiser) {
		this.blnIsAppetiser = blnIsAppetiser;
	}

	public Boolean getBlnIsStarter() {
		return blnIsStarter;
	}

	public void setBlnIsStarter(Boolean blnIsStarter) {
		this.blnIsStarter = blnIsStarter;
	}

	public Boolean getBlnIsSaladAndCondiment() {
		return blnIsSaladAndCondiment;
	}

	public void setBlnIsSaladAndCondiment(Boolean blnIsSaladAndCondiment) {
		this.blnIsSaladAndCondiment = blnIsSaladAndCondiment;
	}

	public Boolean getBlnIsDessert() {
		return blnIsDessert;
	}

	public void setBlnIsDessert(Boolean blnIsDessert) {
		this.blnIsDessert = blnIsDessert;
	}

	public Boolean getBlnIsDrink() {
		return blnIsDrink;
	}

	public void setBlnIsDrink(Boolean blnIsDrink) {
		this.blnIsDrink = blnIsDrink;
	}

}
