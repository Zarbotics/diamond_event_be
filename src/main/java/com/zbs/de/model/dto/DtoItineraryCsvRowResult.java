package com.zbs.de.model.dto;

public class DtoItineraryCsvRowResult {
	private int rowNumber;
	private String typeCode;
	private String itemName;
	private String error;
	private String rawRow;
	
	public DtoItineraryCsvRowResult() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public DtoItineraryCsvRowResult(int rowNumber, String typeCode, String itemName, String error, String rawRow) {
		super();
		this.rowNumber = rowNumber;
		this.typeCode = typeCode;
		this.itemName = itemName;
		this.error = error;
		this.rawRow = rawRow;
	}

	public int getRowNumber() {
		return rowNumber;
	}
	public void setRowNumber(int rowNumber) {
		this.rowNumber = rowNumber;
	}
	public String getTypeCode() {
		return typeCode;
	}
	public void setTypeCode(String typeCode) {
		this.typeCode = typeCode;
	}
	public String getItemName() {
		return itemName;
	}
	public void setItemName(String itemName) {
		this.itemName = itemName;
	}
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	public String getRawRow() {
		return rawRow;
	}
	public void setRawRow(String rawRow) {
		this.rawRow = rawRow;
	}
	
}
