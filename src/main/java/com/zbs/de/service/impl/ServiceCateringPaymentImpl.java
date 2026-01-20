package com.zbs.de.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.zbs.de.controller.ControllerEventType;
import com.zbs.de.mapper.MapperEventPayment;
import com.zbs.de.model.EventBudget;
import com.zbs.de.model.EventPayment;
import com.zbs.de.model.EventPaymentDocument;
import com.zbs.de.model.dto.DtoPayment;
import com.zbs.de.model.dto.DtoPaymentDocument;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.repository.RepositoryEventBudget;
import com.zbs.de.repository.RepositoryEventPaymentDocument;
import com.zbs.de.repository.RepositoryEventPaymentMaster;
import com.zbs.de.service.ServiceCateringPayment;
import com.zbs.de.service.ServiceEventBudget;
import com.zbs.de.util.UtilDateAndTime;
import com.zbs.de.util.UtilFileStorage;

@Service("serviceCateringPaymentImpl")
public class ServiceCateringPaymentImpl implements ServiceCateringPayment {
	@Autowired
	private RepositoryEventPaymentMaster repositoryEventPayment;

	@Autowired
	private RepositoryEventPaymentDocument repositoryEventPaymentDocument;

	@Autowired
	private RepositoryEventBudget repositoryEventBudget;

