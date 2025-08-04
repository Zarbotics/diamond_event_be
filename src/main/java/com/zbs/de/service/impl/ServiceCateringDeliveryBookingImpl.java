package com.zbs.de.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zbs.de.mapper.MapperCateringDeliveryBooking;
import com.zbs.de.model.CateringDeliveryBooking;
import com.zbs.de.model.CateringDeliveryItemDetail;
import com.zbs.de.model.MenuFoodMaster;
import com.zbs.de.model.dto.DtoCateringDeliveryBooking;
import com.zbs.de.model.dto.DtoCateringDeliveryItemDetail;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.model.dto.DtoSearch;
import com.zbs.de.repository.RepositoryCateringDeliveryBooking;
import com.zbs.de.repository.RepositoryCateringDeliveryItemDetail;
import com.zbs.de.repository.RepositoryCustomerMaster;
import com.zbs.de.repository.RepositoryEventType;
import com.zbs.de.repository.RepositoryMenuFoodMaster;
import com.zbs.de.service.ServiceCateringDeliveryBooking;

import jakarta.persistence.EntityNotFoundException;

@Service("serviceCateringDeliveryBookingImpl")
public class ServiceCateringDeliveryBookingImpl implements ServiceCateringDeliveryBooking {

	@Autowired
	private RepositoryCateringDeliveryBooking repositoryCateringDeliveryBooking;

	@Autowired
	private RepositoryCateringDeliveryItemDetail repositoryCateringDeliveryItemDetail;

	@Autowired
	private RepositoryCustomerMaster repositoryCustomerMaster;

	@Autowired
	private RepositoryEventType repositoryEventType;

	@Autowired
	private RepositoryMenuFoodMaster repositoryMenuFoodMaster;

	@Override
	public DtoResult saveOrUpdate(DtoCateringDeliveryBooking dto) {
		DtoResult result = new DtoResult();

		try {
			// Validate required fields
			if (dto.getSerCustId() == null) {
				result.setTxtMessage("Customer is required.");
				return result;
			}
			if (dto.getSerEventTypeId() == null) {
				result.setTxtMessage("Event Type is required.");
				return result;
			}

			// Fetch Customer
			var customer = repositoryCustomerMaster.findById(dto.getSerCustId()).orElse(null);
			if (customer == null) {
				result.setTxtMessage("Customer not found with ID: " + dto.getSerCustId());
				return result;
			}

			// Fetch Event Type
			var eventType = repositoryEventType.findById(dto.getSerEventTypeId()).orElse(null);
			if (eventType == null) {
				result.setTxtMessage("Event Type not found with ID: " + dto.getSerEventTypeId());
				return result;
			}

			CateringDeliveryBooking entity;

			if (dto.getSerDeliveryBookingId() != null) {
				entity = repositoryCateringDeliveryBooking.findById(dto.getSerDeliveryBookingId()).orElseThrow(
						() -> new RuntimeException("Booking not found with ID: " + dto.getSerDeliveryBookingId()));
			} else {
				entity = new CateringDeliveryBooking();
				entity.setTxtDeliveryBookingCode(generateAutoCode());
			}

			// Set master fields
			entity.setCustomerMaster(customer);
			entity.setEventType(eventType);

			// Map other fields using Mapper
			CateringDeliveryBooking mapped = MapperCateringDeliveryBooking.toEntity(dto);
			entity.setTxtBookingStatus(mapped.getTxtBookingStatus());
			entity.setBlnBookingStatus(mapped.getBlnBookingStatus());
			entity.setBlnRequestMeeting(mapped.getBlnRequestMeeting());
			entity.setDteDeliveryDate(mapped.getDteDeliveryDate());
			entity.setTxtDeliveryLocation(mapped.getTxtDeliveryLocation());
			entity.setTxtDeliveryTime(mapped.getTxtDeliveryTime());
			entity.setTxtRemarks(mapped.getTxtRemarks());
			entity.setTxtSpecialInstructions(mapped.getTxtSpecialInstructions());

			// Map item details
			List<CateringDeliveryItemDetail> detailList = new ArrayList<>();
			if (dto.getCateringDeliveryItemDetails() != null) {
				for (DtoCateringDeliveryItemDetail detailDto : dto.getCateringDeliveryItemDetails()) {
					if (detailDto.getSerMenuFoodId() == null) {
						throw new IllegalArgumentException("Menu Food ID is required for each delivery item.");
					}

					MenuFoodMaster food = repositoryMenuFoodMaster.findById(detailDto.getSerMenuFoodId())
							.orElseThrow(() -> new EntityNotFoundException(
									"Menu Food not found with ID: " + detailDto.getSerMenuFoodId()));

					CateringDeliveryItemDetail detail = new CateringDeliveryItemDetail();
					detail.setCateringDeliveryBooking(entity);
					detail.setMenueFoodMaster(food);
					detailList.add(detail);
				}
			}

			entity.setCateringDeliveryItemDetails(detailList);

			repositoryCateringDeliveryBooking.save(entity);

			result.setTxtMessage("Saved successfully");
			result.setResult(MapperCateringDeliveryBooking.toDto(entity));

		} catch (RuntimeException e) {
			result.setTxtMessage("Error: " + e.getMessage());
		} catch (Exception e) {
			result.setTxtMessage("Unexpected error while saving: " + e.getMessage());
		}

		return result;
	}

