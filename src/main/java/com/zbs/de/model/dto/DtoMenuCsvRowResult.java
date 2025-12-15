package com.zbs.de.model.dto;

public class DtoMenuCsvRowResult {
	private int rowNumber;
	private String code;
	private String name;
	private String errorMessage;
	private String rawRow;

	public DtoMenuCsvRowResult() {
	}

	public DtoMenuCsvRowResult(int rowNumber, String code, String name, String errorMessage, String rawRow) {
		this.rowNumber = rowNumber;
		this.code = code;
		this.name = name;
		this.errorMessage = errorMessage;
		this.rawRow = rawRow;
	}

	// getters / setters
	public int getRowNumber() {
		return rowNumber;
	}

	public void setRowNumber(int rowNumber) {
		this.rowNumber = rowNumber;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getRawRow() {
		return rawRow;
	}

	public void setRawRow(String rawRow) {
		this.rawRow = rawRow;
	}
}