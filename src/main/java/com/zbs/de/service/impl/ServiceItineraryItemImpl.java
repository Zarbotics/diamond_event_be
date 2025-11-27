package com.zbs.de.service.impl;

import com.zbs.de.mapper.MapperItinerary;
import com.zbs.de.model.ItineraryItem;
import com.zbs.de.model.dto.DtoItineraryItem;
import com.zbs.de.repository.RepositoryItineraryItem;
import com.zbs.de.service.ServiceItineraryItem;
import com.zbs.de.util.exception.NotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service("serviceItineraryItemImpl")
public class ServiceItineraryItemImpl implements ServiceItineraryItem {

	@Autowired
	private RepositoryItineraryItem repo;

	@Override
	public DtoItineraryItem create(DtoItineraryItem dto) {
		ItineraryItem it = MapperItinerary.toEntity(dto);
		repo.save(it);
		return MapperItinerary.toDto(it);
	}

	@Override
	public DtoItineraryItem update(Long id, DtoItineraryItem dto) {
		ItineraryItem exist = repo.findById(id).orElseThrow(() -> new NotFoundException("Itinerary item not found"));
		exist.setTxtCode(dto.getTxtCode());
		exist.setTxtName(dto.getTxtName());
		exist.setTxtItemType(dto.getTxtItemType());
		exist.setMetadata(dto.getMetadata());
		repo.save(exist);
		return MapperItinerary.toDto(exist);
	}

	@Override
	public DtoItineraryItem getById(Long id) {
		return repo.findById(id).map(MapperItinerary::toDto)
				.orElseThrow(() -> new NotFoundException("Itinerary item not found"));
	}

	@Override
	public List<DtoItineraryItem> getAll() {
		return repo.findAll().stream().map(MapperItinerary::toDto).collect(Collectors.toList());
	}

	@Override
	public void delete(Long id) {
		ItineraryItem exist = repo.findById(id).orElseThrow(() -> new NotFoundException("Itinerary item not found"));
		exist.setBlnIsDeleted(true);
		repo.save(exist);
	}
}
