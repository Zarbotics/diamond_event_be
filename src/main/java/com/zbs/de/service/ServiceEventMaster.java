package com.zbs.de.service;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import com.zbs.de.model.EventMaster;
import com.zbs.de.model.dto.DtoEventMaster;
import com.zbs.de.model.dto.DtoEventMasterAdminPortal;
import com.zbs.de.model.dto.DtoEventMasterSearch;
import com.zbs.de.model.dto.DtoEventMasterStats;
import com.zbs.de.model.dto.DtoEventMasterTableView;
import com.zbs.de.model.dto.DtoResult;
import com.zbs.de.model.dto.DtoSearch;

public interface ServiceEventMaster {

	DtoResult saveAndUpdate(DtoEventMaster dtoEventMaster);

	DtoResult getByEventTypeIdAndCustId(DtoSearch dtoSearch);

	DtoResult getByCustId(DtoSearch dtoSearch);

	String generateNextEventMasterCode();

	DtoResult getAllEvents();

	List<DtoEventMasterStats> getEventTypeStats();

	DtoResult saveAndUpdateWithDocs(DtoEventMaster dtoEventMaster, List<MultipartFile> files) throws IOException;

	DtoResult deleteById(Integer id);

	DtoResult getAllEventsTableView();

	DtoResult saveAndUpdateWithDocsAdminPortal(DtoEventMasterAdminPortal dtoEventMasterAdminPortal,
			List<MultipartFile> files) throws IOException;

	DtoResult getAllEventsAdminPortal();
	
	Page<DtoEventMasterTableView> search(DtoEventMasterSearch dto);
	
	Page<EventMaster> searchEntity(DtoEventMasterSearch dto);
	
	Page<DtoEventMasterTableView> searchByBudgetStatus(String status, int page, int size);
	
	Page<DtoEventMasterAdminPortal> searchInEntityAndEventBudget(DtoEventMasterSearch dto);
	
	EventMaster getEventMasterById(Integer serEventMasterId);
	
	DtoResult validateEventDateAvailability(Date eventDate);
	
	DtoResult getAlreadyBookedDates();

	/**
	 * Upcoming days holding more events than the capacity rule allows.
	 *
	 * <p>
	 * These exist because the rule was not applied on every save path until
	 * recently, and the bookings that resulted are real commitments to real
	 * customers — they are grandfathered rather than corrected. What the team
	 * needs is not for them to disappear but to know which days they are, so
	 * those days can be staffed and resourced for what is actually happening.
	 *
	 * <p>
	 * Past days are left out. They are history, and nothing can be done about
	 * them.
	 */
	DtoResult getDaysOverCapacity();

	/**
	 * Every event, in the narrow shape the admin calendar draws.
	 *
	 * <p>
	 * Deliberately not paginated — a month view missing some of its events is
	 * worse than no month view. The saving is in the width of each row: five
	 * fields instead of sixty, and none of the nested selection collections.
	 */
	DtoResult getCalendarEntries();


	DtoResult saveAndUpdateWithDocsCE(DtoEventMaster dtoEventMaster, List<MultipartFile> files)throws IOException;
	
	DtoEventMaster getEventById(Integer serEventMasterId);
}
