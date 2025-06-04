package com.zbs.de.util;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.Period;
import java.time.Year;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.springframework.util.StringUtils;

import com.zbs.de.model.dto.DtoDatePeriod;

public class UtilDateAndTime {

	private static SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);

	/**
	 * Method will return date in UTC format
	 * 
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static Date localToUTC() {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.ENGLISH);
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date gmt = new Date(sdf.format(date));
		return gmt;
	}

	@SuppressWarnings("deprecation")
	public static Date localToUTC(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.ENGLISH);
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date gmt = new Date(sdf.format(date));
		return gmt;
	}

	/**
	 * Method will convert UTC date to local date
	 * 
	 * @param date
	 * @return
	 */
	public Date utcToLocalDate(Date date) {
		String timeZone = Calendar.getInstance().getTimeZone().getID();
		Date local = new Date(date.getTime() + TimeZone.getTimeZone(timeZone).getOffset(date.getTime()));
		return local;
	}

	/**
	 * @param dateInString
	 * @return
	 * @throws ParseException
	 */
	public static Date stringToDate(String dateInString) throws ParseException {
		Date date = new Date();

		date = formatter.parse(dateInString);

		return date;
	}

	/**
	 * @param date
	 * @return
	 */
	public static Date yyyymmddStringToDate(String date) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
		try {
			if (UtilRandomKey.isNotBlank(date)) {
				return formatter.parse(date);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String yyyymmddDateToString(Date date) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
		try {
			if (UtilRandomKey.isNotNull(date)) {
				return formatter.format(date);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String mmddyyyyDateToString(Date date) {
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
		try {
			if (UtilRandomKey.isNotNull(date)) {
				return formatter.format(date);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String ddmmyyyyDateToString(Date date) {
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
		try {
			if (UtilRandomKey.isNotNull(date)) {
				return formatter.format(date);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

//	public static int daysBetweenDatesDDMMYYYYFormat(String date1, String date2) {
//		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
//		LocalDate dt1 = LocalDate.parse(date1, formatter);
//		LocalDate dt2 = LocalDate.parse(date2, formatter);
//
//		long diffDays = ChronoUnit.DAYS.between(dt1, dt2);
//
//		return Math.abs((int) diffDays);
//	}
	/**
	 * @param date
	 * @return
	 */
	public static Date ddmmyyyyStringToDate(String date) {
		if (UtilRandomKey.isNotBlank(date)) {
			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
			try {
				return formatter.parse(date);
			} catch (Exception e) {
				// e.printStackTrace();
				return yyyymmddStringToDate(date);
			}
		}
		return null;
	}

	public static Date ddmmyyyyStringToDateTime(String date) {
		if (UtilRandomKey.isNotBlank(date)) {
			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH);
			try {
				return formatter.parse(date);
			} catch (Exception e) {
				e.printStackTrace();
				return yyyymmddStringToDate(date);
			}
		}
		return null;
	}

	/**
	 * @param Date object
	 * @return formatted Date object
	 */
	public static Date ddmmyyyyStringToDate(Date date) {
		if (UtilRandomKey.isNotNull(date)) {
			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
			try {
				String dateStr = dateToStringddmmyyyy(date);
				return formatter.parse(dateStr);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static Date atStartOfDay(Date date) {
		LocalDateTime localDateTime = dateToLocalDateTime(date);
		LocalDateTime startOfDay = localDateTime.with(LocalTime.MIN);
		return localDateTimeToDate(startOfDay);
	}

	public static Date atEndOfDay(Date date) {
		LocalDateTime localDateTime = dateToLocalDateTime(date);
		LocalDateTime endOfDay = localDateTime.with(LocalTime.MAX);
		return localDateTimeToDate(endOfDay);
	}

	public static Date atStartOfDay(String date) {
		LocalDateTime localDateTime = stringToLocalDateTime(date);
		LocalDateTime startOfDay = localDateTime.with(LocalTime.MIN);
		return localDateTimeToDate(startOfDay);
	}

	public static Date atEndOfDay(String date) {
		LocalDateTime localDateTime = stringToLocalDateTime(date);
		LocalDateTime endOfDay = localDateTime.with(LocalTime.MAX);
		return localDateTimeToDate(endOfDay);
	}

	public static Date atStartOfMonth(Date date) {
		LocalDateTime localDateTime = dateToLocalDateTime(date);
		localDateTime = localDateTime.withDayOfMonth(1);
		LocalDateTime startOfMonth = localDateTime.with(LocalTime.MIN);
		return localDateTimeToDate(startOfMonth);
	}

	public static Date atEndOfMonth(Date date) {
		LocalDateTime localDateTime = dateToLocalDateTime(date);
		localDateTime = localDateTime.plusMonths(1).withDayOfMonth(1).minusDays(1);
		LocalDateTime endOfMonth = localDateTime.with(LocalTime.MAX);
		return localDateTimeToDate(endOfMonth);
	}

	public static Date atStartOfMonth(String date) {
		LocalDateTime localDateTime = stringToLocalDateTime(date);
		localDateTime = localDateTime.withDayOfMonth(1);
		LocalDateTime startOfMonth = localDateTime.with(LocalTime.MIN);
		return localDateTimeToDate(startOfMonth);
	}

	public static Date atEndOfMonth(String date) {
		LocalDateTime localDateTime = stringToLocalDateTime(date);
		localDateTime = localDateTime.plusMonths(1).withDayOfMonth(1).minusDays(1);
		LocalDateTime endOfMonth = localDateTime.with(LocalTime.MAX);
		return localDateTimeToDate(endOfMonth);
	}

	public static java.sql.Date dateToSqlDate(Date date) {
		if (UtilRandomKey.isNotNull(date)) {
			return new java.sql.Date(date.getTime());
		}
		return null;
	}

	/**
	 * @param date
	 * @return
	 */
	public static String dateToStringddmmyyyy(Date date) {
		if (UtilRandomKey.isNotNull(date)) {
			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
			try {
				return formatter.format(date);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * @param date
	 * @return
	 */
	public static String dateToStringddmmyyyyWithHHmmss(Date date) {
		if (UtilRandomKey.isNotNull(date)) {
			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH);
			try {
				return formatter.format(date);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static String dateToStringddmmyyyy(String date) {
		if (StringUtils.isEmpty(date)) {
			return null;
		}
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
		SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
		try {
			Date date1 = dbFormat.parse(date);
			return formatter.format(date1);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String dateToStringyyyymmdd(String date) {
		if (StringUtils.isEmpty(date)) {
			return null;
		}
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
		SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
		try {
			Date date1 = formatter.parse(date);
			return dbFormat.format(date1);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String dateToStringddmmyyyy(LocalDate date) {
		if (UtilRandomKey.isNotNull(date)) {
			try {
				return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static String localDateToStringyyyyMMdd(LocalDate date) {
		if (UtilRandomKey.isNotNull(date)) {
			try {
				return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static String currentLocalDateToStringyyyyMMddHHmmss() {
		try {
			LocalDateTime currentDateTime = LocalDateTime.now();
			return currentDateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss", Locale.ENGLISH));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param date
	 * @return
	 */
	public static Date yyyymmddStringToDate2(String date) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
		try {
			Date date1 = formatter.parse(date);
			return date1;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param date
	 * @return
	 */

	public static String dateToStringddmmyyyy(LocalDateTime date) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH);
		try {
			if (UtilRandomKey.isNotNull(date)) {
				return date.format(formatter);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param date
	 * @return
	 */
	public static String dateToStringyyyymmdd(Date date) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
		try {
			return formatter.format(date);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * used to convert java.util.Date to java.time.LocalDateTime
	 * 
	 * @param Date
	 * @return
	 */
	public static LocalDateTime dateToLocalDateTime(Date date) {
		if (UtilRandomKey.isNotNull(date)) {
			return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
		}
		return null;
	}

	public static Date LocalDateToDate(LocalDate dateToConvert) {
		LocalDateTime date = dateToConvert.atTime(LocalTime.MAX);
		return Date.from(date.atZone(ZoneId.systemDefault()).toInstant());
	}

	/**
	 * used to convert java.time.LocalDateTime to java.util.Date
	 * 
	 * @param LocalDateTime
	 * @return
	 */
	public static Date localDateTimeToDate(LocalDateTime date) {
		if (UtilRandomKey.isNotNull(date)) {
			return Date.from(date.atZone(ZoneId.systemDefault()).toInstant());
		}
		return null;
	}

	/**
	 * @param date
	 * @return
	 */
	public static Date ddmmyyyyStringTimeToDate(String time) {
		SimpleDateFormat formatter = new SimpleDateFormat("hh:mm aa", Locale.ENGLISH);
		try {
			Date date1 = formatter.parse(time);
			return date1;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param date
	 * @return
	 */
	public static String dateToStringhhmmaa(Date date) {
		SimpleDateFormat formatter = new SimpleDateFormat("hh:mm aa", Locale.ENGLISH);
		try {
			String formatDate = formatter.format(date);
			return formatDate;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param time
	 * @return
	 */
	public static Time getTimeFromStringFrom12Formats(String time) {
		SimpleDateFormat formatter = new SimpleDateFormat("hh:mm aa", Locale.ENGLISH);
		try {
			Date date1 = formatter.parse(time);
			return new java.sql.Time(date1.getTime());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param time
	 * @return
	 */
	public static String convertTimeToString12Formats(Time time) {
		SimpleDateFormat outPutFormat = new SimpleDateFormat("hh:mm aa", Locale.ENGLISH);
		try {
			return outPutFormat.format(time);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param date
	 * @return
	 */
	public static String convertDateDDMMYYYYFormatToDBFormat(String date) {
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
		SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
		try {
			Date date1 = formatter.parse(date);
			return dbFormat.format(date1);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param time
	 * @return
	 */
	public static String convertTimeToString24Formats(Date time) {
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
		SimpleDateFormat outPutFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
		try {
			Date date1 = formatter.parse("" + time);
			return outPutFormat.format(date1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param time
	 * @return
	 */
	public static String convertDateTimeToStringTime24FormatsWithSs(Date time) {
		SimpleDateFormat outPutFormat = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
		try {

			return outPutFormat.format(time);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String convertDateTimeToStringTime24Formats(Date time) {
		SimpleDateFormat outPutFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
		try {
			String timet = outPutFormat.format(time);
			return timet
			// +":00"
			;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String convertDateTimeToStringTime24Formats1(Date time) {
		SimpleDateFormat outPutFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
		try {
			String timet = outPutFormat.format(time);
			return timet + ":00";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param time
	 * @return
	 */
	public static Time getTimeFromStringFrom24Formats(String time) {
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
		try {
			Date date1 = formatter.parse(time);
			return new java.sql.Time(date1.getTime());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param time
	 * @return
	 */
	public static Date getStaticDateWithTimeFromTime24Formats(String time) {
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
		try {
			Date date1 = formatter.parse(time);
			return date1;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param date
	 * @param checkTime
	 * @return
	 */
	public static Date concatinatingCheckDateWithTime(Date checkDate, String checkTime) {
		return concatinatingCheckDateWithTime(dateToStringddmmyyyy(checkDate), checkTime);
	}

	/**
	 * @param date
	 * @param checkTime
	 * @return
	 */
	public static Date concatinatingCheckDateWithTime(String checkDate, String checkTime) {
		if (UtilRandomKey.isNotBlank(checkTime)) {
			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ENGLISH);
			try {
				Date date1 = formatter.parse(checkDate + " " + checkTime);
				return date1;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * @param date
	 * @return
	 */
	public static Date ddmmyyyyhhmmssStringToDate(String date) {
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH);
		try {
			Date date1 = formatter.parse(date);
			return date1;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param date
	 * @return 2020-06-19 00:00:00.0
	 */
	public static String ddmmyyyyhhmmssStringToLocalDateTime(LocalDateTime date) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
		String formattedDateTime = date.format(formatter);
		return formattedDateTime;
	}

	public static Date yyyymmddhhmmssStringToDate(String date) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S", Locale.ENGLISH);
		try {
			Date date1 = formatter.parse(date);
			return date1;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 
	 * @param dateString "01/01/2023"
	 * @return "2023-01-01"
	 */
	public static Date getDateFormate(String dateString) {
		LocalDate localDate = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
		return java.sql.Date.valueOf(localDate);
	}

	public static String ddmmyyyyhhmmssStringToLocalSADateTime(Date date) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
			// sdf.setTimeZone(TimeZone.getTimeZone("Asia/Riyadh"));
			return (sdf.format(date));
		} catch (Exception e) {
			try {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
				// sdf.setTimeZone(TimeZone.getTimeZone("Asia/Riyadh"));
				return (sdf.format(date));
			} catch (Exception e2) {
				e.printStackTrace();
			}
		}

		return null;

	}

	public static String ddmmyyyyhhmmssStringTransactionDateTime(Date transactionDate, Date createdDate) {

		try {
			String time = UtilDateAndTime.convertTime(transactionDate);
			if (time.equals("00:00:00"))
				time = UtilDateAndTime.convertTime(createdDate);

			String date = UtilDateAndTime.yyyymmddDateToString(transactionDate);
			Date oldFormatDate = (new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")).parse(date + " " + time);
			return ((new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")).format(oldFormatDate).toString());

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	public static String convertTime(Date transactionDate) {
		SimpleDateFormat localDateFormat = new SimpleDateFormat("HH:mm:ss");
		String time = localDateFormat.format(transactionDate);
		return (time);
	}

	public static Date ddmmyyyyhhmmssDateTransactionDateTime(Date transactionDate, Date createdDate) {

		try {
			String time = UtilDateAndTime.convertTime(transactionDate);
			if (time.equals("00:00:00"))
				time = UtilDateAndTime.convertTime(createdDate);

			String date = UtilDateAndTime.yyyymmddDateToString(transactionDate);
			return ((new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")).parse(date + " " + time));

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	public static Date ddmmyyyyhhmmssStringToDateTimeZone(String date) {
		if (UtilRandomKey.isNotBlank(date)) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
			try {
				Date date1 = formatter.parse(date);
				return date1;
			} catch (Exception e) {
//				e.printStackTrace();
				return ddmmyyyyStringToDate(date);
			}
		}
		return null;
	}

	public static String ddmmyyyyhhmmssStringTransactionDateTime(String strTransactionDate, String strCreatedDate) {

		try {
			Date transactionDate = UtilDateAndTime.ddmmyyyyhhmmssStringToDateTimeZone(strTransactionDate);
			Date createdDate = UtilDateAndTime.ddmmyyyyhhmmssStringToDateTimeZone(strCreatedDate);

			String time = UtilDateAndTime.convertTime(transactionDate);
			if (time.equals("00:00:00"))
				time = UtilDateAndTime.convertTime(createdDate);

			String date = UtilDateAndTime.yyyymmddDateToString(transactionDate);
			Date oldFormatDate = (new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")).parse(date + " " + time);
			return ((new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")).format(oldFormatDate).toString());

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	/**
	 * 
	 * @param String date Format yyyy-MM-dd HH:mm:ss
	 * @return Util Date
	 */
	public static Date getDateFromString(String date) {
		try {
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public static String localDateTimeToyyyymmddhhmmssString(LocalDateTime date) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		String formattedDateTime = date.format(formatter);
		return formattedDateTime;
	}

	public static String convertTimeToString12Formats(Date time) {
		SimpleDateFormat outPutFormat = new SimpleDateFormat("hh:mm aa", Locale.ENGLISH);
		try {
			return outPutFormat.format(time);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String convertDateToStringTime24Formats(Date date) {
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
		try {
			String HHMMSS = formatter.format(date);
			return HHMMSS;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Integer getWeekDayNumber(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
		return dayOfWeek;
	}

	public static boolean chechCurrentTimeLiesBetweenTwoTimes(String startTime, String endTime, String currentTime) {
		try {
			Date time1 = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).parse(startTime);
			Calendar calendar1 = Calendar.getInstance();
			calendar1.setTime(time1);

			Date time2 = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).parse(endTime);
			Calendar calendar2 = Calendar.getInstance();
			calendar2.setTime(time2);
			calendar2.add(Calendar.DATE, 1);

			Date d = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).parse(currentTime);
			Calendar calendar3 = Calendar.getInstance();
			calendar3.setTime(d);
			calendar3.add(Calendar.DATE, 1);

			Date x = calendar3.getTime();
			if (x.after(calendar1.getTime()) && x.before(calendar2.getTime())) {
				// checkes whether the current time is between 14:49:00 and 20:11:13.
				return true;
			}

		} catch (ParseException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static Date startDate(Integer noOfDays, Date hireDate) {
		Calendar c = Calendar.getInstance();
		if (noOfDays != 0) {
			c.setTime(hireDate); // Now hire date.
			c.add(Calendar.DATE, noOfDays); // Adding days
			hireDate = c.getTime();
		}
		return hireDate;
	}

	public static Date endDate(Integer noOfDays, Integer endDateDays, Date hireDate) {
		Calendar c = Calendar.getInstance();
		if (noOfDays != 0) {
			c.setTime(hireDate); // Now hire date.
			c.add(Calendar.DATE, noOfDays); // Adding days
			hireDate = c.getTime();
		}
		if (endDateDays != 0) {
			c.setTime(hireDate);
			c.add(Calendar.DATE, endDateDays);
			hireDate = c.getTime();

		}
		return hireDate;
	}

	public static Long startDate2(Integer noOfDays, Date hireDate) {
		Calendar c = Calendar.getInstance();
		if (noOfDays != 0) {
			c.setTime(hireDate); // Now hire date.
			c.add(Calendar.DATE, noOfDays); // Adding days
			hireDate = c.getTime();
		}
		return hireDate.getTime();
	}

	public static Long endDate2(Integer noOfDays, Integer endDateDays, Date hireDate) {
		Calendar c = Calendar.getInstance();
		if (noOfDays != 0) {
			c.setTime(hireDate); // Now hire date.
			c.add(Calendar.DATE, noOfDays); // Adding days
			hireDate = c.getTime();
		}
		if (endDateDays != 0) {
			c.setTime(hireDate);
			c.add(Calendar.DATE, endDateDays);
			hireDate = c.getTime();

		}
		return hireDate.getTime();
	}

	public static int getDiffInYears(Date first, Date last) {
		Calendar a = getCalendar(first);
		Calendar b = getCalendar(last);
		int diff = b.get(Calendar.YEAR) - a.get(Calendar.YEAR);
		if (a.get(Calendar.MONTH) > b.get(Calendar.MONTH)
				|| (a.get(Calendar.MONTH) == b.get(Calendar.MONTH) && a.get(Calendar.DATE) > b.get(Calendar.DATE))) {
			diff--;
		}
		return diff;
	}

	public static Calendar getCalendar(Date date) {
		Calendar cal = Calendar.getInstance(Locale.US);
		cal.setTime(date);
		return cal;
	}

	public static long differenceOfDaysbetweenDates(Date firstDate, Date secondDate) throws IOException {
		return ChronoUnit.DAYS.between(firstDate.toInstant(), secondDate.toInstant());
	}

	/* default format to follow */
	public static LocalDateTime stringToLocalDateTime(String date) {
		if (UtilRandomKey.isNotBlank(date)) {

			return dateToLocalDateTime(ddmmyyyyStringToDate(date));
		}
		return null;
	}

	/* default format to follow */
	public static LocalDateTime stringDdmmyyyyStringToLocalDateTime(String date) {
		if (UtilRandomKey.isNotBlank(date)) {

			return dateToLocalDateTime(ddmmyyyyStringToDate(date));
		}
		return null;
	}

	public static LocalDate dateToLocalDate(Date date) {

		return new java.sql.Date(date.getTime()).toLocalDate();

	}

//	DD/MM/yyyy = default date or application date format 
	public static Date parseAnyDateToAppDate(String date) {

		Date dateObj = null;
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
			dateObj = formatter.parse(date);
		} catch (Exception e) {
			try {
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
				dateObj = formatter.parse(date);
			} catch (Exception a) {
				a.printStackTrace();
			}
		}

		return dateObj;

	}

	public static Date copyTimeToDate(Date date, Date time) {
		Calendar t = Calendar.getInstance();
		t.setTime(time);

		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.HOUR_OF_DAY, t.get(Calendar.HOUR_OF_DAY));
		c.set(Calendar.MINUTE, t.get(Calendar.MINUTE));
		c.set(Calendar.SECOND, t.get(Calendar.SECOND));
		c.set(Calendar.MILLISECOND, t.get(Calendar.MILLISECOND));
		return c.getTime();
	}

	public static String convertDateToDateTimeString(Date date) {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH).format(date);
	}

	public static String convertDateToDateTimeString1(Date date) {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(date);
	}

	public static String getTimeDifference(Date startDate, Date endDate) {
		String time = "";
		long differenceInMillis = endDate.getTime() - startDate.getTime();
		long differenceInSeconds = (differenceInMillis / 1000) % 60;
		long differenceInMinutes = (differenceInMillis / (1000 * 60)) % 60;

		time = "Total Time (in Millis): " + differenceInMillis + " ~ (in Secs): " + differenceInSeconds
				+ " ~ (in Mins): " + differenceInMinutes;

		return time;
	}

	public static long getTimeDifferenceInMillis(Date startDate, Date endDate) {
		return endDate.getTime() - startDate.getTime();
	}

	public static long convertMiliisToMinutes(long millis) {
		return (millis / 60000);
	}

	public static long convertMinutesToMiliis(long mins) {
		return (mins * 60000);
	}

	public static long getTimeDifferenceInSeconds(Date startDate, Date endDate) {
		return ((endDate.getTime() - startDate.getTime()) / 1000);
	}

	public static long getTimeDifferenceInMinutes(Date startDate, Date endDate) {
		return ((endDate.getTime() - startDate.getTime()) / (1000 * 60));
	}

	public static long getTimeDifferenceInHours(Date startDate, Date endDate) {
		return ((endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60));
	}

	public static long getTimeDifferenceInDays(Date startDate, Date endDate) {
		return ((endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24));
	}

	public static boolean getBooleanIfStartAndEndDateIsOfTheYearStartAndEnd(LocalDate startDate, LocalDate endDate) {
		if (startDate.getYear() == endDate.getYear() && startDate.getMonthValue() == 1 && endDate.getMonthValue() == 12
				&& startDate.getDayOfMonth() == 1 && endDate.getDayOfMonth() == 31) {
			return true;
		}
		return false;
	}

	public static String getTotalTimeDifferenceInUnits(Date startDate, Date endDate) {
		String time = "";
		long days = getTimeDifferenceInDays(startDate, endDate);
		long hours = getTimeDifferenceInHours(startDate, endDate);
		long mins = getTimeDifferenceInMinutes(startDate, endDate);
		long secs = getTimeDifferenceInSeconds(startDate, endDate);
		long millis = getTimeDifferenceInMillis(startDate, endDate);

		if (days > 0) {
			time = "(in days): " + days + " days, ~ ";
		}

		if (hours > 0) {
			time = time + "(in hours): " + hours + " hours, ~ ";
		}

		if (mins > 0) {
			time = time + "(in mins): " + mins + " mins, ~ ";
		}

		if (secs > 0) {
			time = time + "(in seconds): " + secs + " seconds, ~ ";
		}

		if (millis > 0) {
			time = time + "(in milli seconds): " + millis + " millis";
		}

		return time;
	}

	public static Date dateWithoutTime(Date date) {
		DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		try {
			return formatter.parse(formatter.format(date));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Date getFirstDayOfYear(Date date) {
		LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		LocalDate firstDay = localDate.withDayOfYear(1);
		return Date.from(firstDay.atStartOfDay(ZoneId.systemDefault()).toInstant());
	}

	public static boolean isLastDayOfYear(Date date) {
		LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		LocalDate lastDay = localDate.withDayOfYear(localDate.lengthOfYear());
		return localDate.equals(lastDay);
	}

	public static boolean isLastDayOfMonth(Date date) {
		LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		LocalDate lastDay = localDate.withDayOfMonth(localDate.lengthOfMonth());
		return localDate.equals(lastDay);
	}

	/**
	 * This method will be used to get the date and time with the
	 * <code>Locale.ENGLISH</code>
	 * 
	 * @return <code>Date</code>
	 */
	public static Date getCurrentDate() {

		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.ENGLISH);
		try {
			date = sdf.parse(sdf.format(date));
		} catch (ParseException e) {
			e.printStackTrace();
		}

		if (UtilRandomKey.isNotNull(date)) {
			return (date);
		}
		return null;
	}

	public static Date getCurrentDateWithOutTime() {

		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
		try {
			date = sdf.parse(sdf.format(date));
		} catch (ParseException e) {
			e.printStackTrace();
		}

		if (UtilRandomKey.isNotNull(date)) {
			return (date);
		}
		return null;
	}

	@SuppressWarnings("deprecation")
	public static Date getFirstDayOfMonth(Date date) {
		Calendar result = Calendar.getInstance();
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, date.getYear() + 1900);
		calendar.set(Calendar.MONTH, date.getMonth());
		calendar.set(Calendar.DATE, date.getDate());

		result.set(Calendar.DATE, calendar.getActualMinimum(Calendar.DATE));
		result.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
		result.set(Calendar.YEAR, calendar.get(Calendar.YEAR));

		return dateWithoutTime(result.getTime());
	}

	public static Date getLastDayOfMonth(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.MONTH, 1);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		calendar.add(Calendar.DATE, -1);
		Date lastDayOfMonth = dateWithoutTime(calendar.getTime());
		return lastDayOfMonth;
	}

	public static Date getPreviousDayDate(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_YEAR, -1);
		Date previousDate = calendar.getTime();
		return previousDate;
	}

	public static String convertSecondsToHMmSs(long seconds) {
		long s = seconds % 60;
		long m = (seconds / 60) % 60;
		long h = (seconds / (60 * 60)) % 24;
		return String.format("%d:%02d:%02d", h, m, s);
	}

	public static int calculateAgeFromDateOfBirth(Date dateOfBirth) throws ParseException {

		Calendar c = Calendar.getInstance();
		c.setTime(dateOfBirth);
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH) + 1;
		int date = c.get(Calendar.DATE);
		LocalDate l1 = LocalDate.of(year, month, date);
		LocalDate now1 = LocalDate.now();
		Period diff1 = Period.between(l1, now1);

		return diff1.getYears();
	}

	public static String getDateToStringDateDDMMYYYY(Date d) {
		SimpleDateFormat form = new SimpleDateFormat("dd/MM/yyyy");
//	    System.out.println(form.format(d));
		String str = form.format(d);
		return str;
	}

	public static int getMonthFromDate(Date d) {
		LocalDate currentLocalDate = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		int currentMonth = currentLocalDate.getMonthValue();
		return currentMonth;
	}

	public static String getMonthNameFromDate(Date d) {
		if (d == null)
			return null;

		LocalDate currentLocalDate = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		return currentLocalDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH); // "Jan", "Feb", etc.
	}

	public static long getDifferenceOfDaysFromTwoStringddmmyyyyy(String stringDateddmmyyyy1,
			String stringDateddmmyyyy2) {
		LocalDateTime date1 = UtilDateAndTime.stringToLocalDateTime(stringDateddmmyyyy1);
		LocalDateTime date2 = UtilDateAndTime.stringToLocalDateTime(stringDateddmmyyyy2);
		return Duration.between(date1, date2).toDays();
	}

	public static String yyyymmddhhmmssToddmmyy(Date src) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		SimpleDateFormat formatterDisplay = new SimpleDateFormat("dd/MM/yyyy");
		try {
			Date date = formatter.parse(src.toString());
			return formatterDisplay.format(date);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static int getSeconds() {
		Calendar calendar = Calendar.getInstance();
		return calendar.get(Calendar.SECOND);
	}

	public static Duration parseStrDuration(String strDuration) {

		String[] arr = strDuration.split(":");
		String strIsoDuration = "PT" + arr[0] + "H" + arr[1] + "M" + arr[2] + "s";
		return Duration.parse(strIsoDuration);
	}

	public static String formatDurationJava8Plus(Duration duration) {
		return String.format("%02d:%02d:%02d", duration.toHours(), duration.toMinutes() % 60,
				duration.getSeconds() % 60);
	}

	public static int daysBetweenDatesYYYYMMDDFormat(String date1, String date2) {
		LocalDate dt1 = LocalDate.parse(date1);
		LocalDate dt2 = LocalDate.parse(date2);

		long diffDays = ChronoUnit.DAYS.between(dt1, dt2);

		return Math.abs((int) diffDays);
	}

	public static int daysBetweenDatesDDMMYYYYFormat(String date1, String date2) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		LocalDate dt1 = LocalDate.parse(date1, formatter);
		LocalDate dt2 = LocalDate.parse(date2, formatter);

		long diffDays = ChronoUnit.DAYS.between(dt1, dt2);

		return Math.abs((int) diffDays);
	}

	public static Date yesterdayDate(boolean endTime) {
		final Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		if (endTime)
			return atEndOfDay(cal.getTime());
		else
			return cal.getTime();
	}

	public static String yyyymmddhhmmssTommddyy(Date src) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		SimpleDateFormat formatterDisplay = new SimpleDateFormat("MM-dd-yyyy");
		try {
			Date date = formatter.parse(src.toString());
			return formatterDisplay.format(date);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Date mmddyyyyhhmmssStringToDate(String date) {

		Date date1;
		try {
			String pattern = "MM-dd-yyyy";
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
			date1 = simpleDateFormat.parse(date);
			return date1;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public static String getCurrentTime() {
		DateFormat dateFormat = new SimpleDateFormat("HH:mm");
		Date date = new Date();
		return dateFormat.format(date);

	}

	public static String getTime(Date date) {
		DateFormat dateFormat = new SimpleDateFormat("HH:mm");
		return dateFormat.format(date);

	}

	public static String getTime12Hours(Date date) {
		DateFormat dateFormat = new SimpleDateFormat("hh:mm a");
		return dateFormat.format(date);
	}

	public static String getCurrentDay() {
		Calendar c = Calendar.getInstance();
		Date date = new Date();
		c.setTime(date);
		return new SimpleDateFormat("EEEE").format(date);

	}

	public static Calendar getCalendarWithoutTime(Date date) {
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar;
	}

	public static Date findingPreviousDateFromProvidedDate(Date date) {
		// create a Calendar object and set it to today's date
		Calendar calendar = Calendar.getInstance();
		try {
			calendar.setTime(date);

			// subtract one day from the calendar
			calendar.add(Calendar.DAY_OF_MONTH, -1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// get the updated date from the calendar
		return calendar.getTime();
	}

	public static Date findingNextDayDateFromProvidedDate(Date date) {
		// create a Calendar object and set it to today's date
		Calendar calendar = Calendar.getInstance();
		try {
			calendar.setTime(date);

			// subtract one day from the calendar
			calendar.add(Calendar.DAY_OF_MONTH, +1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// get the updated date from the calendar
		return calendar.getTime();
	}

	/**
	 * Converts a date string in the format "MM/dd/yyyy" to a {@code Date} object
	 * with the current time. The resulting {@code Date} object represents the
	 * combination of the parsed date and the current time.
	 *
	 * @param date A string representing the date in the format "MM/dd/yyyy".
	 * @return A {@code Date} object representing the combined date and time.
	 * @throws DateTimeParseException If the input date string is not in the
	 *                                expected format.
	 * @see java.text.SimpleDateFormat
	 * @see java.util.Date
	 * @see java.time.LocalDateTime
	 * @see java.time.LocalDate
	 * @see java.time.LocalTime
	 * @see java.time.ZoneId
	 */
	public static Date getDDMMYYYHHMMSSDate(String date) {
		DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

		// Parse the input string to a LocalDateTime object
		LocalDateTime localDateTime = LocalDateTime.of(LocalDate.parse(date, inputFormatter), LocalTime.MIDNIGHT);

		// Get the current date and time
		LocalDateTime currentDateTime = LocalDateTime.now();

		// Combine the date from the input with the current time
		LocalDateTime combinedDateTime = localDateTime.withHour(currentDateTime.getHour())
				.withMinute(currentDateTime.getMinute()).withSecond(currentDateTime.getSecond());

		// Convert LocalDateTime to Date
		return Date.from(combinedDateTime.atZone(ZoneId.systemDefault()).toInstant());
	}

	/**
	 * 
	 * */
	public static BigDecimal calcuatingPerMonth(Date startEnd, Date endDate, boolean isStartDateIsIncluded) {
		if (isStartDateIsIncluded)
			endDate = findingNextDayDateFromProvidedDate(endDate);
		LocalDate date1 = UtilDateAndTime.dateToLocalDate(startEnd);
		LocalDate date2 = UtilDateAndTime.dateToLocalDate(endDate);
		Period period = Period.between(date1, date2);

		double years = period.getYears();
		double months = period.getMonths();
		double days = period.getDays();
		double daysPerMonth = 0;
		if (days > 0) {
			daysPerMonth = ((days) / 365) * (12);
		}
		double perMonth = (years * 12) + months + (daysPerMonth);
		System.out.println(perMonth + "-----------");
		return BigDecimal.valueOf(perMonth).setScale(2, RoundingMode.HALF_UP);
	}

	/*
	 * Gets the start date of a specified week in Arabic locale for a given year.
	 *
	 * @param year The year for which the week's start date is calculated.
	 * 
	 * @param weekNumber The week number within the specified year.
	 * 
	 * @return The start date of the specified week in Arabic locale as a {@link
	 * Date} object.
	 */
	public static Date getStartDateOfWeekinArabic(Integer year, Integer weekNumber) {
		LocalDate firstDayOfYear = LocalDate.of(year, 1, 1);
		LocalDate firstDayOfWeek = firstDayOfYear.with(TemporalAdjusters.previousOrSame(DayOfWeek.SATURDAY))
				.plusWeeks(weekNumber - 1);
		Instant instant = firstDayOfWeek.atStartOfDay(ZoneId.systemDefault()).toInstant();
		return Date.from(instant);
	}

	/**
	 * Gets the end date of a specified week in Arabic locale for a given year.
	 *
	 * @param year       The year for which the week's end date is calculated.
	 * @param weekNumber The week number within the specified year.
	 * @return The end date of the specified week in Arabic locale as a {@link Date}
	 *         object.
	 */
	public static Date getEndDateOfWeekInArabic(Integer year, Integer weekNumber) {
		LocalDate firstDayOfYear = LocalDate.of(year, 1, 1);
		LocalDate firstDayOfWeek = firstDayOfYear.with(TemporalAdjusters.previousOrSame(DayOfWeek.SATURDAY))
				.plusWeeks(weekNumber - 1);
		LocalDate lastDayOfWeek = firstDayOfWeek.plusDays(6);
		Instant instant = lastDayOfWeek.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant();
		return Date.from(instant);
	}

	/**
	 * Subtracts the specified number of days from the given date.
	 *
	 * @param date The original date.
	 * @param days The number of days to subtract.
	 * @return A new {@code Date} object representing the result of subtracting the
	 *         specified days.
	 */
	public static Date subtractDaysFromDate(Date date, Long days) {
		LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		LocalDate resultLocalDate = localDate.minusDays(days);
		return Date.from(resultLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
	}

	/**
	 * Adds the specified number of days to the given date.
	 *
	 * @param date The original date.
	 * @param days The number of days to add.
	 * @return A new {@code Date} object representing the result of adding the
	 *         specified days.
	 */
	public static Date addDaysWithDate(Date date, Long days) {
		LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		LocalDate resultLocalDate = localDate.plusDays(days);
		return Date.from(resultLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
	}

	/**
	 * Adds the specified number of days to the given date.
	 *
	 * @param date The original date.
	 * @param days The number of minutes to add.
	 * @return A new {@code Date} object representing the result of adding the
	 *         specified days.
	 */
	public static Date addMinutesWithDate(Date date, Long minute) {
		LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
		// Add 5 minutes to LocalDateTime
		LocalDateTime newDateTime = localDateTime.plusMinutes(minute);
		// Convert LocalDateTime back to Date
		Date newDate = Date.from(newDateTime.atZone(ZoneId.systemDefault()).toInstant());
		return newDate;
	}

	/**
	 * Adds the specified number of seconds to the given date.
	 *
	 * @param date The original date.
	 * @param days The number of second to add.
	 * @return A new {@code Date} object representing the result of adding the
	 *         specified second.
	 */
	public static Date addSecondWithDate(Date date, Long second) {
		LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
		LocalDateTime newDateTime = localDateTime.plusSeconds(second);
		// Convert LocalDateTime back to Date
		Date newDate = Date.from(newDateTime.atZone(ZoneId.systemDefault()).toInstant());
		return newDate;
	}

	/**
	 * Subtracts one year from the given date.
	 *
	 * @param date The original date.
	 * @return A new {@code Date} object representing the result of subtracting one
	 *         year.
	 */
	public static Date subtractYear(Date date) {
		LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		LocalDate resultLocalDate = localDate.minusYears(1);
		return Date.from(resultLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
	}

	/**
	 * Subtracts one year from the given date.
	 *
	 * @param date The original date.
	 * @return A new {@code Date} object representing the result of subtracting one
	 *         year.
	 */
	public static Date addYear(Date date, Long year) {
		LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		LocalDate resultLocalDate = localDate.plusYears(year);
		return Date.from(resultLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
	}

	/**
	 * Converts a date string in the "yyyy-MM-dd" format to a {@code java.util.Date}
	 * object.
	 *
	 * @param date A string representing a date in the "yyyy-MM-dd" format.
	 * @return A {@code java.util.Date} object representing the parsed date.
	 * @throws RuntimeException If an error occurs during parsing or conversion.
	 */
	public static Date yyyMMDDFromStringtoDate(String date) {
		try {
			LocalDate localDate = LocalDate.parse(date);

			return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}

	}

	/**
	 * Gets the start date of the specified year.
	 *
	 * @param year The year for which to get the start date.
	 * @return The start date of the specified year as a {@code Date} object.
	 */
	public static Date getStartDateFromYear(Integer year) {
		LocalDate startDate = Year.of(year).atDay(1);
		Instant instant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
		return Date.from(instant);
	}

	/**
	 * Gets the end date of the specified year.
	 *
	 * @param year The year for which to get the end date.
	 * @return The end date of the specified year as a {@code Date} object.
	 */
	public static Date getEndDateFromYear(Integer year) {
		LocalDate endDate = Year.of(year).atDay(Year.of(year).length());
		Instant instant = endDate.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant();
		return Date.from(instant);
	}

	/**
	 * Generates a list of date periods representing previous weeks, months, or
	 * quarters based on the given date and period number.
	 *
	 * @param date         The input date in the format "dd/MM/yyyy".
	 * @param periodNumber The number of previous periods to generate.
	 * @param periodType   The type of period to generate ("WEEK", "MONTH", or
	 *                     "QUARTER").
	 * @return A list of {@code DtoDatePeriod} objects representing the start and
	 *         end dates of each period.
	 */
	public static List<DtoDatePeriod> getListOfPeriods(String date, Integer periodNumber, String periodType) {
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		LocalDate givenDate = LocalDate.parse(date, dateFormatter);
		List<DtoDatePeriod> finalPreviousPeriodsStartDates = new ArrayList<>();

		if (periodType.equals("QUARTER")) {
			givenDate = givenDate.minusMonths(3).withDayOfMonth(1);
		}
		List<DtoDatePeriod> previousPeriodsStartDates = new ArrayList<>();
		for (int i = 1; i <= periodNumber; i++) {
			DtoDatePeriod datePeriod = new DtoDatePeriod();
			LocalDate startDate = null;
			LocalDate endDate = null;

			switch (periodType.toUpperCase()) {
			case "WEEK":
				LocalDate currentDate = LocalDate.now();
				int result = givenDate.compareTo(currentDate);
				startDate = givenDate.minusWeeks(i).with(DayOfWeek.SATURDAY);
				if (result == 0 || result > 0) {
					startDate = startDate.minusDays(6);
				}
				endDate = startDate.plusDays(6);
				datePeriod.setPeriod("WEEK " + startDate.get(WeekFields.ISO.weekOfWeekBasedYear()));
				datePeriod.setPeriodNumber(startDate.get(WeekFields.ISO.weekOfWeekBasedYear()));
				break;
			case "MONTH":
				startDate = givenDate.minusMonths(i).withDayOfMonth(1);
				endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());
				datePeriod.setPeriod("MONTH " + startDate.getMonth() + " " + startDate.getYear());
				datePeriod.setPeriodNumber(startDate.getMonth().getValue());
				break;
			case "QUARTER":
				startDate = givenDate.minusMonths((i - 1) * 3).withDayOfMonth(1);
				endDate = startDate.plusMonths(3).with(TemporalAdjusters.lastDayOfMonth());
				datePeriod.setPeriod(startDate.getYear() + "");
				datePeriod.setPeriodNumber(((startDate.getMonthValue() - 1) / 3 + 1));
				break;
			default:
				// Handle unsupported period types or throw an exception
				throw new IllegalArgumentException("Unsupported period type: " + periodType);
			}

			Instant fromInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
			Instant toInstant = endDate.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant();

			Date fromDate = Date.from(fromInstant);
			Date toDate = Date.from(toInstant);
			datePeriod.setFromDate(fromDate);
			datePeriod.setToDate(toDate);

			datePeriod.setFromDateStr(UtilDateAndTime.dateToStringddmmyyyy(fromDate));
			datePeriod.setToDateStr(UtilDateAndTime.dateToStringddmmyyyy(toDate));
			previousPeriodsStartDates.add(datePeriod);
		}
		int sno = 0;
		for (int i = previousPeriodsStartDates.size() - 1; i >= 0; i--) {
			DtoDatePeriod dtoDatePeriod = previousPeriodsStartDates.get(i);
			dtoDatePeriod.setCount(++sno);
			finalPreviousPeriodsStartDates.add(dtoDatePeriod);
		}
		return finalPreviousPeriodsStartDates;
	}

	/**
	 * Gets the start date of the current week.
	 *
	 * @return The start date of the current week as a {@code Date}.
	 */
	public static Date getCurrentWeekStartDate() {
		LocalDate currentDate = LocalDate.now();
		LocalDate weekStartDate = currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SATURDAY)).plusDays(1);
		Instant instant = weekStartDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
		return Date.from(instant);
	}

	/**
	 * Gets the end date of the current week.
	 *
	 * @return The end date of the current week as a {@code Date}.
	 */
	public static Date getCurrentWeekEndDate() {
		LocalDate currentDate = LocalDate.now();
		LocalDate firstDayOfWeek = currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SATURDAY)).plusDays(1);
		LocalDate lastDayOfWeek = firstDayOfWeek.plusDays(6);
		Instant instant = lastDayOfWeek.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant();
		return Date.from(instant);
	}

	/**
	 * Gets the start date of the current month.
	 *
	 * @return The start date of the current month as a {@code Date}.
	 */
	public static Date getCurrentMothStartDate() {
		LocalDate currentDate = LocalDate.now();
		LocalDate monthStartDate = currentDate.with(TemporalAdjusters.firstDayOfMonth());
		Instant instant = monthStartDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
		return Date.from(instant);
	}

	/**
	 * Gets the end date of the current month.
	 *
	 * @return The end date of the current month as a {@code Date}.
	 */
	public static Date getCurrentMothEndDate() {
		LocalDate currentDate = LocalDate.now();
		LocalDate monthEndDate = currentDate.with(TemporalAdjusters.lastDayOfMonth());
		Instant instant = monthEndDate.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant();
		return Date.from(instant);

	}

	/**
	 * Gets the start date of the current quarter.
	 *
	 * @return The start date of the current quarter as a {@code Date}.
	 */
	public static Date getCurrentQuarterStartDate() {
		LocalDate currentDate = LocalDate.now();
		int currentQuarter = (currentDate.getMonthValue() - 1) / 3 + 1;
		LocalDate quarterStartDate = LocalDate.of(currentDate.getYear(), (currentQuarter - 1) * 3 + 1, 1);
		Instant instant = quarterStartDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
		return Date.from(instant);
	}

	/**
	 * Gets the end date of the current quarter.
	 *
	 * @return The end date of the current quarter as a {@code Date}.
	 */
	public static Date getCurrentQuarterEndDate() {
		LocalDate currentDate = LocalDate.now();
		int currentQuarter = (currentDate.getMonthValue() - 1) / 3 + 1;
		LocalDate quarterStartDate = LocalDate.of(currentDate.getYear(), (currentQuarter - 1) * 3 + 1, 1);
		LocalDate quarterEndDate = quarterStartDate.plusMonths(2).with(TemporalAdjusters.lastDayOfMonth());
		Instant instant = quarterEndDate.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant();
		return Date.from(instant);

	}

	/**
	 * Gets the current week number based on the week-of-year field.
	 *
	 * @return The current week number.
	 */
	public static int getCurrentWeekNumber() {
		LocalDate currentDate = LocalDate.now();
		return currentDate.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
	}

	/**
	 * Gets the current month number and name.
	 *
	 * @return A string containing the current month name.
	 */
	public static String getCurrentMonthName() {
		LocalDate currentDate = LocalDate.now();
		Month currentMonth = currentDate.getMonth();
		String monthName = currentMonth.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.getDefault());
		return "Month: " + monthName;
	}

	public static int getCurrentMonthNumber() {
		LocalDate currentDate = LocalDate.now();
		Month currentMonth = currentDate.getMonth();
		return currentMonth.getValue();
	}

	/**
	 * Gets the current quarter number based on the month.
	 *
	 * @return The current quarter number.
	 */
	public static int getCurrentQuarter() {
		LocalDate currentDate = LocalDate.now();
		Month currentMonth = currentDate.getMonth();
		return (currentMonth.getValue() - 1) / 3 + 1;
	}

	/**
	 * Parses the input date string and returns the last date of the month as a
	 * {@code Date} object.
	 *
	 * @param dateStr A string representing a date in the format "dd-MM-yyyy".
	 * @return The last date of the month for the given input date string.
	 * @throws DateTimeParseException If the input string is not in the expected
	 *                                format.
	 */
	public static Date getDateLastDayOfTheMonth(String dateStr) {
		// Define the date format pattern
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		LocalDate givenDate = LocalDate.parse(dateStr, dateFormatter);
		// Get the last day of the month
		LocalDate lastDayOfMonth = givenDate.withDayOfMonth(givenDate.lengthOfMonth());

		// Convert the LocalDate to Date
		Instant instant = lastDayOfMonth.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant();
		return Date.from(instant);
	}

	/**
	 * Gets the start date of the previous year from the current date.
	 *
	 * @return The start date of the previous year.
	 */
	public static Date getPreviousYearStartDate() {
		LocalDate currentDate = LocalDate.now();
		LocalDate withDayOfYear = currentDate.minusYears(1).withDayOfYear(1);
		Instant instant = withDayOfYear.atStartOfDay(ZoneId.systemDefault()).toInstant();
		return Date.from(instant);
	}

	/**
	 * Gets the end date of the previous year from the current date.
	 *
	 * @return The end date of the previous year.
	 */
	public static Date getPreviousYearEndDate() {
		LocalDate currentDate = LocalDate.now();
		LocalDate withDayOfYear = currentDate.minusYears(1).withDayOfYear(currentDate.minusYears(1).lengthOfYear());
		Instant instant = withDayOfYear.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant();
		return Date.from(instant);
	}

	public static int getCurrentYearLastTwoDigits() {
		Calendar calendar = Calendar.getInstance();
		int currentYear = calendar.get(Calendar.YEAR);
		return currentYear % 100;
	}

	public static boolean findTimeLiesBetweenTimesFromStartTimeAndEndTimeStringFrom24Formats(String currentStart,
			String currentEnd, String newStart, String newEnd) {
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
		boolean isBetween = false;
		try {
			Date currentStartDate = formatter.parse(currentStart);
			Date currentEndDate = formatter.parse(currentEnd);
			Date newStartDate = formatter.parse(newStart);
			Date newEndDate = formatter.parse(newEnd);
			if (newStartDate.after(currentStartDate) && newStartDate.before(currentEndDate)) {
				isBetween = true;
			}
			if (newEndDate.after(currentStartDate) && newEndDate.before(currentEndDate)) {
				isBetween = true;
			}
			if (newStartDate.before(currentStartDate) && newEndDate.after(currentEndDate)) {
				isBetween = true;
			}
			if (newStartDate.equals(currentStartDate) && newEndDate.equals(currentEndDate)) {
				isBetween = true;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return isBetween;
	}

	/**
	 * Gets the month value (1 to 12) from the provided java.util.Date.
	 *
	 * @param date The java.util.Date from which to extract the month.
	 * @return The month value (1 for January, 2 for February, and so on).
	 * @throws NullPointerException If the input date is null.
	 * @since 1.0
	 */
	public static int getNumberOfMonthFromDate(Date date) {
		LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		return localDate.getMonthValue();
	}

	/**
	 * Gets the year from a {@code Date} object.
	 *
	 * @param date The input {@code Date} object.
	 * @return The year extracted from the input date.
	 * @throws NullPointerException If the provided date is null.
	 */
	public static int getYearFromDate(Date date) {
		// Convert Date to Instant
		Instant instant = date.toInstant();

		// Convert Instant to LocalDate
		LocalDate localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();

		// Extract and return the year
		return localDate.getYear();
	}

	public static String ddmmyyyyhhmmssZStringToLocalSADateTime(Date date) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
			// sdf.setTimeZone(TimeZone.getTimeZone("Asia/Riyadh"));
			return (sdf.format(date));
		} catch (Exception e) {
			try {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS'Z'", Locale.ENGLISH);
				// sdf.setTimeZone(TimeZone.getTimeZone("Asia/Riyadh"));
				return (sdf.format(date));
			} catch (Exception e2) {
				e.printStackTrace();
			}
		}

		return null;

	}

	public static String getDifference(Date startDate, Date endDate) {
		LocalDateTime startDateTime = LocalDateTime.ofInstant(startDate.toInstant(), java.time.ZoneId.systemDefault());
		LocalDateTime endDateTime = LocalDateTime.ofInstant(endDate.toInstant(), java.time.ZoneId.systemDefault());

		// Calculate the duration between the two LocalDateTime objects
		Duration duration = Duration.between(startDateTime, endDateTime);

		return formatDurationJava8Plus(duration);
	}

	public static String convertToHHMMSS(long totalSeconds) {
		totalSeconds %= 24 * 3600;
		if (totalSeconds < 0) {
			totalSeconds += 24 * 3600;
		}
		// Calculate hours, minutes, and seconds
		long hours = totalSeconds / 3600;
		long minutes = (totalSeconds % 3600) / 60;
		long seconds = totalSeconds % 60;

		// Create LocalTime object
		LocalTime time = LocalTime.of((int) hours, (int) minutes, (int) seconds);

		// Format and return the time
		return time.toString();
	}

	public static String convertToHHMM(long totalMilliSeconds) {
		DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

		return String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(totalMilliSeconds),
				TimeUnit.MILLISECONDS.toMinutes(totalMilliSeconds)
						- TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(totalMilliSeconds)), // The change is
																										// in this line
				TimeUnit.MILLISECONDS.toSeconds(totalMilliSeconds)
						- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(totalMilliSeconds)));

	}

	/**
	 * Returns the date and time for the start of the current year (January 1st).
	 *
	 * @return the date and time for the start of the current year
	 */
	public static Date getCurrentYearDateTimeFromStarting() {
		Date currentDate = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(currentDate);
		calendar.set(Calendar.MONTH, Calendar.JANUARY);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		Date startOfYearDate = calendar.getTime();
		return atStartOfDay(startOfYearDate);
	}

	public static Date getPreviousDayDateFromProvidedDate(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_YEAR, -1);
		Date previousDate = calendar.getTime();
		return previousDate;
	}

	public static Date getNextDayDateFromProvidedDate(Date date) {
		// create a Calendar object and set it to today's date
		Calendar calendar = Calendar.getInstance();
		try {
			calendar.setTime(date);

			// subtract one day from the calendar
			calendar.add(Calendar.DAY_OF_MONTH, +1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// get the updated date from the calendar
		return calendar.getTime();
	}

	public static Date getDateFomString(String date) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

		// Parse the string into LocalDateTime
		LocalDateTime localDateTime = LocalDateTime.parse(date, formatter);

		// Convert LocalDateTime to java.util.Date
		return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
	}

	public static String extractDateOnly(String date) {
		if (date != null) {
			try {
				return date.split("[ T]")[0];
			} catch (DateTimeParseException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * Converts a Date object to a formatted String. If the date is null or
	 * formatting fails, it returns an empty string.
	 *
	 * @param date The Date object to format.
	 * @return A formatted date string in "yyyy-MM-dd" format, or an empty string if
	 *         an error occurs.
	 */
	public static String convertDateToString(Date date) {
		try {
			if (date == null) {
				return "";
			}
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			return formatter.format(date);
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	public static boolean isDateInExpectedFormat(String date, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		sdf.setLenient(false);
		try {
			sdf.parse(date);
			return true;
		} catch (ParseException e) {
			return false;
		}
	}

	public static String dateToStringddmmyyyyWithHHmm(Date date) {
		if (UtilRandomKey.isNotNull(date)) {
			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ENGLISH);
			try {
				return formatter.format(date);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}