package com.zbs.de.model.dto;

import java.util.List;

public class DtoResult {

	String txtMessage;
	List<String> txtMessageLst;
	Object result;
	List<Object> resulList;

	public DtoResult() {
		super();
		// TODO Auto-generated constructor stub
	}

	public DtoResult(String txtMessage, List<String> txtMessageLst, Object result, List<Object> resulList) {
		super();
		this.txtMessage = txtMessage;
		this.txtMessageLst = txtMessageLst;
		this.result = result;
		this.resulList = resulList;
	}

	public String getTxtMessage() {
		return txtMessage;
	}

	public void setTxtMessage(String txtMessage) {
		this.txtMessage = txtMessage;
	}

	public List<String> getTxtMessageLst() {
		return txtMessageLst;
	}

	public void setTxtMessageLst(List<String> txtMessageLst) {
		this.txtMessageLst = txtMessageLst;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public List<Object> getResulList() {
		return resulList;
	}

	public void setResulList(List<Object> resulList) {
		this.resulList = resulList;
	}

}
