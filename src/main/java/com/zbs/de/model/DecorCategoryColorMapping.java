package com.zbs.de.model;

import jakarta.persistence.Column;
import jakarta.persistence.Id;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import lombok.Data;

import java.io.Serializable;

import org.hibernate.annotations.DynamicInsert;

@Entity
@DynamicInsert
@Data
@Table(name = "decor_category_color_mapping")
@NamedQuery(name = "DecorCategoryColorMapping.findAll", query = "SELECT a FROM DecorCategoryColorMapping a")
public class DecorCategoryColorMapping extends BaseEntity implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_mapping_id")
	private Integer serMappingId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_decor_category_id")
	private DecorCategoryMaster decorCategory;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_decor_color_id")
	private DecorColorMaster decorColorMaster;

	public Integer getSerMappingId() {
		return serMappingId;
	}

	public void setSerMappingId(Integer serMappingId) {
		this.serMappingId = serMappingId;
	}

	public DecorCategoryMaster getDecorCategory() {
		return decorCategory;
	}

	public void setDecorCategory(DecorCategoryMaster decorCategory) {
		this.decorCategory = decorCategory;
	}

	public DecorColorMaster getDecorColorMaster() {
		return decorColorMaster;
	}

	public void setDecorColorMaster(DecorColorMaster decorColorMaster) {
		this.decorColorMaster = decorColorMaster;
	}

}