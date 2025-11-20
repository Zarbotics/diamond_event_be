package com.zbs.de.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.zbs.de.model.dto.DtoDailyCreatedCount;
import com.zbs.de.model.dto.DtoDailyDateEventSale;
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
	private final ZoneId tz = ZoneId.of("Asia/Karachi"); // use Pakistan timezone per your context

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
}
