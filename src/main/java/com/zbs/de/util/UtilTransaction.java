package com.zbs.de.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.NoTransactionException;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Marks the current transaction so that it cannot commit.
 *
 * <p>
 * The service layer here is written in a house style: a method does several
 * writes inside one {@code try}, and its {@code catch} logs the failure and
 * returns a {@code DtoResult} carrying an error message. That reads as though
 * the failure has been handled, and from the caller's point of view it has —
 * but Spring only rolls back on an exception that <em>propagates out</em> of
 * the transactional method. A caught one does not. The transaction therefore
 * commits, and every write that ran before the failure is made permanent while
 * the response says the operation failed.
 *
 * <p>
 * {@code ServicePriceVersionImpl.setAsDefault} shows what that costs. It clears
 * the flag on the current default price version, then sets it on the new one.
 * If the second save fails, the catch swallows it, the transaction commits, and
 * the menu is left with <em>no</em> default price version at all — so every
 * subsequent price lookup falls through. The customer sees a blank price, not
 * an error, and nothing in the log ties the two together.
 *
 * <p>
 * Calling this in the catch closes that gap: the caller still gets its error
 * message, and the database is left as it was.
 *
 * <p>
 * It is deliberately safe to call when no transaction is running. The same
 * methods are reachable from paths that were never transactional, and a helper
 * whose whole purpose is to make failure handling correct must not itself
 * become a second failure.
 */
public final class UtilTransaction {

	private static final Logger LOGGER = LoggerFactory.getLogger(UtilTransaction.class);

	private UtilTransaction() {
	}

	/**
	 * Marks the surrounding transaction rollback-only, if there is one.
	 *
	 * <p>
	 * Call this from a {@code catch} block that returns a failure result rather
	 * than rethrowing.
	 */
	public static void markRollbackOnly() {
		try {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
		} catch (NoTransactionException ignored) {
			// Nothing to roll back. The caller is handling its own error.
		}
	}

	/**
	 * Runs something once the surrounding transaction has actually committed.
	 *
	 * <p>
	 * For side effects that reach outside the database and cannot be taken back
	 * — sending an email being the obvious one. Doing that inline is wrong in a
	 * way that is easy to miss: the work has not committed yet, so a failure
	 * later in the same method rolls the change back and leaves the customer
	 * holding a confirmation for a booking that does not exist. The reverse
	 * mistake, waiting for a commit that never comes, is why this runs the task
	 * immediately when there is no transaction to wait for.
	 *
	 * <p>
	 * Failures in the task are swallowed. It runs after commit, so throwing
	 * cannot undo anything — it would only turn a completed operation into an
	 * error the caller cannot act on.
	 */
	public static void afterCommit(Runnable task) {
		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			runQuietly(task);
			return;
		}

		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				runQuietly(task);
			}
		});
	}

	private static void runQuietly(Runnable task) {
		try {
			task.run();
		} catch (Exception e) {
			LOGGER.warn("An after-commit task failed: {}", e.getMessage());
		}
	}
}
