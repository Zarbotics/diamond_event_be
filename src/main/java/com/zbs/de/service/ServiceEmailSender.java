package com.zbs.de.service;

import jakarta.mail.MessagingException;

public interface ServiceEmailSender {
	void sendEmail(String to, String subject, String body) throws MessagingException;
}
