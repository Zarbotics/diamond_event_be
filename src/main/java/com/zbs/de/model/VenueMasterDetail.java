package com.zbs.de.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import org.hibernate.annotations.DynamicInsert;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@DynamicInsert
@Data
@Table(name = "venue_master_detail")
@NamedQuery(name = "VenueMasterDetail.findAll", query = "SELECT s FROM VenueMasterDetail s")
public class VenueMasterDetail extends BaseEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ser_venue_master_detail_id")
	private Integer serVenueMasterDetailId;

	@Column(name = "txt_hall_code", nullable = false)
	private String txtHallCode;

	@Column(name = "txt_hall_name", nullable = false)
	private String txtHallName;

	@Column(name = "num_capacity", nullable = false)
	private Integer numCapacity;

	@Column(name = "txt_capacity")
	private String txtCapacity;

	@Column(name = "num_price")
	private BigDecimal numPrice;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ser_venue_master_id", nullable = false)
	@JsonBackReference
	private VenueMaster venueMaster;

	@OneToMany(mappedBy = "venueMasterDetail", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference
	private List<VenueMasterDetailDocument> venueMasterDetailDocument;

	public Integer getSerVenueMasterDetailId() {
		return serVenueMasterDetailId;
	}

	public void setSerVenueMasterDetailId(Integer serVenueMasterDetailId) {
		this.serVenueMasterDetailId = serVenueMasterDetailId;
	}

	public String getTxtHallCode() {
		return txtHallCode;
	}

	public void setTxtHallCode(String txtHallCode) {
		this.txtHallCode = txtHallCode;
	}

	public String getTxtHallName() {
		return txtHallName;
	}

	public void setTxtHallName(String txtHallName) {
		this.txtHallName = txtHallName;
	}

	public Integer getNumCapacity() {
		return numCapacity;
	}

	public void setNumCapacity(Integer numCapacity) {
		this.numCapacity = numCapacity;
	}

	public String getTxtCapacity() {
		return txtCapacity;
	}

	public void setTxtCapacity(String txtCapacity) {
		this.txtCapacity = txtCapacity;
	}

	public BigDecimal getNumPrice() {
		return numPrice;
	}

	public void setNumPrice(BigDecimal numPrice) {
		this.numPrice = numPrice;
	}

	public VenueMaster getVenueMaster() {
		return venueMaster;
	}

	public void setVenueMaster(VenueMaster venueMaster) {
		this.venueMaster = venueMaster;
	}

	public List<VenueMasterDetailDocument> getVenueMasterDetailDocument() {
		return venueMasterDetailDocument;
	}

	public void setVenueMasterDetailDocument(List<VenueMasterDetailDocument> venueMasterDetailDocument) {
		this.venueMasterDetailDocument = venueMasterDetailDocument;
	}

}
