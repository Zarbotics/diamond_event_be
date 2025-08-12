package com.zbs.de.config;

import com.zbs.de.model.UserMaster;
import com.zbs.de.repository.RepositoryUserMaster;
import com.zbs.de.service.ServiceEmailSender;
import com.zbs.de.util.UtilDateAndTime;

import jakarta.mail.MessagingException;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service("customerOAuth2USerService")
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

	@Autowired
	private RepositoryUserMaster repositoryUserMaster;
	
	@Autowired
	private ServiceEmailSender emailSender;

//	@Override
//	public OAuth2User loadUser(OAuth2UserRequest userRequest) {
//		OAuth2User oauth2User = super.loadUser(userRequest);
//
//		// Extract Google profile attributes
//		String googleId = oauth2User.getAttribute("sub");
//		String email = oauth2User.getAttribute("email");
//		String name = oauth2User.getAttribute("name");
//		String picture = oauth2User.getAttribute("picture");
//		Boolean emailVerified = oauth2User.getAttribute("email_verified");
//
//
//		// Find user by Google ID or email
//		UserMaster user = repositoryUserMaster.findByTxtGoogleId(googleId)
//				.or(() -> repositoryUserMaster.findByTxtEmail(email)).orElseGet(() -> {
//					// Create new user if not found
//					UserMaster newUser = new UserMaster();
//					newUser.setTxtGoogleId(googleId);
//					newUser.setTxtEmail(email);
//					newUser.setTxtName(name);
//					newUser.setTxtPictureUrl(picture);
//					newUser.setBlnEmailVerified(emailVerified != null && emailVerified);
//					newUser.setBlnIsActive(true);
//					newUser.setBlnIsDeleted(false);
//					newUser.setBlnIsApproved(false);
//					newUser.setCreatedDate(UtilDateAndTime.getCurrentDate());
//					newUser.setTxtRole("ROLE_USER");
//					return repositoryUserMaster.save(newUser);
//				});
//
//		// Update "last login" or updated date
//		user.setUpdatedDate(UtilDateAndTime.getCurrentDate());
//		repositoryUserMaster.save(user);
//
//		return oauth2User;
//	}
	
	
	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) {
		OAuth2User oauth2User = super.loadUser(userRequest);

		// Extract Google profile attributes
		String googleId = oauth2User.getAttribute("sub");
		String email = oauth2User.getAttribute("email");
		String name = oauth2User.getAttribute("name");
		String picture = oauth2User.getAttribute("picture");
		Boolean emailVerified = oauth2User.getAttribute("email_verified");

		// Try find by Google ID, then by email
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		Optional<UserMaster> byGoogle = repositoryUserMaster.findByTxtGoogleId(googleId);
		Optional<UserMaster> byEmail = repositoryUserMaster.findByTxtEmail(email);

		UserMaster user;

		if (byGoogle.isPresent()) {
			user = byGoogle.get();

		} else if (byEmail.isPresent()) {
			user = byEmail.get();
			if (user.getTxtGoogleId() == null || user.getTxtGoogleId().isBlank()) {
				user.setTxtGoogleId(googleId);
				user.setUpdatedDate(UtilDateAndTime.getCurrentDate());
				repositoryUserMaster.save(user); // persist link
			}

		} else {
			// NEW user — create a user record with a random password (hashed)
			String randomPassword = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
			String hashedPassword = encoder.encode(randomPassword);

			UserMaster newUser = new UserMaster();
			newUser.setTxtGoogleId(googleId);
			newUser.setTxtEmail(email);
			newUser.setTxtName(name);
			newUser.setTxtPictureUrl(picture);
			newUser.setBlnEmailVerified(emailVerified != null && emailVerified);
			newUser.setTxtPassword(hashedPassword);
			newUser.setBlnIsActive(true);
			newUser.setBlnIsDeleted(false);
			newUser.setBlnIsApproved(false);
			newUser.setCreatedDate(UtilDateAndTime.getCurrentDate());
			newUser.setTxtRole("ROLE_USER");

			user = repositoryUserMaster.save(newUser);

			// Send welcome email **with** the temporary password
			// Note: we catch exceptions so email errors don't break login
			try {
				String subject = "Welcome to Diamond Events";
				StringBuilder body = new StringBuilder();
				body.append("Hello ").append(name != null ? name : "").append(",\n\n");
				body.append("Thank you for signing up using Google.\n\n");
				body.append("If you ever want to log in without Google, you can use this temporary password:\n\n");
				body.append(randomPassword).append("\n\n");
				body.append("For security, please change this password after your first login.\n");
				body.append("If you did not request this, please contact support.\n\n");
				body.append("Regards,\nDiamond Events Team");

				emailSender.sendEmail(user.getTxtEmail(), subject, body.toString());
			} catch (MessagingException me) {

			} catch (Exception e) {
				// logger.warn("Unexpected email send problem", e);
			}
		}

		user.setUpdatedDate(UtilDateAndTime.getCurrentDate());
		repositoryUserMaster.save(user);

		return oauth2User;
	}
}