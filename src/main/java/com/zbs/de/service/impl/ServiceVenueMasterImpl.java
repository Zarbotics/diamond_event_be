package com.zbs.de.service.impl;

import com.zbs.de.model.CityMaster;
import com.zbs.de.model.VenueMaster;
import com.zbs.de.model.VenueMasterDetail;
import com.zbs.de.model.VenueMasterDetailDocument;
import com.zbs.de.mapper.MapperVenueMaster;
import com.zbs.de.model.dto.*;
import com.zbs.de.repository.RepositoryVenueMaster;
import com.zbs.de.service.ServiceCityMaster;
import com.zbs.de.service.ServiceVenueMaster;
import com.zbs.de.util.ResponseMessage;
import com.zbs.de.util.UtilFileStorage;
import com.zbs.de.util.UtilRandomKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service("serviceVenueMaster")
public class ServiceVenueMasterImpl implements ServiceVenueMaster {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCustomerMasterImpl.class);

	@Autowired
	private RepositoryVenueMaster repositoryVenueMaster;

	@Autowired
	private ServiceCityMaster serviceCityMaster;

	@Override
	public ResponseMessage saveOrUpdate(DtoVenueMaster dto) {
		ResponseMessage res = new ResponseMessage();
		try {

			if (UtilRandomKey.isNull(dto.getSerCityId())) {
				res.setMessage("Please Select City To Save A New Venue");
				return res;
			}
			CityMaster cityMaster = serviceCityMaster.getByPK(dto.getSerCityId());
			if (cityMaster == null) {
				return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST,
						"Invalid City Selected", null);
			}

			// ******** Setting Venue Master *********
			VenueMaster venue = MapperVenueMaster.toVenueMaster(dto);
			venue.setCityMaster(cityMaster);
			venue.setBlnIsActive(true);
			venue.setBlnIsDeleted(false);
			venue.setBlnIsApproved(true);
			venue.setCreatedDate(new Date());

			// ******* Setting Venue Master Details *******
			List<VenueMasterDetail> venueMasterDetails = new ArrayList<>();
			for (DtoVenueMasterDetail detailDto : dto.getVenueMasterDetails()) {
				VenueMasterDetail detail = MapperVenueMaster.toVenuMasterDetail(detailDto);
				detail.setVenueMaster(venue);

				if (detail.getVenueMasterDetailDocument() != null) {
					for (VenueMasterDetailDocument doc : detail.getVenueMasterDetailDocument()) {
						doc.setVenueMasterDetail(detail);
					}
				}

				venueMasterDetails.add(detail);
			}
			venue.setVenueMasterDetails(venueMasterDetails);

			VenueMaster saved = repositoryVenueMaster.saveAndFlush(venue);

			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Venue saved successfully",
					MapperVenueMaster.toDto(saved));
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Error saving venue: " + e.getMessage(), null);
		}
	}

	@Override
	public ResponseMessage getAllVenues() {
		try {
			List<VenueMaster> list = repositoryVenueMaster.findByBlnIsDeletedFalse();
			List<DtoVenueMaster> dtos = list.stream().map(MapperVenueMaster::toDto).collect(Collectors.toList());
			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Fetched all venues", dtos);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Error fetching venues: " + e.getMessage(), null);
		}
	}

	@Override
	public ResponseMessage getAllVenuesGroupedByCity() {
		try {
			List<VenueMaster> list = repositoryVenueMaster.findByBlnIsDeletedFalse();
			Map<CityMaster, List<VenueMaster>> grouped = list.stream()
					.collect(Collectors.groupingBy(VenueMaster::getCityMaster));

			List<DtoVenueMasterByCity> result = new ArrayList<>();
			for (Map.Entry<CityMaster, List<VenueMaster>> entry : grouped.entrySet()) {
				CityMaster city = entry.getKey();
				List<DtoVenueMaster> venues = entry.getValue().stream().map(MapperVenueMaster::toDto)
						.collect(Collectors.toList());

				DtoVenueMasterByCity dto = new DtoVenueMasterByCity();
				dto.setSerCityId(city.getSerCityId());
				dto.setTxtCityName(city.getTxtCityName());
				dto.setTxtCityCode(city.getTxtCityCode());
				dto.setVenueMasters(venues);
				result.add(dto);
			}

			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Fetched venues grouped by city", result);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Error grouping venues by city: " + e.getMessage(), null);
		}
	}

	@Override
	public ResponseMessage getVenuesByCityId(Integer cityId) {
		try {
			CityMaster cityMaster = serviceCityMaster.getByPK(cityId);
			if (UtilRandomKey.isNull(cityMaster)) {
				return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST,
						"Invalid city selected", null);
			}

			List<VenueMaster> venues = repositoryVenueMaster.findByCityMasterAndBlnIsDeletedFalse(cityMaster);
			List<DtoVenueMaster> dtos = venues.stream().map(MapperVenueMaster::toDto).collect(Collectors.toList());

			return new ResponseMessage(HttpStatus.OK.value(), HttpStatus.OK, "Fetched venues for the city", dtos);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR,
					"Error fetching venues by city: " + e.getMessage(), null);
		}
	}

	public VenueMaster saveVenueWithDetails(DtoVenueMaster dto, List<MultipartFile> files) throws IOException {
		VenueMaster venue = new VenueMaster();
		venue.setTxtVenueName(dto.getTxtVenueName());
		venue.setTxtVenueCode(dto.getTxtVenueCode());
		venue.setTxtAddress(dto.getTxtAddress());
		venue.setCityMaster(serviceCityMaster.getByPK(dto.getSerCityId()));

		List<VenueMasterDetail> details = new ArrayList<>();
		Map<String, MultipartFile> fileMap = files.stream()
				.collect(Collectors.toMap(MultipartFile::getOriginalFilename, f -> f));

		for (DtoVenueMasterDetail detailDto : dto.getVenueMasterDetails()) {
			VenueMasterDetail detail = new VenueMasterDetail();
			detail.setTxtHallName(detailDto.getTxtHallName());
			detail.setTxtHallCode(detailDto.getTxtHallCode());
			detail.setNumCapacity(detailDto.getNumCapacity());
			detail.setNumPrice(detailDto.getNumPrice());
			detail.setVenueMaster(venue);

			List<VenueMasterDetailDocument> documents = new ArrayList<>();
			for (DtoVenueMasterDetailDocument docDto : detailDto.getDocuments()) {
				MultipartFile file = fileMap.get(docDto.getOriginalName());
				if (file != null) {
					String uploadPath = UtilFileStorage.saveFile(file, "venues");
					VenueMasterDetailDocument doc = new VenueMasterDetailDocument();
					doc.setDocumentName(file.getName());
					doc.setOriginalName(file.getOriginalFilename());
					doc.setDocumentType(file.getContentType());
					doc.setSize(String.valueOf(file.getSize()));
					doc.setFilePath(uploadPath);
					doc.setVenueMasterDetail(detail);
					documents.add(doc);
				}
			}
			detail.setVenueMasterDetailDocument(documents);
			details.add(detail);
		}

		venue.setVenueMasterDetails(details);
		venue = repositoryVenueMaster.save(venue);

		return venue;
	}

	@Override
	public VenueMaster getByPK(Integer serVenueMasterId) {
		try {
			Optional<VenueMaster> entity = repositoryVenueMaster.findById(serVenueMasterId);
			if (entity.isPresent()) {
				return entity.get();
			} else {
				return null;
			}
		} catch (Exception e) {
			LOGGER.error("Error Fetching Venue Master by ID", e);
			return null;
		}
	}

}
