package com.zbs.de.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.CateringDeliveryBooking;

@Repository("repositoryCateringDeliveryBooking")
public interface RepositoryCateringDeliveryBooking extends JpaRepository<CateringDeliveryBooking, Integer> {
	List<CateringDeliveryBooking> findByBlnIsDeletedFalse();

	Optional<CateringDeliveryBooking> findBySerDeliveryBookingIdAndBlnIsDeletedFalse(Integer id);

	boolean existsByTxtDeliveryBookingCodeAndBlnIsDeletedFalse(String code);

	@Query("SELECT MAX(e.txtDeliveryBookingCode) FROM CateringDeliveryBooking e")
	String findMaxCateringDeliveryBookingCode();

	@Query("SELECT e FROM CateringDeliveryBooking e WHERE e.customerMaster.serCustId = :custId AND e.blnIsDeleted = false")
	List<CateringDeliveryBooking> findByCustomerId(@Param("custId") Integer custId);

	@Query(value = """
			SELECT *
			FROM catering_delivery_booking
			WHERE COALESCE(bln_is_deleted, false) = false
			ORDER BY
			    (dte_delivery_date < CURRENT_DATE) ASC,
			    ABS(EXTRACT(EPOCH FROM (dte_delivery_date - CURRENT_DATE)))
			""", nativeQuery = true)
	List<CateringDeliveryBooking> findAllSortedByClosestDeliveryDate();
	
	@EntityGraph(attributePaths = { "customerMaster", "eventType", "eventBudget" })
	Page<CateringDeliveryBooking> findAll(Specification<CateringDeliveryBooking> spec, Pageable pageable);
	
	boolean existsByDteDeliveryDateAndBlnIsDeletedFalse(Date dteDeliveryDate);

	@Query("select distinct(e.dteDeliveryDate) from CateringDeliveryBooking e where e.blnIsDeleted = false order by e.dteDeliveryDate asc")
	List<Date> getAlreadyBookedDates();
}
