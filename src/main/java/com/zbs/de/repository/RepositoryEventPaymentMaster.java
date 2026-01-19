package com.zbs.de.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.EventPayment;

@Repository("repositoryEventPaymentMaster")
public interface RepositoryEventPaymentMaster extends JpaRepository<EventPayment, Integer> {

    List<EventPayment> findByEventBudget_SerEventBudgetIdAndBlnIsDeleted(Integer serEventBudgetId, Boolean blnIsDeleted);

    @Query(value = "SELECT COALESCE(SUM(num_amount),0) FROM event_payment WHERE ser_event_budget_id = :budgetId AND bln_is_deleted = false", nativeQuery = true)
    BigDecimal sumPaidByBudgetId(Integer budgetId);

    @Query(value = "SELECT to_char(dte_payment_date, 'YYYY-MM') AS month, SUM(num_amount) AS totalReceived FROM event_payment WHERE bln_is_deleted = false GROUP BY to_char(dte_payment_date, 'YYYY-MM') ORDER BY 1", nativeQuery = true)
    List<Object[]> getMonthlyReceivedRaw();
    
    List<EventPayment> findBySerEventMasterIdAndBlnIsDeletedFalseOrderByDtePaymentDateDesc(
            Integer serEventMasterId
    );
}
