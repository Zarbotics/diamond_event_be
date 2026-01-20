package com.zbs.de.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.zbs.de.model.EventBudget;
import com.zbs.de.model.dto.DtoEventAnalytics;

public interface RepositoryEventBudget extends JpaRepository<EventBudget, Integer> {

	List<EventBudget> findByBlnIsDeleted(Boolean blnIsDeleted);

	Optional<EventBudget> findByEventMaster_SerEventMasterId(Integer serEventMasterId);
	
	Optional<EventBudget> findByCateringDeliveryBooking_SerDeliveryBookingId(Integer serDeliveryBookingId);

	@Query(value = """
			SELECT
			    DATE_FORMAT(e.dte_deal_date, '%Y-%m') AS month,
			    SUM(e.num_total_budget) AS totalSales
			FROM event_budget e
			GROUP BY month
			ORDER BY month
			""", nativeQuery = true)
	List<DtoEventAnalytics> getMonthlySales();

	@Query(value = "SELECT TO_CHAR(eb.dte_deal_date, 'YYYY-MM') AS month, " + "et.txt_event_type_name AS eventType, "
			+ "SUM(eb.num_total_profit) AS totalProfit " + "FROM event_budget eb "
			+ "JOIN event_master em ON em.ser_event_master_id = eb.ser_event_master_id "
			+ "JOIN event_type et ON et.ser_event_type_id = em.ser_event_type_id " + "WHERE eb.bln_is_deleted = false "
			+ "GROUP BY TO_CHAR(eb.dte_deal_date, 'YYYY-MM'), et.txt_event_type_name "
			+ "ORDER BY TO_CHAR(eb.dte_deal_date, 'YYYY-MM')", nativeQuery = true)
	List<Object[]> getRawProfitByEventTypeAndMonth();

	@Query("SELECT new com.zbs.de.model.dto.DtoEventAnalytics("
			+ "SUM(eb.numTotalBudget), SUM(eb.numTotalProfit), SUM(eb.numTotalExpense)) " + "FROM EventBudget eb "
			+ "WHERE eb.blnIsDeleted = false")
	DtoEventAnalytics getSalesSummary();
}
