package com.zbs.de.model.dto;

import java.util.List;

public class DtoMenuCsvImportResult {
	private int totalRows;
	private int successful;
	private int failed;
	private List<DtoMenuCsvRowResult> errors;

	public DtoMenuCsvImportResult() {
	}

	public DtoMenuCsvImportResult(int totalRows, int successful, int failed, List<DtoMenuCsvRowResult> errors) {
		this.totalRows = totalRows;
		this.successful = successful;
		this.failed = failed;
		this.errors = errors;
	}

	// getters / setters
	public int getTotalRows() {
		return totalRows;
	}

	public void setTotalRows(int totalRows) {
		this.totalRows = totalRows;
	}

	public int getSuccessful() {
		return successful;
	}

	public void setSuccessful(int successful) {
		this.successful = successful;
	}

	public int getFailed() {
		return failed;
	}

	public void setFailed(int failed) {
		this.failed = failed;
	}

	public List<DtoMenuCsvRowResult> getErrors() {
		return errors;
	}

	public void setErrors(List<DtoMenuCsvRowResult> errors) {
		this.errors = errors;
	}
}
