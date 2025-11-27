package com.zbs.de.model;

import java.util.Map;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "ingredient")
@NamedQuery(name = "Ingredient.findAll", query = "SELECT a FROM Ingredient a")
public class Ingredient extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_ingredient_id")
	private Long serIngredientId;

	@Column(name = "txt_name", nullable = false)
	private String txtName;

	/**
	 * keep allergy flags or tags in metadata or a simple json list
	 */
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "metadata", columnDefinition = "jsonb")
	private Map<String, Object> metadata;

	public Ingredient() {
		super();
	}

	public Long getSerIngredientId() {
		return serIngredientId;
	}

	public void setSerIngredientId(Long serIngredientId) {
		this.serIngredientId = serIngredientId;
	}

	public String getTxtName() {
		return txtName;
	}

	public void setTxtName(String txtName) {
		this.txtName = txtName;
	}

	public Map<String, Object> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}

}
