package com.zbs.de.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UtilRandomKey {

	/** The log. */
	private static final Logger log = LoggerFactory.getLogger(UtilRandomKey.class.getName());

	/**
	 * it return the 6 digit random numeric number.
	 *
	 * @return the random order number
	 */
	public static String getRandomOrderNumber() {
		char[] chars = "1234567890".toCharArray();
		StringBuilder sb = new StringBuilder();
		Random random = new Random();
		for (int i = 0; i < 6; i++) {
			char c = chars[random.nextInt(chars.length)];
			sb.append(c);
		}
		return sb.toString();
	}

	/** The random. */
	private SecureRandom random = new SecureRandom();

	/**
	 * Next random key.
	 *
	 * @return the string
	 */
	public String nextRandomKey() {
		return new BigInteger(60, random).toString(32);
	}

	/**
	 * it generates encrypted session key.
	 *
	 * @return the string
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 */
	private static String nextRandomSessionKey() throws NoSuchAlgorithmException {
		KeyGenerator gen = KeyGenerator.getInstance("DES");
		gen.init(56); /* 56-bit DES */
		SecretKey secret = gen.generateKey();
		byte[] binary = secret.getEncoded();
		String text = String.format("%08X", new BigInteger(+1, binary));
		return text;
	}

	/**
	 * it return session key string for logged used.
	 *
	 * @return the string
	 */
	public static String generateSessionKey() {
		String key = null;
		try {
			key = nextRandomSessionKey();
		} catch (NoSuchAlgorithmException e) {
			StringWriter st = new StringWriter();
			e.printStackTrace(new PrintWriter(st));
			log.error(st.toString());
		}
		return key;
	}

	/**
	 * Checks if is not blank.
	 *
	 * @param s the s
	 * @return true, if is not blank
	 */
	public static boolean isNotBlank(final String s) {
		// Null-safe, short-circuit evaluation.
		return s != null && !s.trim().isEmpty();
	}

	/**
	 * Checks if is not blank.
	 *
	 * @param c the c
	 * @return true, if is not blank
	 */
	public static boolean isNotBlank(final Character c) {
		// Null-safe, short-circuit evaluation.
		return c != null;
	}

	public static boolean isNull(Object object) {
		return (object == null);
	}

	public static boolean isNotNull(Object object) {
		return (object != null);
	}

	public static boolean isNotNullOrNotEmpty(String object) {
		return (object != null && !object.isEmpty());
	}
}
