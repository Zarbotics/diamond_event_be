package com.zbs.de.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.zbs.de.model.EmailVerificationToken;
import com.zbs.de.model.UserMaster;
import com.zbs.de.repository.RepositoryEmailVerificationToken;
import com.zbs.de.repository.RepositoryUserMaster;
import com.zbs.de.service.ServiceEmailSender;
import com.zbs.de.service.ServiceEmailVerification;
import com.zbs.de.service.ServiceUserMaster;
import com.zbs.de.util.UtilDateAndTime;

@Service("serviceEmailSender")
public class ServiceEmailSenderImpl implements ServiceEmailSender, ServiceEmailVerification {

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private RepositoryEmailVerificationToken tokenRepository;

	@Autowired
	private RepositoryUserMaster userRepository;
	
	@Autowired
	private ServiceUserMaster serviceUserMaster;	
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceEmailSenderImpl.class);


	@Value("${app.jwt.verification-expiration}")
	private long verificationDurationMs;

	public void sendEmail(String to, String subject, String body) throws MessagingException {
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true);
		helper.setTo(to);
		helper.setSubject(subject);
		helper.setText(body, false); // false = plain text

		mailSender.send(message);
	}

	@Override
	public EmailVerificationToken createToken(UserMaster user) {

		Date expiryDate = new Date(System.currentTimeMillis() + verificationDurationMs);
		EmailVerificationToken token = new EmailVerificationToken();
		token.setToken(UUID.randomUUID().toString());
		token.setUserMaster(user);
		token.setExpiryDate(expiryDate);
		return tokenRepository.save(token);
	}

	@Override
	public boolean verifyToken(String token) {
		Optional<EmailVerificationToken> optToken = tokenRepository.findByToken(token);
		if (optToken.isPresent()) {
			EmailVerificationToken verificationToken = optToken.get();
			if (!verificationToken.isExpired()) {
				UserMaster user = verificationToken.getUserMaster();
				user.setBlnEmailVerified(true);
				userRepository.save(user);
				tokenRepository.delete(verificationToken); // cleanup
				return true;
			}
		}
		return false;
	}
	
	
	public void sendEventRegistrationEmail(String toEmail, String txtName, String eventCode, String eventType, Date eventDate) throws MessagingException {
	    String subject = "Your Event Registration Confirmation – Diamond Event";

	    String message = String.format(
	        """
	        Dear %s,

	        Thank you for choosing Diamond Event Services. We’re delighted to confirm that your event has been successfully registered.

	        📌 Event Details:
	        • Event Code: %s
	        • Event Type: %s
	        • Event Date: %s

	        We truly appreciate your trust in our services. To ensure the smooth and timely execution of your event, we kindly request you to settle your dues at the earliest possible convenience.

	        If you have any questions or special requests, our team is always ready to assist you. Simply reply to this email or reach out to us via our support channels.

	        We look forward to making your event an unforgettable experience.

	        Warm regards,
	        The Diamond Event Team
	        ✨ Celebrating Moments, Creating Memories ✨
	        """,
	        txtName,eventCode, eventType, UtilDateAndTime.dateToStringddmmyyyy(eventDate)
	    );

	    this.sendEmail(toEmail, subject, message);
	}
	
	
	public void sendEventRegistrationEmailToAdmin(String adminEmail, String txtName, String eventCode, String eventType, Date eventDate) throws MessagingException {
	    String subject = "New Event Registration – Diamond Event";

	    String message = String.format(
	        """
	        Hello Admin,

	        A new event has been registered through the Diamond Event Services platform.

	        📌 Event Details:
	        • Registrant Name: %s
	        • Event Code: %s
	        • Event Type: %s
	        • Event Date: %s

	        Please review the registration and take any necessary follow-up actions.

	        Best regards,
	        Diamond Event System
	        ✨ Celebrating Moments, Creating Memories ✨
	        """,
	        txtName, eventCode, eventType, UtilDateAndTime.dateToStringddmmyyyy(eventDate)
	    );

	    // Call your existing email sending method here, e.g.,
	    sendEmail(adminEmail, subject, message);
	}

	
	@Override
	public void sendEventRegistrationEmailToAdminUsers(String txtName, String eventCode, String eventType, Date eventDate) {

		List<UserMaster> userMasters = serviceUserMaster.getAllActiveAdminUsersForEmail();
		if (userMasters != null && !userMasters.isEmpty()) {
			String subject = "New Event Registration – Diamond Event";
			for (UserMaster userMaster : userMasters) {
				if (userMaster.getTxtEmail() != null) {
					try {

						String message = String.format("""
								Hello %s,

								A new event has been registered through the Diamond Event Services platform.

								📌 Event Details:
								• Registrant Name: %s
								• Event Code: %s
								• Event Type: %s
								• Event Date: %s

								Please review the registration and take any necessary follow-up actions.

								Best regards,
								Diamond Event System
								✨ Celebrating Moments, Creating Memories ✨
								""", userMaster.getTxtName(), txtName, eventCode, eventType,
								UtilDateAndTime.dateToStringddmmyyyy(eventDate));
						sendEmail(userMaster.getTxtEmail(), subject, message);
					} catch (Exception e) {
						LOGGER.debug(e.getMessage(), e);
					}

				}
			}
		}

	}
	
	
	
	@Override
	public void sendNewCustomerRegistrationEmailToAdminUsers(String txtName, String txtCode, String txtEmail, String txtPhone) {

		List<UserMaster> userMasters = serviceUserMaster.getAllActiveAdminUsersForEmail();
		if (userMasters != null && !userMasters.isEmpty()) {
			String subject = "New Customer Registration – Diamond Event";
			for (UserMaster userMaster : userMasters) {
				if (userMaster.getTxtEmail() != null && userMaster.getBlnReceiveEmail()) {
					try {

						String message = String.format("""
								Hello %s,

								A new customer has been registered through the Diamond Event Services platform.

								📌 Customer Details:
								• Customer Name: %s
								• Customer Code: %s
								• Customer Phone: %s
								• Customer Email: %s

								Please review the registration and take any necessary follow-up actions.

								Best regards,
								Diamond Event System
								✨ Celebrating Moments, Creating Memories ✨
								""", userMaster.getTxtName(), txtName, txtCode, txtPhone,txtEmail);
						sendEmail(userMaster.getTxtEmail(), subject, message);
					} catch (Exception e) {
						LOGGER.debug(e.getMessage(), e);
					}

				}
			}
		}

	}
	

}
