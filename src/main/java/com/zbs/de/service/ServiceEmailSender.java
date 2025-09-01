package com.zbs.de.service;

import java.util.Date;

import jakarta.mail.MessagingException;

public interface ServiceEmailSender {

	void sendEmail(String to, String subject, String body) throws MessagingException;

	void sendEventRegistrationEmail(String toEmail, String txtName, String eventCode, String eventType, Date eventDate)
			throws MessagingException;

	void sendEventRegistrationEmailToAdminUsers(String txtName, String eventCode, String eventType, Date eventDate);

	void sendNewCustomerRegistrationEmailToAdminUsers(String txtName, String txtCode, String txtEmail, String txtPhone);
}
