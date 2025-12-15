package com.zbs.de.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "menu_item_ingredient")
@NamedQuery(name = "MenuItemIngredient.findAll", query = "SELECT a FROM MenuItemIngredient a")
public class MenuItemIngredient extends BaseEntity {

	@EmbeddedId
	private MenuItemIngredientKey id;

	@Column(name = "num_quantity")
	private Double numQuantity;

	@Column(name = "txt_uom")
	private String txtUOM;

	public MenuItemIngredient() {
		super();
	}

	public MenuItemIngredientKey getId() {
		return id;
	}

	public void setId(MenuItemIngredientKey id) {
		this.id = id;
	}

	public Double getNumQuantity() {
		return numQuantity;
	}

	public void setNumQuantity(Double numQuantity) {
		this.numQuantity = numQuantity;
	}

	public String getTxtUOM() {
		return txtUOM;
	}

	public void setTxtUOM(String txtUOM) {
		this.txtUOM = txtUOM;
	}

	@Embeddable
	public static class MenuItemIngredientKey implements Serializable {
		@Column(name = "menu_item_id")
		private Long menuItemId;

		@Column(name = "ingredient_id")
		private Long ingredientId;

		public MenuItemIngredientKey() {
		}

		public MenuItemIngredientKey(Long menuItemId, Long ingredientId) {
			this.menuItemId = menuItemId;
			this.ingredientId = ingredientId;
		}

		// getters/setters, equals & hashCode
		public Long getMenuItemId() {
			return menuItemId;
		}

		public void setMenuItemId(Long menuItemId) {
			this.menuItemId = menuItemId;
		}

		public Long getIngredientId() {
			return ingredientId;
		}

		public void setIngredientId(Long ingredientId) {
			this.ingredientId = ingredientId;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (!(o instanceof MenuItemIngredientKey))
				return false;
			MenuItemIngredientKey that = (MenuItemIngredientKey) o;
			return Objects.equals(menuItemId, that.menuItemId) && Objects.equals(ingredientId, that.ingredientId);
		}

		@Override
		public int hashCode() {
			return Objects.hash(menuItemId, ingredientId);
		}
	}
}
