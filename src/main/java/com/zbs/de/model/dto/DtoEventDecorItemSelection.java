package com.zbs.de.model.dto;

import java.util.List;

public class DtoEventDecorItemSelection {
	private Integer serEventDecorItemId;
	private Integer serEventMasterId;
	private Integer serDecorItemId;
	private String txtDecorCode;
	private Integer selectedColorId;
	private String selectedColorName;
	private Integer numCount;
	private List<Integer> selectedReferenceDocumentIds;
	private Boolean blnIsActive;

	public Integer getSerEventDecorItemId() {
		return serEventDecorItemId;
	}

	public void setSerEventDecorItemId(Integer serEventDecorItemId) {
		this.serEventDecorItemId = serEventDecorItemId;
	}

	public Integer getSerEventMasterId() {
		return serEventMasterId;
	}

	public void setSerEventMasterId(Integer serEventMasterId) {
		this.serEventMasterId = serEventMasterId;
	}

	public Integer getSerDecorItemId() {
		return serDecorItemId;
	}

	public void setSerDecorItemId(Integer serDecorItemId) {
		this.serDecorItemId = serDecorItemId;
	}

	public String getTxtDecorCode() {
		return txtDecorCode;
	}

	public void setTxtDecorCode(String txtDecorCode) {
		this.txtDecorCode = txtDecorCode;
	}

	public Integer getSelectedColorId() {
		return selectedColorId;
	}

	public void setSelectedColorId(Integer selectedColorId) {
		this.selectedColorId = selectedColorId;
	}

	public String getSelectedColorName() {
		return selectedColorName;
	}

	public void setSelectedColorName(String selectedColorName) {
		this.selectedColorName = selectedColorName;
	}

	public Integer getNumCount() {
		return numCount;
	}

	public void setNumCount(Integer numCount) {
		this.numCount = numCount;
	}

	public List<Integer> getSelectedReferenceDocumentIds() {
		return selectedReferenceDocumentIds;
	}

	public void setSelectedReferenceDocumentIds(List<Integer> selectedReferenceDocumentIds) {
		this.selectedReferenceDocumentIds = selectedReferenceDocumentIds;
	}

	public Boolean getBlnIsActive() {
		return blnIsActive;
	}

	public void setBlnIsActive(Boolean blnIsActive) {
		this.blnIsActive = blnIsActive;
	}

}
