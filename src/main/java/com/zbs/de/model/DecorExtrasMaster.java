package com.zbs.de.model;

import java.io.Serializable;
import java.util.List;

import org.hibernate.annotations.DynamicInsert;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@DynamicInsert
@Data
@Table(name = "decor_extras_master")
@NamedQuery(name = "DecorExtrasMaster.findAll", query = "SELECT a FROM DecorExtrasMaster a")
public class DecorExtrasMaster extends BaseEntity implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_extras_id")
	private Integer serExtrasId;

	@Column(name = "txt_extras_code")
	private String txtExtrasCode;

	@Column(name = "txt_extras_name")
	private String txtExtrasName;

	@OneToMany(mappedBy = "decorExtrasMaster", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<DecorExtrasOption> decorExtrasOptions;

	public Integer getSerExtrasId() {
		return serExtrasId;
	}

	public void setSerExtrasId(Integer serExtrasId) {
		this.serExtrasId = serExtrasId;
	}

	public String getTxtExtrasCode() {
		return txtExtrasCode;
	}

	public void setTxtExtrasCode(String txtExtrasCode) {
		this.txtExtrasCode = txtExtrasCode;
	}

	public String getTxtExtrasName() {
		return txtExtrasName;
	}

	public void setTxtExtrasName(String txtExtrasName) {
		this.txtExtrasName = txtExtrasName;
	}

	public List<DecorExtrasOption> getDecorExtrasOptions() {
		return decorExtrasOptions;
	}

	public void setDecorExtrasOptions(List<DecorExtrasOption> decorExtrasOptions) {
		this.decorExtrasOptions = decorExtrasOptions;
	}

}
