package com.zbs.de.mapper;

import java.util.stream.Collectors;
import com.zbs.de.model.EventPayment;
import com.zbs.de.model.EventPaymentDocument;
import com.zbs.de.model.dto.DtoPayment;
import com.zbs.de.model.dto.DtoPaymentDocument;
import com.zbs.de.util.UtilDateAndTime;

public class MapperEventPayment {

	public static DtoPayment toDto(EventPayment p) {
		if (p == null)
			return null;
		DtoPayment dto = new DtoPayment();
		dto.setSerEventPaymentId(p.getSerEventPaymentId());
		dto.setSerEventBudgetId(p.getEventBudget() != null ? p.getEventBudget().getSerEventBudgetId() : null);
		dto.setSerEventMasterId(p.getSerEventMasterId());
		dto.setSerDeliveryBookingId(p.getSerDeliveryBookingId());
		dto.setNumAmount(p.getNumAmount());
		dto.setTxtPaymentMode(p.getTxtPaymentMode());
		dto.setTxtTransactionRef(p.getTxtTransactionRef());
		dto.setDtePaymentDate(UtilDateAndTime.mmddyyyyDateToString(p.getDtePaymentDate()));
		dto.setTxtPaymentStatus(p.getTxtPaymentStatus());
		dto.setTxtRemarks(p.getTxtRemarks());
		if (p.getDocuments() != null) {
			dto.setDocuments(p.getDocuments().stream().map(MapperEventPayment::docToDto).collect(Collectors.toList()));
		}
		return dto;
	}

	private static DtoPaymentDocument docToDto(EventPaymentDocument d) {
		DtoPaymentDocument dto = new DtoPaymentDocument();
		dto.setSerEventPaymentDocumentId(d.getSerEventPaymentDocumentId());
		dto.setSerEventPaymentId(d.getEventPayment() != null ? d.getEventPayment().getSerEventPaymentId() : null);
		dto.setTxtFileName(d.getTxtFileName());
		dto.setTxtOriginalFileName(d.getTxtOriginalFileName());
		dto.setTxtFilePath(d.getTxtFilePath());
		dto.setTxtContentType(d.getTxtContentType());
		dto.setNumFileSize(d.getNumFileSize());
		return dto;
	}

	public static EventPayment toEntity(DtoPayment dto) {
		if (dto == null)
			return null;
		EventPayment p = new EventPayment();
		p.setSerEventPaymentId(dto.getSerEventPaymentId());
		p.setSerDeliveryBookingId(dto.getSerDeliveryBookingId());
		p.setNumAmount(dto.getNumAmount());
		p.setTxtPaymentMode(dto.getTxtPaymentMode());
		p.setTxtTransactionRef(dto.getTxtTransactionRef());
		p.setDtePaymentDate(UtilDateAndTime.ddmmyyyyStringToDate(dto.getDtePaymentDate()));
		p.setTxtPaymentStatus(dto.getTxtPaymentStatus());
		p.setTxtRemarks(dto.getTxtRemarks());
		// Documents are handled by service after file upload
		return p;
	}
}
