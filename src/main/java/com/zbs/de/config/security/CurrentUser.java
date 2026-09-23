package com.zbs.de.config.security;

import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.zbs.de.model.CustomerMaster;
import com.zbs.de.model.UserMaster;
import com.zbs.de.repository.RepositoryCustomerMaster;

/**
 * Resolves the caller behind the current request.
 *
 * <p>
 * Login identity ({@code user_master}) and billing identity
 * ({@code customer_master}) are separate tables joined only by email address.
 * This component is the single place that knows that, so the rest of the
 * application does not have to.
 */
@Component
public class CurrentUser {

	private final RepositoryCustomerMaster repositoryCustomerMaster;

	public CurrentUser(RepositoryCustomerMaster repositoryCustomerMaster) {
		this.repositoryCustomerMaster = repositoryCustomerMaster;
	}

	/** The authenticated login account, if any. */
	public Optional<UserMaster> user() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof UserMaster user)) {
			return Optional.empty();
		}
		return Optional.of(user);
	}

	/** The authenticated user's email, lower-cased, if any. */
	public Optional<String> email() {
		return user().map(UserMaster::getTxtEmail).filter(e -> e != null && !e.isBlank())
				.map(e -> e.toLowerCase().trim());
	}

	/** True when the caller holds {@link SecurityRoles#ADMIN}. */
	public boolean isAdmin() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated()) {
			return false;
		}
		for (GrantedAuthority authority : auth.getAuthorities()) {
			if (SecurityRoles.ADMIN.equals(authority.getAuthority())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * The customer records belonging to the authenticated user.
	 *
	 * <p>
	 * This returns a list rather than a single record because
	 * {@code customer_master.txt_email} carries no unique constraint, so duplicates
	 * exist in the live data. Every one of them legitimately belongs to this signed-in
	 * person, so all are treated as owned. Adding that unique constraint is tracked
	 * separately; until then this must not assume a single row.
	 */
	public List<CustomerMaster> customers() {
		return email().map(repositoryCustomerMaster::findByTxtEmailIgnoreCaseAndBlnIsDeletedFalse)
				.orElseGet(List::of);
	}

	/** The ids of the customer records belonging to the authenticated user. */
	public List<Integer> customerIds() {
		return customers().stream().map(CustomerMaster::getSerCustId).filter(java.util.Objects::nonNull).toList();
	}
}
