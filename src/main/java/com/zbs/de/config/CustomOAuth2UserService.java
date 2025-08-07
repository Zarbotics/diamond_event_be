package com.zbs.de.config;

import com.zbs.de.model.UserMaster;
import com.zbs.de.repository.RepositoryUserMaster;
import com.zbs.de.util.UtilDateAndTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service("customerOAuth2USerService")
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

	@Autowired
	private RepositoryUserMaster repositoryUserMaster;

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) {
		OAuth2User oauth2User = super.loadUser(userRequest);

		// Extract Google profile attributes
		String googleId = oauth2User.getAttribute("sub");
		String email = oauth2User.getAttribute("email");
		String name = oauth2User.getAttribute("name");
		String picture = oauth2User.getAttribute("picture");
		Boolean emailVerified = oauth2User.getAttribute("email_verified");

		// Find user by Google ID or email
		UserMaster user = repositoryUserMaster.findByTxtGoogleId(googleId)
				.or(() -> repositoryUserMaster.findByTxtEmail(email)).orElseGet(() -> {
					// Create new user if not found
					UserMaster newUser = new UserMaster();
					newUser.setTxtGoogleId(googleId);
					newUser.setTxtEmail(email);
					newUser.setTxtName(name);
					newUser.setTxtPictureUrl(picture);
					newUser.setBlnEmailVerified(emailVerified != null && emailVerified);
					newUser.setBlnIsActive(true);
					newUser.setBlnIsDeleted(false);
					newUser.setBlnIsApproved(false);
					newUser.setCreatedDate(UtilDateAndTime.getCurrentDate());
					newUser.setTxtRole("ROLE_USER");
					return repositoryUserMaster.save(newUser);
				});

		// Update "last login" or updated date
		user.setUpdatedDate(UtilDateAndTime.getCurrentDate());
		repositoryUserMaster.save(user);

		return oauth2User;
	}
}