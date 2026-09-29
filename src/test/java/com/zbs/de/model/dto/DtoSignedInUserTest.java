package com.zbs.de.model.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zbs.de.model.UserMaster;

/**
 * What {@code GET /auth/status} is allowed to tell a browser.
 *
 * <h2>The defect this is here to prevent coming back</h2>
 *
 * The endpoint returned {@code authentication.getPrincipal()} — the
 * {@link UserMaster} entity, serialised whole. Nineteen fields went to the
 * browser on every page load, and one of them was {@code txtPassword}: the
 * bcrypt hash of that account's password.
 *
 * <p>
 * It reached the network tab, anything logging responses, and the browser
 * console — the admin portal printed the whole object on every render of its
 * header, on a machine several coordinators share.
 *
 * <p>
 * A hash is not a password and bcrypt is slow on purpose, so this was not an
 * immediate compromise. It was the account's one secret, handed out for no
 * reason, to every client, forever.
 *
 * <p>
 * The assertions below are deliberately of two kinds. The first names the
 * fields that must never appear, which is the specific bug. The second is an
 * allow-list over the DTO's own fields, which catches the general one: a field
 * added to this DTO later, carrying something new, without anybody thinking
 * about who reads it.
 */
class DtoSignedInUserTest {

	/** Everything the two clients actually read, and nothing else. */
	private static final List<String> ALLOWED = Arrays.asList(
			"serUserId",
			"txtEmail",
			"txtName",
			"txtFirstName",
			"txtLastName",
			"txtPictureUrl",
			"txtRole",
			"blnEmailVerified");

	private UserMaster aUser() {
		UserMaster user = new UserMaster();

		user.setSerUserId(2L);
		user.setTxtEmail("dev.admin@example.com");
		user.setTxtName("Dev Admin");
		user.setTxtFirstName("Dev");
		user.setTxtLastName("Admin");
		user.setTxtRole("ROLE_ADMIN");
		user.setBlnEmailVerified(true);

		/* The three that must not travel. */
		user.setTxtPassword("$2a$10$0buxxJB.A5yXTo7yG01ArOAu6WML6qQtlUGPZ1r");
		user.setTxtGoogleId("google-oauth-subject-id");
		user.setTxtAppleId("apple-subject-id");

		return user;
	}

	@Test
	void carriesWhatTheScreensNeed() {
		DtoSignedInUser dto = DtoSignedInUser.from(aUser());

		assertThat(dto.getSerUserId()).isEqualTo(2L);
		assertThat(dto.getTxtEmail()).isEqualTo("dev.admin@example.com");
		assertThat(dto.getTxtName()).isEqualTo("Dev Admin");
		assertThat(dto.getTxtFirstName()).isEqualTo("Dev");
		assertThat(dto.getTxtPictureUrl()).isNull();
		assertThat(dto.getTxtRole()).isEqualTo("ROLE_ADMIN");
	}

	/** The bug itself: the hash must not be in the JSON at all. */
	@Test
	void doesNotCarryThePasswordHash() throws Exception {
		String json = new ObjectMapper().writeValueAsString(DtoSignedInUser.from(aUser()));

		assertThat(json).doesNotContain("txtPassword");
		assertThat(json).doesNotContain("$2a$10$");
	}

	/** Nor the identifiers of the accounts it is federated with. */
	@Test
	void doesNotCarryTheFederatedIdentifiers() throws Exception {
		String json = new ObjectMapper().writeValueAsString(DtoSignedInUser.from(aUser()));

		assertThat(json).doesNotContain("txtGoogleId");
		assertThat(json).doesNotContain("txtAppleId");
		assertThat(json).doesNotContain("google-oauth-subject-id");
		assertThat(json).doesNotContain("apple-subject-id");
	}

	/**
	 * The general form. A field added here later is a field somebody has to
	 * have decided a browser may read.
	 */
	@Test
	void carriesNothingBeyondTheAllowList() {
		List<String> declared = Arrays.stream(DtoSignedInUser.class.getDeclaredFields())
				.filter(field -> !field.isSynthetic())
				.map(Field::getName)
				.collect(Collectors.toList());

		assertThat(declared).containsExactlyInAnyOrderElementsOf(ALLOWED);
	}

	/** A user the database has half-filled must not throw on the way out. */
	@Test
	void survivesAUserWithAlmostNothingOnIt() {
		DtoSignedInUser dto = DtoSignedInUser.from(new UserMaster());

		assertThat(dto).isNotNull();
		assertThat(dto.getTxtEmail()).isNull();
	}
}
