package com.zbs.de.model.dto;

import java.util.List;

public class DtoItineraryCsvImportResult {
	private int total;
	private int success;
	private int failed;
	private List<DtoItineraryCsvRowResult> errors;



	public DtoItineraryCsvImportResult() {
		super();
		// TODO Auto-generated constructor stub
	}

	
	public DtoItineraryCsvImportResult(int total, int success, int failed, List<DtoItineraryCsvRowResult> errors) {
		super();
		this.total = total;
		this.success = success;
		this.failed = failed;
		this.errors = errors;
	}


	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public int getSuccess() {
		return success;
	}

	public void setSuccess(int success) {
		this.success = success;
	}

	public int getFailed() {
		return failed;
	}

	public void setFailed(int failed) {
		this.failed = failed;
	}

	public List<DtoItineraryCsvRowResult> getErrors() {
		return errors;
	}

	public void setErrors(List<DtoItineraryCsvRowResult> errors) {
		this.errors = errors;
	}

}
