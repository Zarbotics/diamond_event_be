package com.zbs.de.model.dto;

import com.zbs.de.model.UserMaster;

import lombok.Data;

/**
 * Who is signed in, as much of it as a browser has any business knowing.
 *
 * <h3>Why this exists</h3>
 *
 * {@code GET /auth/status} returned {@code authentication.getPrincipal()},
 * which is the {@link UserMaster} entity, serialised whole. Among the nineteen
 * fields that went to the browser on every page load was
 * {@code txtPassword} — the bcrypt hash of the account's password.
 *
 * <p>
 * It went into the network tab, into anything that logs responses, and into
 * the browser console: the admin portal's header had a
 * {@code console.log('User Status Data:', ...)} on a {@code useEffect} keyed
 * to it, so the hash was printed on every render, on a machine several
 * coordinators share. The entity also carried {@code txtGoogleId},
 * {@code txtAppleId} and the audit columns, none of which any client reads.
 *
 * <p>
 * A hash is not a password, and bcrypt is slow on purpose. But it is the one
 * secret the account has, handing it out puts it within reach of anything with
 * time and a wordlist, and there was no reason to send it — the controller's
 * own comment said as much: <em>"You can create DTO for privacy if needed."</em>
 *
 * <h3>What is in it</h3>
 *
 * What the two clients actually read, and nothing else: the admin portal's
 * header wants a name, an address and a picture; the customer journey's
 * personal-details step wants an address and a first name. The role and the id
 * are here because they are already public knowledge to a signed-in client and
 * a screen may reasonably branch on them.
 */
@Data
public class DtoSignedInUser {

	private Long serUserId;
	private String txtEmail;
	private String txtName;
	private String txtFirstName;
	private String txtLastName;
	private String txtPictureUrl;
	private String txtRole;
	private Boolean blnEmailVerified;

	public static DtoSignedInUser from(UserMaster user) {
		DtoSignedInUser dto = new DtoSignedInUser();

		dto.setSerUserId(user.getSerUserId());
		dto.setTxtEmail(user.getTxtEmail());
		dto.setTxtName(user.getTxtName());
		dto.setTxtFirstName(user.getTxtFirstName());
		dto.setTxtLastName(user.getTxtLastName());
		dto.setTxtPictureUrl(user.getTxtPictureUrl());
		dto.setTxtRole(user.getTxtRole());
		dto.setBlnEmailVerified(user.getBlnEmailVerified());

		return dto;
	}
}
