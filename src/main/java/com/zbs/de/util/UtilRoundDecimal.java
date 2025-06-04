package com.zbs.de.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;

/**
 * Description: Round Decimal Utility for BTI Name of Project: BTI Created on:
 * May 11, 2017 Modified on: May 12, 2017 5:39:32 PM
 * 
 * @author seasia Version:
 */
public class UtilRoundDecimal {

	private static int SCALE = 2;
	private static String currencyFormatReg0 = "#,###,###,###,###,##0";
	private static String currencyFormatReg1 = "#,###,###,###,###,##0.0";
	private static String currencyFormatReg = "#,###,###,###,###,##0.00";
	private static String currencyFormatReg3 = "#,###,###,###,###,##0.000";
	private static String currencyFormatReg4 = "#,###,###,###,###,##0.0000";
	private static String currencyFormatReg5 = "#,###,###,###,###,##0.00000";
	private static String currencyFormatReg6 = "#,###,###,###,###,##0.000000";

	public static double roundDecimalValue(Double value) {

		if (value != null) {
			BigDecimal bigDecimal = new BigDecimal(String.valueOf(value)).setScale(getDefaultScale(),
					BigDecimal.ROUND_HALF_UP);
			return bigDecimal.doubleValue();
		} else {
			return 0.0;
		}
	}

	public static BigDecimal roundToBigDecimalValue(Double value) {

		if (value != null) {
			BigDecimal bigDecimal = new BigDecimal(value).setScale(getDefaultScale(), BigDecimal.ROUND_HALF_UP);
			return bigDecimal;
		} else {
			return new BigDecimal(0.0);
		}
	}

	public static BigDecimal roundToBigDecimalValue(Double value, int scale) {

		if (value != null) {
			BigDecimal bigDecimal = new BigDecimal(value).setScale(scale, BigDecimal.ROUND_HALF_UP);
			return bigDecimal;
		} else {
			return new BigDecimal(0.0);
		}
	}

	public static String roundStringValue(String value) {

		if (value != null && !value.isEmpty())
			return (new BigDecimal(value).setScale(getDefaultScale(), BigDecimal.ROUND_HALF_UP).toString());

		return (new BigDecimal(0.0).toString());
	}

	public static String roundStringValueforSales(String value, int roundeing) {

		if (value != null && !value.isEmpty())
			return (new BigDecimal(value).setScale(roundeing, BigDecimal.ROUND_HALF_UP).toString());

		return (new BigDecimal(0.0).toString());
	}

	public static BigDecimal setScale(BigDecimal value) {

		if (value != null) {
			return value.setScale(getDefaultScale(), BigDecimal.ROUND_HALF_UP);
		}
		return value;
	}

	public static int getDefaultScale() {

		return SCALE;
	}

	public static String currencyFormatter(String amount) {
		if (UtilRandomKey.isNotBlank(amount)) {
			DecimalFormat formatter = new DecimalFormat(currencyFormatReg);
			return formatter.format(Double.parseDouble(amount));
		}
		return "";
	}

	public static String currencyFormatter(double amount) {
		DecimalFormat formatter = new DecimalFormat(currencyFormatReg);
		return formatter.format(amount);

	}

	public static String currencyFormatter(BigDecimal amount) {
		if (amount == null) {
			amount = new BigDecimal(0);
		}
		DecimalFormat formatter = new DecimalFormat(currencyFormatReg);
		return formatter.format(amount);
	}

	public static BigDecimal currencyFormatterBigDecimal(BigDecimal amount) {
		if (amount == null) {
			amount = new BigDecimal(0);
		}
		DecimalFormat formatter = new DecimalFormat("##,###,###.00");
		return new BigDecimal(formatter.format(amount));
	}

	public static String currencyFormatterfroSales(BigDecimal amount, int value) {
		if (amount == null) {
			amount = new BigDecimal(0);
		}
		DecimalFormat formatter = new DecimalFormat(value == 0 ? currencyFormatReg0
				: value == 1 ? currencyFormatReg1
						: value == 2 ? currencyFormatReg
								: value == 3 ? currencyFormatReg
										: value == 4 ? currencyFormatReg
												: value == 5 ? currencyFormatReg
														: value == 6 ? currencyFormatReg : currencyFormatReg);
		return formatter.format(amount);
	}

	public static String currencyFormatterBdToString(BigDecimal amount) {

		DecimalFormat formatter = new DecimalFormat(currencyFormatReg);
		return formatter.format(amount);
	}

	public static Double currencyInDouble(String amount) {

		NumberFormat format = NumberFormat.getNumberInstance();
		Double value = new Double(0);
		try {
			value = format.parse(amount).doubleValue();
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
		return (value);
	}

	public static String removeCommaFromNumberString(String number) {
		String value = "";
		value = number.replaceAll("[^\\d.]", "");
		return value;
	}

	public static boolean isNumeric(String strNum) {
		if (strNum == null) {
			return false;
		}
		try {
			double d = Double.parseDouble(strNum);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	// public static void main(String[] args) {
	// // test round method
	// System.out.println(currencyFormatter("123.765"));
	// }
	/**
	 * use this method for parse String To BigDecimal if String Number having comma
	 * 
	 * @param value
	 * @return BigDecimal
	 */

	public static BigDecimal parseStringToBigDecimal(String value) {
		try {
			if (value.contains(",")) {
				DecimalFormatSymbols symbols = new DecimalFormatSymbols();
				symbols.setGroupingSeparator(',');
				symbols.setDecimalSeparator('.');
				DecimalFormat decimalFormat = new DecimalFormat(currencyFormatReg, symbols);
				decimalFormat.setParseBigDecimal(true);
				return (BigDecimal) decimalFormat.parse(value);
			} else {
				return new BigDecimal(value);
			}

		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String removeUnwantedCharacters(String input) {
		// Define the characters you want to remove inside the brackets []
		return input.replaceAll("[,\\[\\]{}()]", "");
	}
}
