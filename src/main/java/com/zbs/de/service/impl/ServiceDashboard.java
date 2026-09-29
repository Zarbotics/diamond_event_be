package com.zbs.de.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.zbs.de.model.dto.DtoDailyCreatedCount;
import com.zbs.de.model.dto.DtoDailyDateEventSale;
import com.zbs.de.model.dto.DtoBusinessSummary;
import com.zbs.de.model.dto.DtoDashboard;
import com.zbs.de.model.dto.DtoPercentageChange;
import com.zbs.de.model.dto.DtoTypeSales;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.*;
import java.util.*;

@Service
public class ServiceDashboard {

	private final JdbcTemplate jdbc;
	/*
	 * Europe/London. This read Asia/Karachi, which is five hours ahead — so
	 * "this month" and "today" rolled over at 7pm or 8pm UK time depending on
	 * the season, and every count near a month boundary was taken against the
	 * wrong day. The business, its venues and its customers are in the UK.
	 */
	private final ZoneId tz = ZoneId.of("Europe/London");

	public ServiceDashboard(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	public DtoDashboard getAnalyticsSummary() {
		LocalDate today = LocalDate.now(tz);
		LocalDate firstOfThisMonth = today.withDayOfMonth(1);
		LocalDate firstOfNextMonth = firstOfThisMonth.plusMonths(1);
		LocalDate firstOfLastMonth = firstOfThisMonth.minusMonths(1);

		Timestamp startThisMonthTs = Timestamp.valueOf(firstOfThisMonth.atStartOfDay());
		Timestamp startNextMonthTs = Timestamp.valueOf(firstOfNextMonth.atStartOfDay());
		Timestamp startLastMonthTs = Timestamp.valueOf(firstOfLastMonth.atStartOfDay());

		// 1: Total customers
		Long totalCustomers = jdbc.queryForObject(
				"SELECT COALESCE(COUNT(*),0) FROM customer_master WHERE COALESCE(bln_is_deleted,false)=false",
				Long.class);

		// 2: Customers this month (created_date)
		Long customersThisMonth = jdbc.queryForObject(
				"SELECT COALESCE(COUNT(*),0) FROM customer_master WHERE COALESCE(bln_is_deleted,false)=false AND created_date >= ? AND created_date < ?",
				Long.class, startThisMonthTs, startNextMonthTs);

		// customers last month
		Long customersLastMonth = jdbc.queryForObject(
				"SELECT COALESCE(COUNT(*),0) FROM customer_master WHERE COALESCE(bln_is_deleted,false)=false AND created_date >= ? AND created_date < ?",
				Long.class, startLastMonthTs, startThisMonthTs);

		BigDecimal customersPct = percentChange(customersLastMonth, customersThisMonth);
		DtoPercentageChange customersChange = new DtoPercentageChange(customersLastMonth, customersThisMonth,
				customersPct);

		// 4: total events
		Long totalEvents = jdbc.queryForObject(
				"SELECT COALESCE(COUNT(*),0) FROM event_master WHERE COALESCE(bln_is_deleted,false)=false", Long.class);

		// 5: events this month (created_date)
		Long eventsThisMonth = jdbc.queryForObject(
				"SELECT COALESCE(COUNT(*),0) FROM event_master WHERE COALESCE(bln_is_deleted,false)=false AND created_date >= ? AND created_date < ?",
				Long.class, startThisMonthTs, startNextMonthTs);

		// events last month
		Long eventsLastMonth = jdbc.queryForObject(
				"SELECT COALESCE(COUNT(*),0) FROM event_master WHERE COALESCE(bln_is_deleted,false)=false AND created_date >= ? AND created_date < ?",
				Long.class, startLastMonthTs, startThisMonthTs);

		BigDecimal eventsPct = percentChange(eventsLastMonth, eventsThisMonth);
		DtoPercentageChange eventsChange = new DtoPercentageChange(eventsLastMonth, eventsThisMonth, eventsPct);

		// 7 & 10: date wise customers & events for current month (created_date)
		String sqlCustomersByDay = "SELECT to_char(created_date::date,'YYYY-MM-DD') as dt, COUNT(*) as cnt "
				+ "FROM customer_master "
				+ "WHERE COALESCE(bln_is_deleted,false)=false AND created_date >= ? AND created_date < ? "
				+ "GROUP BY dt ORDER BY dt";

		List<Map<String, Object>> custRows = jdbc.queryForList(sqlCustomersByDay, startThisMonthTs, startNextMonthTs);
		Map<String, Integer> custByDate = new HashMap<>();
		for (Map<String, Object> r : custRows) {
			String dt = (String) r.get("dt");
			int cnt = ((Number) r.get("cnt")).intValue();
			custByDate.put(dt, cnt);
		}

		String sqlEventsByDay = "SELECT to_char(created_date::date,'YYYY-MM-DD') as dt, COUNT(*) as cnt "
				+ "FROM event_master "
				+ "WHERE COALESCE(bln_is_deleted,false)=false AND created_date >= ? AND created_date < ? "
				+ "GROUP BY dt ORDER BY dt";

		List<Map<String, Object>> evRows = jdbc.queryForList(sqlEventsByDay, startThisMonthTs, startNextMonthTs);
		Map<String, Integer> eventsByDate = new HashMap<>();
		for (Map<String, Object> r : evRows) {
			String dt = (String) r.get("dt");
			int cnt = ((Number) r.get("cnt")).intValue();
			eventsByDate.put(dt, cnt);
		}

		List<DtoDailyCreatedCount> createdCounts = new ArrayList<>();
		LocalDate d = firstOfThisMonth;
		while (!d.isEqual(firstOfNextMonth)) {
			String key = d.toString();
			int c = custByDate.getOrDefault(key, 0);
			int e = eventsByDate.getOrDefault(key, 0);
			createdCounts.add(new DtoDailyCreatedCount(key, c, e));
			d = d.plusDays(1);
		}

		// 8: total sale (num_paid_amount). Sum only for not-deleted events and
		// not-deleted budgets
		BigDecimal totalSale = jdbc.queryForObject(
				"SELECT COALESCE(SUM(eb.num_paid_amount),0) FROM event_budget eb "
						+ "JOIN event_master em ON eb.ser_event_master_id = em.ser_event_master_id "
						+ "WHERE COALESCE(eb.bln_is_deleted,false)=false AND COALESCE(em.bln_is_deleted,false)=false",
				BigDecimal.class);

		// 9: events and sale per event type
		String sqlTypeStats = "SELECT et.ser_event_type_id, et.txt_event_type_name, COUNT(em.ser_event_master_id) as total_events, COALESCE(SUM(eb.num_paid_amount),0) as total_sale "
				+ "FROM event_type et "
				+ "LEFT JOIN event_master em ON em.ser_event_type_id = et.ser_event_type_id AND COALESCE(em.bln_is_deleted,false)=false "
				+ "LEFT JOIN event_budget eb ON eb.ser_event_master_id = em.ser_event_master_id AND COALESCE(eb.bln_is_deleted,false)=false "
				+ "WHERE COALESCE(et.bln_is_deleted, false) = false GROUP BY et.ser_event_type_id, et.txt_event_type_name ORDER BY total_events DESC";

		List<Map<String, Object>> typeRows = jdbc.queryForList(sqlTypeStats);
		List<DtoTypeSales> typeSales = new ArrayList<>();
		for (Map<String, Object> r : typeRows) {
			Integer id = (r.get("ser_event_type_id") == null) ? null : ((Number) r.get("ser_event_type_id")).intValue();
			String name = (String) r.get("txt_event_type_name");
			long totalEv = ((Number) r.get("total_events")).longValue();
			BigDecimal sale = (r.get("total_sale") == null) ? BigDecimal.ZERO : (BigDecimal) r.get("total_sale");
			typeSales.add(new DtoTypeSales(id, name, totalEv, sale));
		}

		// 11: events by dte_event_date (event date) for current month
		// Note: We use the date range based on dte_event_date between firstOfThisMonth
		// and firstOfNextMonth
		String sqlEventsByDte = "SELECT to_char(dte_event_date::date,'YYYY-MM-DD') as dt, COUNT(em.ser_event_master_id) as events, COALESCE(SUM(eb.num_paid_amount),0) as sale "
				+ "FROM event_master em "
				+ "LEFT JOIN event_budget eb ON eb.ser_event_master_id = em.ser_event_master_id AND COALESCE(eb.bln_is_deleted,false)=false "
				+ "WHERE COALESCE(em.bln_is_deleted,false)=false AND em.dte_event_date >= ? AND em.dte_event_date < ? "
				+ "GROUP BY dt ORDER BY dt";

		Timestamp startDteThisMonth = Timestamp.valueOf(firstOfThisMonth.atStartOfDay());
		Timestamp startDteNextMonth = Timestamp.valueOf(firstOfNextMonth.atStartOfDay());

		List<Map<String, Object>> byDteRows = jdbc.queryForList(sqlEventsByDte, startDteThisMonth, startDteNextMonth);
		Map<String, DtoDailyDateEventSale> mapByDte = new HashMap<>();
		for (Map<String, Object> r : byDteRows) {
			String dt = (String) r.get("dt");
			long evCount = ((Number) r.get("events")).longValue();
			BigDecimal sale = (r.get("sale") == null) ? BigDecimal.ZERO : (BigDecimal) r.get("sale");
			mapByDte.put(dt, new DtoDailyDateEventSale(dt, evCount, sale));
		}

		List<DtoDailyDateEventSale> eventsByDte = new ArrayList<>();
		d = firstOfThisMonth;
		while (!d.isEqual(firstOfNextMonth)) {
			String k = d.toString();
			DtoDailyDateEventSale rec = mapByDte.getOrDefault(k, new DtoDailyDateEventSale(k, 0L, BigDecimal.ZERO));
			eventsByDte.add(rec);
			d = d.plusDays(1);
		}

		DtoDashboard dto = new DtoDashboard();
		dto.setTotalCustomers(totalCustomers != null ? totalCustomers : 0L);
		dto.setCustomersThisMonth(customersThisMonth != null ? customersThisMonth : 0L);
		dto.setCustomersMonthlyRate(customersChange);

		dto.setTotalEvents(totalEvents != null ? totalEvents : 0L);
		dto.setEventsThisMonth(eventsThisMonth != null ? eventsThisMonth : 0L);
		dto.setEventsMonthlyRate(eventsChange);

		dto.setCreatedCountsThisMonth(createdCounts);
		dto.setTotalSales(totalSale == null ? BigDecimal.ZERO : totalSale);
		dto.setEventTypeStats(typeSales);
		dto.setEventsByEventDateThisMonth(eventsByDte);

		return dto;
	}

	/**
	 * Compute percent change = ((cur - last) / last) * 100 If last == 0: - if cur
	 * == 0 -> 0% - if cur > 0 -> null (undefined/infinite increase)
	 */
	private BigDecimal percentChange(Long last, Long cur) {
		long l = (last == null) ? 0L : last;
		long c = (cur == null) ? 0L : cur;
		if (l == 0L) {
			if (c == 0L)
				return BigDecimal.ZERO;
			return null; // undefined (infinite increase)
		}
		double pct = ((double) (c - l) / (double) l) * 100.0;
		return BigDecimal.valueOf(pct).setScale(2, RoundingMode.HALF_UP);
	}

	/**
	 * The pipeline, the year ahead, and what is waiting on somebody.
	 *
	 * <p>
	 * See {@link DtoBusinessSummary} for what each figure is and why it is that
	 * one rather than the ones the old dashboard showed.
	 */
	public DtoBusinessSummary getBusinessSummary() {
		DtoBusinessSummary summary = new DtoBusinessSummary();

		LocalDate today = LocalDate.now(tz);
		LocalDate firstOfThisMonth = today.withDayOfMonth(1);
		Timestamp fromTs = Timestamp.valueOf(firstOfThisMonth.atStartOfDay());
		Timestamp toTs = Timestamp.valueOf(firstOfThisMonth.plusMonths(MONTHS_AHEAD).atStartOfDay());

		/*
		 * What a booking is worth: its priced lines plus their VAT. Written by
		 * the pricing engine at save, so it is the same arithmetic the customer
		 * was quoted — see DtoBusinessSummary for why not the budget column.
		 */
		final String VALUE_CTE = "WITH value AS ("
				+ "  SELECT ser_event_master_id,"
				+ "         SUM(COALESCE(num_line_total,0) + COALESCE(num_vat,0)) AS total"
				+ "  FROM event_price_line"
				+ "  GROUP BY ser_event_master_id"
				+ ") ";

		// --- The pipeline -------------------------------------------------
		List<DtoBusinessSummary.Stage> pipeline = jdbc.query(
				VALUE_CTE
						+ "SELECT COALESCE(NULLIF(TRIM(b.txt_status),''), 'Enquiry') AS stage,"
						+ "       COUNT(*) AS bookings,"
						+ "       COALESCE(SUM(v.total),0) AS value "
						+ "FROM event_master e "
						+ "LEFT JOIN event_budget b ON b.ser_event_master_id = e.ser_event_master_id "
						+ "LEFT JOIN value v ON v.ser_event_master_id = e.ser_event_master_id "
						+ "WHERE COALESCE(e.bln_is_deleted,false) = false "
						+ "GROUP BY 1",
				(rs, i) -> {
					DtoBusinessSummary.Stage stage = new DtoBusinessSummary.Stage();
					stage.setTxtStage(rs.getString("stage"));
					stage.setNumBookings(rs.getLong("bookings"));
					stage.setNumValue(nonNull(rs.getBigDecimal("value")));
					return stage;
				});

		/* In the order the business works through them, not alphabetically. */
		pipeline.sort(Comparator.comparingInt(s -> stageOrder(s.getTxtStage())));
		summary.setPipeline(pipeline);

		long taken = 0;
		long confirmed = 0;
		for (DtoBusinessSummary.Stage stage : pipeline) {
			taken += stage.getNumBookings();
			if (isConfirmed(stage.getTxtStage())) {
				confirmed += stage.getNumBookings();
				summary.setNumConfirmedValue(summary.getNumConfirmedValue().add(stage.getNumValue()));
			} else {
				summary.setNumPipelineValue(summary.getNumPipelineValue().add(stage.getNumValue()));
			}
		}

		if (taken > 0) {
			summary.setNumConversionRate(BigDecimal.valueOf(confirmed)
					.multiply(BigDecimal.valueOf(100))
					.divide(BigDecimal.valueOf(taken), 1, RoundingMode.HALF_UP));
		}

		if (confirmed > 0) {
			summary.setNumAverageBookingValue(summary.getNumConfirmedValue()
					.divide(BigDecimal.valueOf(confirmed), 2, RoundingMode.HALF_UP));
		}

		// --- The months ahead ---------------------------------------------
		Map<String, DtoBusinessSummary.Month> months = new LinkedHashMap<>();
		for (int i = 0; i < MONTHS_AHEAD; i += 1) {
			DtoBusinessSummary.Month month = new DtoBusinessSummary.Month();
			month.setTxtMonth(firstOfThisMonth.plusMonths(i).toString().substring(0, 7));
			months.put(month.getTxtMonth(), month);
		}

		jdbc.query(
				VALUE_CTE
						+ "SELECT to_char(e.dte_event_date, 'YYYY-MM') AS ym,"
						+ "       COUNT(*) AS bookings,"
						+ "       COALESCE(SUM(e.num_number_of_guests),0) AS guests,"
						+ "       COALESCE(SUM(v.total),0) AS value "
						+ "FROM event_master e "
						+ "LEFT JOIN value v ON v.ser_event_master_id = e.ser_event_master_id "
						+ "WHERE COALESCE(e.bln_is_deleted,false) = false "
						+ "  AND e.dte_event_date >= ? AND e.dte_event_date < ? "
						+ "GROUP BY 1",
				rs -> {
					/*
					 * Into the months already laid out, so a month with nothing
					 * in it is a gap in the line rather than a missing point —
					 * a chart that skips empty months reads as busier than the
					 * year actually is.
					 */
					DtoBusinessSummary.Month month = months.get(rs.getString("ym"));
					if (month != null) {
						month.setNumBookings(rs.getLong("bookings"));
						month.setNumGuests(rs.getLong("guests"));
						month.setNumValue(nonNull(rs.getBigDecimal("value")));
					}
				},
				fromTs, toTs);

		summary.setMonthsAhead(new ArrayList<>(months.values()));
		summary.setNumBookingsAhead(months.values().stream().mapToLong(DtoBusinessSummary.Month::getNumBookings).sum());
		summary.setNumGuestsAhead(months.values().stream().mapToLong(DtoBusinessSummary.Month::getNumGuests).sum());

		// --- The mix ------------------------------------------------------
		summary.setByType(jdbc.query(
				"SELECT COALESCE(NULLIF(TRIM(t.txt_event_type_name),''), 'Not set') AS type,"
						+ "       COUNT(*) AS bookings,"
						+ "       COALESCE(SUM(e.num_number_of_guests),0) AS guests "
						+ "FROM event_master e "
						+ "LEFT JOIN event_type t ON t.ser_event_type_id = e.ser_event_type_id "
						+ "WHERE COALESCE(e.bln_is_deleted,false) = false "
						+ "  AND e.dte_event_date >= ? AND e.dte_event_date < ? "
						+ "GROUP BY 1 ORDER BY 2 DESC",
				(rs, i) -> {
					DtoBusinessSummary.TypeShare share = new DtoBusinessSummary.TypeShare();
					share.setTxtEventType(rs.getString("type"));
					share.setNumBookings(rs.getLong("bookings"));
					share.setNumGuests(rs.getLong("guests"));
					return share;
				},
				fromTs, toTs));

		// --- Waiting on somebody ------------------------------------------
		summary.setNumWithoutDate(count(
				"SELECT COUNT(*) FROM event_master e "
						+ "WHERE COALESCE(e.bln_is_deleted,false) = false AND e.dte_event_date IS NULL"));

		summary.setNumWithoutPrice(count(
				"SELECT COUNT(*) FROM event_master e "
						+ "WHERE COALESCE(e.bln_is_deleted,false) = false "
						+ "  AND e.dte_event_date >= CURRENT_DATE "
						+ "  AND NOT EXISTS (SELECT 1 FROM event_price_line l "
						+ "                  WHERE l.ser_event_master_id = e.ser_event_master_id)"));

		return summary;
	}

	/** How many months of the year ahead the dashboard shows. */
	private static final int MONTHS_AHEAD = 12;

	private long count(String sql) {
		Long value = jdbc.queryForObject(sql, Long.class);
		return value == null ? 0L : value;
	}

	private static BigDecimal nonNull(BigDecimal value) {
		return value == null ? BigDecimal.ZERO : value;
	}

	private static boolean isConfirmed(String stage) {
		return stage != null && stage.trim().equalsIgnoreCase("Confirmed");
	}

	/**
	 * Enquiry, then Quoted, then Confirmed — the order the business works
	 * through them. Anything it does not recognise goes last rather than
	 * being dropped, because a stage nobody expected is worth seeing.
	 */
	private static int stageOrder(String stage) {
		if (stage == null) return 99;
		switch (stage.trim().toLowerCase()) {
			case "enquiry": return 0;
			case "quoted": return 1;
			case "confirmed": return 2;
			default: return 98;
		}
	}
}
