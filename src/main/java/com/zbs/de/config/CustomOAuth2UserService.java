
package com.zbs.de.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zbs.de.model.UserMaster;
import com.zbs.de.repository.RepositoryUserMaster;
import com.zbs.de.service.ServiceEmailSender;
import com.zbs.de.util.UtilDateAndTime;

import jakarta.mail.MessagingException;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
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

		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		String registrationId = userRequest.getClientRegistration().getRegistrationId();

		if ("apple".equals(registrationId)) {

		    Map<String, Object> additionalParams = userRequest.getAdditionalParameters();
		    String idToken = (String) additionalParams.get("id_token");
		    Map<String, Object> claims = decodeJwt(idToken);

		    String appleId = (String) claims.get("sub");
		    String email = (String) claims.get("email");

		    // ✅ FIX: Read name from the 'user' JSON param (only sent on first login)
		    String firstName = null;
		    String lastName = null;
		    String userJson = (String) additionalParams.get("user");
		    if (userJson != null) {
		        try {
		            ObjectMapper mapper = new ObjectMapper();
		            Map<String, Object> userMap = mapper.readValue(userJson, Map.class);
		            Map<String, Object> nameMap = (Map<String, Object>) userMap.get("name");
		            if (nameMap != null) {
		                firstName = (String) nameMap.get("firstName");
		                lastName  = (String) nameMap.get("lastName");
		            }
		        } catch (Exception ignored) {}
		    }
		    final String resolvedName = buildName(firstName, lastName);

		    Optional<UserMaster> userOpt = repositoryUserMaster.findByTxtAppleId(appleId);
		    if (userOpt.isEmpty() && email != null) {
		        userOpt = repositoryUserMaster.findByTxtEmail(email);
		    }

		    UserMaster user = userOpt.orElseGet(() -> {
				// NEW user — create a user record with a random password (hashed)
				String randomPassword = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
				String hashedPassword = encoder.encode(randomPassword);
		        UserMaster newUser = new UserMaster();
		        newUser.setTxtAppleId(appleId);
		        newUser.setTxtEmail(email != null ? email : "apple_" + appleId + "@noemail.com");
		        newUser.setTxtName(resolvedName != null ? resolvedName : "");
		        newUser.setTxtRole("ROLE_USER");
		        newUser.setTxtPassword(hashedPassword);
		        newUser.setBlnIsActive(true);
		        newUser.setBlnIsDeleted(false);
		        newUser.setBlnIsApproved(false);
		        newUser.setCreatedDate(UtilDateAndTime.getCurrentDate());
		        
		     // Send welcome email **with** the temporary password
				// Note: we catch exceptions so email errors don't break login
				if (email != null) {
					try {
						String subject = "Welcome to Diamond Events";
						StringBuilder body = new StringBuilder();
						body.append("Hello ").append(resolvedName != null ? resolvedName : "").append(",\n\n");
						body.append("Thank you for signing up using Google.\n\n");
						body.append(
								"If you ever want to log in without Google, you can use this temporary password:\n\n");
						body.append(randomPassword).append("\n\n");
						body.append("For security, please change this password after your first login.\n");
						body.append("If you did not request this, please contact support.\n\n");
						body.append("Regards,\nDiamond Events Team");

						emailSender.sendEmail(newUser.getTxtEmail(), subject, body.toString());
					} catch (MessagingException me) {

					} catch (Exception e) {
						// logger.warn("Unexpected email send problem", e);
					}

				}

		        return repositoryUserMaster.save(newUser);
		    });

		    // ✅ FIX: Update name only if we got it and the stored name is still generic
		    if (resolvedName != null && "Apple User".equals(user.getTxtName())) {
		        user.setTxtName(resolvedName);
		    }

		    user.setUpdatedDate(UtilDateAndTime.getCurrentDate());
		    repositoryUserMaster.save(user);

		    return new DefaultOAuth2User(
		            List.of(() -> "ROLE_USER"),
		            claims,
		            "sub"
		    );
		}
		
		OAuth2User oauth2User = super.loadUser(userRequest);
		
		
		// Extract Google profile attributes
		String googleId = oauth2User.getAttribute("sub");
		String email = oauth2User.getAttribute("email");
		String name = oauth2User.getAttribute("name");
		String picture = oauth2User.getAttribute("picture");
		Boolean emailVerified = oauth2User.getAttribute("email_verified");

		// Try find by Google ID, then by email
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
			newUser.setBlnReceiveEmail(false);
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
	
	private Map<String, Object> decodeJwt(String idToken) {
	    try {
	        String[] chunks = idToken.split("\\.");
	        String payload = new String(Base64.getDecoder().decode(chunks[1]));
	        ObjectMapper mapper = new ObjectMapper();
	        return mapper.readValue(payload, Map.class);
	    } catch (Exception e) {
	        throw new RuntimeException("Invalid Apple ID token", e);
	    }
	}
	
	private String buildName(String firstName, String lastName) {
	    if (firstName == null && lastName == null) return null;
	    StringBuilder sb = new StringBuilder();
	    if (firstName != null) sb.append(firstName.trim());
	    if (lastName  != null) sb.append(" ").append(lastName.trim());
	    return sb.toString().trim();
	}
}