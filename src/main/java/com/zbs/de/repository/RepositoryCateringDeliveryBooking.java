package com.zbs.de.repository;

import java.util.List;
import java.util.Optional;

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
}