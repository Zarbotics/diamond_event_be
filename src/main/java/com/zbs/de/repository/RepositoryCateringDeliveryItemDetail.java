package com.zbs.de.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.zbs.de.model.CateringDeliveryItemDetail;

@Repository("repositoryCateringDeliveryItemDetail")
public interface RepositoryCateringDeliveryItemDetail extends JpaRepository<CateringDeliveryItemDetail, Integer> {
	List<CateringDeliveryItemDetail> findByCateringDeliveryBookingSerDeliveryBookingIdAndBlnIsDeletedFalse(
			Integer bookingId);
}
