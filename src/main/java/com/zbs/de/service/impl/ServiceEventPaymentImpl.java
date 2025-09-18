package com.zbs.de.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
import com.zbs.de.service.ServiceEventPayment;
import com.zbs.de.util.UtilDateAndTime;
import com.zbs.de.util.UtilFileStorage;

@Service
public class ServiceEventPaymentImpl implements ServiceEventPayment {

	@Autowired
	private RepositoryEventPaymentMaster repositoryEventPayment;

	@Autowired
	private RepositoryEventPaymentDocument repositoryEventPaymentDocument;

	@Autowired
	private RepositoryEventBudget repositoryEventBudget;

	@Override
	@Transactional
	public DtoResult savePayment(DtoPayment dtoPayment) {
		DtoResult result = new DtoResult();
		if (dtoPayment.getSerEventBudgetId() == null) {
			result.setTxtMessage("Budget ID is required.");
			return result;
		}

		Optional<EventBudget> optBudget = repositoryEventBudget.findById(dtoPayment.getSerEventBudgetId());
		if (optBudget.isEmpty()) {
			result.setTxtMessage("Invalid Budget ID.");
			return result;
		}
		EventBudget budget = optBudget.get();

		EventPayment payment;
		if (dtoPayment.getSerEventPaymentId() != null) {
			payment = repositoryEventPayment.findById(dtoPayment.getSerEventPaymentId()).orElse(new EventPayment());
		} else {
			payment = new EventPayment();
		}

		// map fields
		payment.setEventBudget(budget);
		payment.setSerEventMasterId(dtoPayment.getSerEventMasterId() != null ? dtoPayment.getSerEventMasterId()
				: (budget.getEventMaster() != null ? budget.getEventMaster().getSerEventMasterId() : null));
		payment.setNumAmount(dtoPayment.getNumAmount());
		payment.setTxtPaymentMode(dtoPayment.getTxtPaymentMode());
		payment.setTxtTransactionRef(dtoPayment.getTxtTransactionRef());
		payment.setDtePaymentDate(UtilDateAndTime.ddmmyyyyStringToDate(dtoPayment.getDtePaymentDate()));
		payment.setTxtPaymentStatus(dtoPayment.getTxtPaymentStatus());
		payment.setTxtRemarks(dtoPayment.getTxtRemarks());

		if (payment.getSerEventPaymentId() == null) {
			payment.setCreatedBy(ServiceCurrentUser.getCurrentUserId());
		} else {
			payment.setUpdatedBy(ServiceCurrentUser.getCurrentUserId());
		}

		repositoryEventPayment.save(payment);

		// update budget paid amount & status
		BigDecimal totalPaid = repositoryEventPayment.sumPaidByBudgetId(budget.getSerEventBudgetId());
		budget.setNumPaidAmount(totalPaid == null ? BigDecimal.ZERO : totalPaid);
		// derive status
		if (budget.getNumTotalBudget() != null) {
			if (budget.getNumTotalBudget().compareTo(BigDecimal.ZERO) == 0) {
				budget.setTxtPaymentStatus("UNDEFINED");
			} else if (totalPaid != null && totalPaid.compareTo(budget.getNumTotalBudget()) >= 0) {
				budget.setTxtPaymentStatus("PAID");
			} else if (totalPaid != null && totalPaid.compareTo(BigDecimal.ZERO) > 0) {
				budget.setTxtPaymentStatus("PARTIAL");
			} else {
				budget.setTxtPaymentStatus("UNPAID");
			}
		}
		budget.setUpdatedBy(ServiceCurrentUser.getCurrentUserId());
		repositoryEventBudget.save(budget);

		result.setTxtMessage("Success");
		result.setResult(MapperEventPayment.toDto(payment));
		return result;
	}

