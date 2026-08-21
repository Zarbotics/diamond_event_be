package com.zbs.de.model.dto;

/**
 * A failure that happened in somebody's browser.
 *
 * <p>
 * Everything here is written by a client and none of it is trusted. The
 * controller truncates and flattens each field before it reaches a log.
 *
 * <p>
 * Note what is <em>not</em> here: no customer name, no email address, no event
 * details. A message and a stack are enough to find the fault, and an
 * application log is not a place personal data should accumulate — logs are
 * shipped to aggregators, copied onto laptops and kept far longer than the
 * retention that applies to the database.
 */
public class DtoClientError {

	/** Which application: the journey or the admin portal. */
	private String txtApp;

	/** The route the customer was on, without any query string. */
	private String txtPath;

	private String txtMessage;
	private String txtStack;
	private String txtUserAgent;

	public String getTxtApp() {
		return txtApp;
	}

	public void setTxtApp(String txtApp) {
		this.txtApp = txtApp;
	}

	public String getTxtPath() {
		return txtPath;
	}

	public void setTxtPath(String txtPath) {
		this.txtPath = txtPath;
	}

	public String getTxtMessage() {
		return txtMessage;
	}

	public void setTxtMessage(String txtMessage) {
		this.txtMessage = txtMessage;
	}

	public String getTxtStack() {
		return txtStack;
	}

	public void setTxtStack(String txtStack) {
		this.txtStack = txtStack;
	}

	public String getTxtUserAgent() {
		return txtUserAgent;
	}

	public void setTxtUserAgent(String txtUserAgent) {
		this.txtUserAgent = txtUserAgent;
	}
}
