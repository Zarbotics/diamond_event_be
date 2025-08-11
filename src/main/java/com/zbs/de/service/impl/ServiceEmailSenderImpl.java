package com.zbs.de.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import com.zbs.de.service.ServiceEmailSender;

public class ServiceEmailSenderImpl implements ServiceEmailSender {

	@Autowired
	private JavaMailSender mailSender;

	public void sendEmail(String to, String subject, String body) throws MessagingException {
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true);
		helper.setTo(to);
		helper.setSubject(subject);
		helper.setText(body, false); // false = plain text

		mailSender.send(message);
	}
}