	@Override
	public DtoResult softDelete(Integer id) {
		DtoResult result = new DtoResult();
		try {
			CateringDeliveryBooking entity = repositoryCateringDeliveryBooking.findById(id)
					.orElseThrow(() -> new RuntimeException("Booking not found"));
			entity.setBlnIsDeleted(true);
			repositoryCateringDeliveryBooking.save(entity);
			result.setTxtMessage("Deleted successfully");
		} catch (Exception e) {
			result.setTxtMessage("Error while deleting: " + e.getMessage());
		}
		return result;
	}

	@Override
	public DtoResult getByPK(Integer id) {
		DtoResult result = new DtoResult();
		repositoryCateringDeliveryBooking.findBySerDeliveryBookingIdAndBlnIsDeletedFalse(id).ifPresentOrElse(
				booking -> result.setResult(MapperCateringDeliveryBooking.toDto(booking)),
				() -> result.setTxtMessage("Not found"));
		return result;
	}

	@Override
	public DtoResult getAll() {
		DtoResult result = new DtoResult();
		List<CateringDeliveryBooking> bookings = repositoryCateringDeliveryBooking.findByBlnIsDeletedFalse();
		List<DtoCateringDeliveryBooking> list = bookings.stream().map(MapperCateringDeliveryBooking::toDto)
				.collect(Collectors.toList());
		result.setResulList(new ArrayList<>(list));
		result.setTxtMessage("Fetched successfully");
		return result;
	}

	@Override
	public DtoResult getByCustId(DtoSearch dtoSearch) {
		DtoResult result = new DtoResult();
		List<CateringDeliveryBooking> bookings = repositoryCateringDeliveryBooking.findByCustomerId(dtoSearch.getId());
		List<DtoCateringDeliveryBooking> list = bookings.stream().map(MapperCateringDeliveryBooking::toDto)
				.collect(Collectors.toList());
		result.setResulList(new ArrayList<>(list));
		result.setTxtMessage("Fetched successfully");
		return result;
	}

	@Override
	public DtoResult search(String keyword) {
		DtoResult result = new DtoResult();
		List<CateringDeliveryBooking> all = repositoryCateringDeliveryBooking.findByBlnIsDeletedFalse();
		List<DtoCateringDeliveryBooking> filtered = all.stream()
				.filter(e -> e.getTxtDeliveryLocation() != null
						&& e.getTxtDeliveryLocation().toLowerCase().contains(keyword.toLowerCase()))
				.map(MapperCateringDeliveryBooking::toDto).collect(Collectors.toList());
		result.setResulList(new ArrayList<>(filtered));
		result.setTxtMessage("Filtered");
		return result;
	}

	public String generateAutoCode() {
		String maxCode = repositoryCateringDeliveryBooking.findMaxCateringDeliveryBookingCode();

		int nextNumber = 1;

		if (maxCode != null && maxCode.startsWith("CDB-")) {
			try {
				String numberPart = maxCode.substring(4);
				nextNumber = Integer.parseInt(numberPart) + 1;
			} catch (NumberFormatException e) {
				nextNumber = 1;
			}
		}

		return String.format("CDB-%03d", nextNumber);
	}

}