	@Override
	@Transactional
	public DtoResult savePaymentWithFiles(DtoPayment dtoPayment, List<MultipartFile> files) {
		// First, save payment (this also updates budget totals)
		DtoResult baseResult = savePayment(dtoPayment);
		if (!"Success".equalsIgnoreCase(baseResult.getTxtMessage())) {
			return baseResult;
		}

		// Payment DTO returned by savePayment
		DtoPayment savedDto = (DtoPayment) baseResult.getResult();
		if (savedDto == null || savedDto.getSerEventPaymentId() == null) {
			DtoResult err = new DtoResult();
			err.setTxtMessage("Payment save failed or returned null id.");
			return err;
		}

		// Retrieve persisted payment entity
		Integer paymentId = savedDto.getSerEventPaymentId();
		Optional<EventPayment> optPayment = repositoryEventPayment.findById(paymentId);
		if (optPayment.isEmpty()) {
			DtoResult err = new DtoResult();
			err.setTxtMessage("Unable to find payment after saving.");
			return err;
		}
		EventPayment payment = optPayment.get();

		List<DtoPaymentDocument> savedDocs = new ArrayList<>();
		for (MultipartFile f : files) {
			if (f == null || f.isEmpty())
				continue;
			try {
				// Use your existing UtilFileStorage to save file.
				// category will be "event-payments/{paymentId}"
				String category = "event-payments/" + payment.getSerEventPaymentId();
				String publicUrl = UtilFileStorage.saveFile(f, category); // returns URL as you implemented

				EventPaymentDocument doc = new EventPaymentDocument();
				doc.setEventPayment(payment);
				doc.setTxtFileName(f.getOriginalFilename());
				doc.setTxtFilePath(publicUrl);
				doc.setTxtContentType(f.getContentType());
				doc.setNumFileSize(f.getSize());
				doc.setCreatedBy(ServiceCurrentUser.getCurrentUserId());

				repositoryEventPaymentDocument.save(doc);

				// convert to DTO for response
				DtoPaymentDocument pdto = new DtoPaymentDocument();
				pdto.setSerEventPaymentDocumentId(doc.getSerEventPaymentDocumentId());
				pdto.setSerEventPaymentId(payment.getSerEventPaymentId());
				pdto.setTxtFileName(doc.getTxtFileName());
				pdto.setTxtFilePath(doc.getTxtFilePath());
				pdto.setTxtContentType(doc.getTxtContentType());
				pdto.setNumFileSize(doc.getNumFileSize());
				savedDocs.add(pdto);

			} catch (Exception ex) {
				// log error and continue with next file
				// If you want the whole operation to rollback when any file save fails,
				// rethrow the exception here instead of continuing.
				ex.printStackTrace();
			}
		}

		// Update DTO result to include documents
		savedDto.setDocuments(savedDocs);
		DtoResult finalResult = new DtoResult();
		finalResult.setTxtMessage("Success");
		finalResult.setResult(savedDto);
		return finalResult;
	}

	@Override
	public List<DtoPayment> getPaymentsByBudgetId(Integer serEventBudgetId) {
		List<EventPayment> list = repositoryEventPayment
				.findByEventBudget_SerEventBudgetIdAndBlnIsDeleted(serEventBudgetId, false);
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
		Optional<EventPayment> opt = repositoryEventPayment.findById(serEventPaymentId);
		if (opt.isEmpty()) {
			res.setTxtMessage("No payment found");
			return res;
		}
		EventPayment payment = opt.get();
		payment.setBlnIsDeleted(true);
		payment.setUpdatedBy(ServiceCurrentUser.getCurrentUserId());
		repositoryEventPayment.save(payment);

		// update budget totals
		EventBudget budget = payment.getEventBudget();
		BigDecimal totalPaid = repositoryEventPayment.sumPaidByBudgetId(budget.getSerEventBudgetId());
		budget.setNumPaidAmount(totalPaid == null ? BigDecimal.ZERO : totalPaid);
		if (budget.getNumTotalBudget() != null) {
			if (totalPaid != null && totalPaid.compareTo(budget.getNumTotalBudget()) >= 0) {
				budget.setTxtPaymentStatus("PAID");
			} else if (totalPaid != null && totalPaid.compareTo(BigDecimal.ZERO) > 0) {
				budget.setTxtPaymentStatus("PARTIAL");
			} else {
				budget.setTxtPaymentStatus("UNPAID");
			}
		}
		repositoryEventBudget.save(budget);

		res.setTxtMessage("Deleted (soft) successfully");
		return res;
	}

	@Override
	public BigDecimal getTotalPaidByBudget(Integer serEventBudgetId) {
		return repositoryEventPayment.sumPaidByBudgetId(serEventBudgetId);
	}
}
