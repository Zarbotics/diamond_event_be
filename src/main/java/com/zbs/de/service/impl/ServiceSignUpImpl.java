package com.zbs.de.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.zbs.de.model.UserMaster;
import com.zbs.de.model.dto.DtoSignupRequest;
import com.zbs.de.repository.RepositoryUserMaster;
import com.zbs.de.service.ServiceEmailVerificationToken;
import com.zbs.de.service.ServiceSignUp;
import com.zbs.de.util.UtilDateAndTime;

public class ServiceSignUpImpl implements ServiceSignUp {

	@Autowired
	private RepositoryUserMaster userRepo;

	@Autowired
	private ServiceEmailVerificationToken emailService;

	public void signup(DtoSignupRequest request) {
		if (userRepo.findByTxtEmail(request.getEmail()).isPresent()) {
			throw new RuntimeException("Email already in use");
		}

		UserMaster user = new UserMaster();
		user.setTxtEmail(request.getEmail());
		user.setTxtPassword(new BCryptPasswordEncoder().encode(request.getPassword()));
		user.setTxtFirstName(request.getFirstName());
		user.setTxtLastName(request.getLastName());
		user.setTxtName(request.getFullName());
		user.setBlnEmailVerified(false);
		user.setBlnIsActive(true);
		user.setBlnIsDeleted(false);
		user.setBlnIsApproved(false);
		user.setTxtRole("ROLE_USER");
		user.setCreatedDate(UtilDateAndTime.getCurrentDate());

		UserMaster savedUser = userRepo.save(user);

		emailService.sendVerificationEmail(savedUser);
	}

}
