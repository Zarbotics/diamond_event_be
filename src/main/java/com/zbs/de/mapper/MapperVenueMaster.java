package com.zbs.de.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.zbs.de.model.VenueMaster;
import com.zbs.de.model.VenueMasterDetail;
import com.zbs.de.model.VenueMasterDetailDocument;
import com.zbs.de.model.dto.DtoVenueMasterDetailDocument;
import com.zbs.de.model.dto.DtoVenueMaster;
import com.zbs.de.model.dto.DtoVenueMasterDetail;

public class MapperVenueMaster {

	public static DtoVenueMaster toDto(VenueMaster entity) {
		DtoVenueMaster dto = new DtoVenueMaster();
		dto.setSerVenueMasterId(entity.getSerVenueMasterId());
		dto.setTxtVenueCode(entity.getTxtVenueCode());
		dto.setTxtVenueName(entity.getTxtVenueName());
		dto.setTxtAddress(entity.getTxtAddress());
		if (entity.getCityMaster() != null) {
			dto.setSerCityId(entity.getCityMaster().getSerCityId());
			dto.setTxtCityName(entity.getCityMaster().getTxtCityName());
			dto.setTxtCityCode(entity.getCityMaster().getTxtCityCode());
		}
		dto.setBlnIsActive(entity.getBlnIsActive());

		List<DtoVenueMasterDetail> hallDtos = new ArrayList<>();
		if (entity.getVenueMasterDetails() != null) {
			for (VenueMasterDetail detail : entity.getVenueMasterDetails()) {
				DtoVenueMasterDetail hallDto = new DtoVenueMasterDetail();
				hallDto.setSerVenueMasterDetailId(detail.getSerVenueMasterDetailId());
				hallDto.setTxtHallCode(detail.getTxtHallCode());
				hallDto.setTxtHallName(detail.getTxtHallName());
				hallDto.setNumCapacity(detail.getNumCapacity());
				hallDto.setTxtCapacity(detail.getTxtCapacity());
				hallDto.setNumPrice(detail.getNumPrice());

				if (detail.getVenueMasterDetailDocument() != null) {
					hallDto.setDocuments(detail.getVenueMasterDetailDocument().stream().map(doc -> {
						DtoVenueMasterDetailDocument d = new DtoVenueMasterDetailDocument();
						d.setDocumentId(doc.getDocumentId());
						d.setDocumentName(doc.getDocumentName());
						d.setDocumentType(doc.getDocumentType());
						d.setOriginalName(doc.getOriginalName());
						d.setSize(doc.getSize());
//						d.setDocumentFile(doc.getDocumentFile());
						return d;
					}).collect(Collectors.toList()));
				}

				hallDtos.add(hallDto);
			}
		}

		dto.setVenueMasterDetails(hallDtos);
		return dto;
	}

	public static VenueMaster toEntity(DtoVenueMaster dto) {
		VenueMaster venue = toVenueMaster(dto);

		if (dto.getVenueMasterDetails() != null) {
			List<VenueMasterDetail> venueHallList = dto.getVenueMasterDetails().stream()
					.map(hallDto -> toVenuMasterDetail(hallDto)).collect(Collectors.toList());
			venue.setVenueMasterDetails(venueHallList);
		}

		return venue;
	}

	public static VenueMaster toVenueMaster(DtoVenueMaster dto) {
		VenueMaster venue = new VenueMaster();
		venue.setSerVenueMasterId(dto.getSerVenueMasterId());
		venue.setTxtVenueCode(dto.getTxtVenueCode());
		venue.setTxtVenueName(dto.getTxtVenueName());
		venue.setTxtAddress(dto.getTxtAddress());
		venue.setBlnIsActive(dto.getBlnIsActive());
		return venue;
	}

	public static VenueMasterDetail toVenuMasterDetail(DtoVenueMasterDetail dto) {
		VenueMasterDetail hall = new VenueMasterDetail();
		hall.setSerVenueMasterDetailId(dto.getSerVenueMasterDetailId());
		hall.setTxtHallCode(dto.getTxtHallCode());
		hall.setTxtHallName(dto.getTxtHallName());
		hall.setNumCapacity(dto.getNumCapacity());
		hall.setTxtCapacity(dto.getTxtCapacity());
		hall.setBlnIsActive(dto.getBlnIsActive());
		hall.setNumPrice(dto.getNumPrice());

		if (dto.getDocuments() != null) {
			List<VenueMasterDetailDocument> documents = dto.getDocuments().stream()
					.map(d -> toVenuMasterDetailDocument(d)).collect(Collectors.toList());
			hall.setVenueMasterDetailDocument(documents);
		}

		return hall;
	}

	public static VenueMasterDetailDocument toVenuMasterDetailDocument(DtoVenueMasterDetailDocument dto) {
		VenueMasterDetailDocument doc = new VenueMasterDetailDocument();
		doc.setDocumentId(dto.getDocumentId());
		doc.setDocumentName(dto.getDocumentName());
		doc.setDocumentType(dto.getDocumentType());
		doc.setOriginalName(dto.getOriginalName());
		doc.setSize(dto.getSize());
//		doc.setDocumentFile(dto.getDocumentFile());
		return doc;
	}

}