	@Autowired
	private ServiceEventBudget serviceEventBudget;

	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerEventType.class);

	@Override
	@Transactional
	public DtoResult savePayment(DtoPayment dtoPayment) {

		DtoResult result = new DtoResult();

		if (dtoPayment.getSerDeliveryBookingId() == null) {
			result.setTxtMessage("Catering ID is required.");
			return result;
		}

//		EventBudget budget = repositoryEventBudget.findById(dtoPayment.getSerEventBudgetId())
//				.orElseThrow(() -> new RuntimeException("Invalid Budget ID"));

		EventBudget budget = repositoryEventBudget.findByCateringDeliveryBooking_SerDeliveryBookingId(dtoPayment.getSerDeliveryBookingId())
				.orElseThrow(() -> new RuntimeException("Invalid Catering ID"));

		EventPayment payment;

		if (dtoPayment.getSerEventPaymentId() != null) {
			payment = repositoryEventPayment.findById(dtoPayment.getSerEventPaymentId())
					.orElseThrow(() -> new RuntimeException("Payment not found"));
			payment.setUpdatedBy(ServiceCurrentUser.getCurrentUserId());
		} else {
			payment = new EventPayment();
			payment.setCreatedBy(ServiceCurrentUser.getCurrentUserId());
		}

		/*
		 * =============================== MAP PAYMENT FIELDS
		 * ===============================
		 */
		payment.setEventBudget(budget);
		payment.setSerDeliveryBookingId(
				dtoPayment.getSerDeliveryBookingId() != null ? dtoPayment.getSerDeliveryBookingId()
						: budget.getCateringDeliveryBooking().getSerDeliveryBookingId());
		payment.setNumAmount(dtoPayment.getNumAmount());
		payment.setTxtPaymentMode(dtoPayment.getTxtPaymentMode());
		payment.setTxtTransactionRef(dtoPayment.getTxtTransactionRef());
		payment.setDtePaymentDate(UtilDateAndTime.ddmmyyyyStringToDate(dtoPayment.getDtePaymentDate()));
		payment.setTxtPaymentStatus(dtoPayment.getTxtPaymentStatus());
		payment.setTxtRemarks(dtoPayment.getTxtRemarks());

		repositoryEventPayment.save(payment);

		/*
		 * =============================== CENTRALIZED BUDGET RECALC
		 * ===============================
		 */
		serviceEventBudget.recalculateBudget(budget.getSerEventBudgetId());

		result.setTxtMessage("Success");
		result.setResult(MapperEventPayment.toDto(payment));
		return result;
	}

	@Override
	@Transactional
	public DtoResult savePaymentWithFiles(DtoPayment dtoPayment, List<MultipartFile> files) {

		DtoResult baseResult = new DtoResult();
		try {

			baseResult = savePayment(dtoPayment);
			if (!"Success".equalsIgnoreCase(baseResult.getTxtMessage())) {
				return baseResult;
			}

			DtoPayment savedDto = (DtoPayment) baseResult.getResult();
			EventPayment payment = repositoryEventPayment.findById(savedDto.getSerEventPaymentId())
					.orElseThrow(() -> new RuntimeException("Payment not found after save"));

			/*
			 * ========================= FILE MAP (LIKE YOUR METHOD)
			 * =========================
			 */
			Map<String, MultipartFile> fileMap = files != null
					? files.stream().collect(Collectors.toMap(MultipartFile::getOriginalFilename, f -> f, (a, b) -> a))
					: Collections.emptyMap();

			List<DtoPaymentDocument> savedDocuments = new ArrayList<>();

			/*
			 * ========================= DOCUMENT LOOP (DTO DRIVEN)
			 * =========================
			 */
			if (dtoPayment.getDocuments() != null) {

				for (DtoPaymentDocument docDto : dtoPayment.getDocuments()) {

					if (docDto == null || docDto.getTxtOriginalFileName() == null) {
						continue;
					}

					MultipartFile file = fileMap.get(docDto.getTxtOriginalFileName());
					if (file == null) {
						continue;
					}

					String uploadPath = UtilFileStorage.saveFile(file, "event-payments");

					EventPaymentDocument document = new EventPaymentDocument();
					document.setEventPayment(payment);
					document.setTxtOriginalFileName(file.getOriginalFilename());
					document.setTxtFileName(docDto.getTxtFileName());
					document.setTxtFilePath(uploadPath);
					document.setTxtContentType(file.getContentType());
					document.setNumFileSize(file.getSize());
					document.setCreatedBy(ServiceCurrentUser.getCurrentUserId());

					repositoryEventPaymentDocument.save(document);

					DtoPaymentDocument savedDoc = new DtoPaymentDocument();
					savedDoc.setSerEventPaymentDocumentId(document.getSerEventPaymentDocumentId());
					savedDoc.setSerEventPaymentId(payment.getSerEventPaymentId());
					savedDoc.setTxtFileName(document.getTxtFileName());
					savedDoc.setTxtOriginalFileName(document.getTxtOriginalFileName());
					savedDoc.setTxtFilePath(document.getTxtFilePath());
					savedDoc.setTxtContentType(document.getTxtContentType());
					savedDoc.setNumFileSize(document.getNumFileSize());

					savedDocuments.add(savedDoc);
				}
			}

			savedDto.setDocuments(savedDocuments);

			baseResult.setTxtMessage("Success");
			baseResult.setResult(savedDto);
			return baseResult;

		} catch (Exception e) {
			LOGGER.debug(e.getMessage(), e);
			baseResult.setTxtMessage(e.getMessage());
			return baseResult;
		}
	}

	@Override
	public List<DtoPayment> getPaymentsBySerDeliveryBookingId(Integer serDeliveryBookingId) {

		List<EventPayment> list = repositoryEventPayment
				.findBySerDeliveryBookingIdAndBlnIsDeletedFalseOrderByDtePaymentDateDesc(serDeliveryBookingId);

		List<DtoPayment> dtos = new ArrayList<>();
		for (EventPayment p : list) {
			dtos.add(MapperEventPayment.toDto(p));
		}
		return dtos;
	}

	@Override
	@Transactional
	public DtoResult deletePayment(Integer serEventPaymentId) {

		DtoResult res = new DtoResult();

		EventPayment payment = repositoryEventPayment.findById(serEventPaymentId)
				.orElseThrow(() -> new RuntimeException("Payment not found"));

		payment.setBlnIsDeleted(true);
		payment.setUpdatedBy(ServiceCurrentUser.getCurrentUserId());
		repositoryEventPayment.save(payment);

		/*
		 * =============================== RECALCULATE AFTER DELETE
		 * ===============================
		 */
		serviceEventBudget.recalculateBudget(payment.getEventBudget().getSerEventBudgetId());

		res.setTxtMessage("Deleted successfully");
		return res;
	}
}
