package com.zbs.de.model.dto;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * Description: The DTO class for Search Operations Name of Project: Ims
 * Version: 0.0.1
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DtoSearch {

	/** The search keyword. */
	private String searchKeyword;

	/** The page number. */
	private Integer pageNumber;

	/** The page size. */
	private Integer pageSize;

	/** The sort on. */
	private String sortOn;

	/** The sort by. */
	private String sortBy;

	/** The id. */
	private Integer id;

	private Integer id1;

	private Integer id2;

	/** The Long Id. */
	private Long idL;

	private Long idL1;

	private Long idL2;

	private String txtQuery;
	private String txtRole;
	private String txtType;
	private Integer numlimit;

	/** The ids. */
	private List<String> ids;

	/** The ids. */
	private List<String> categories;
	/** The total count. */
	private Integer totalCount;

	/** The date. */
	private Date date;

	private List<String> messages;

	private List<Integer> listIds;
	private List<Long> listIdLs;

	private String fileHeaderName;

	private List<Integer> userIds;
	private List<Integer> itemIds;

	private boolean inActiveItems;
	private boolean blnFlag;

	private String fromDate;
	private String toDate;
	private String isComeFrom;
	private List<Short> types;

	private byte[] attachment;

	public String getSearchKeyword() {
		return searchKeyword;
	}

	public void setSearchKeyword(String searchKeyword) {
		this.searchKeyword = searchKeyword;
	}

	public Integer getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(Integer pageNumber) {
		this.pageNumber = pageNumber;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	public String getSortOn() {
		return sortOn;
	}

	public void setSortOn(String sortOn) {
		this.sortOn = sortOn;
	}

	public String getSortBy() {
		return sortBy;
	}

	public void setSortBy(String sortBy) {
		this.sortBy = sortBy;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public List<String> getIds() {
		return ids;
	}

	public void setIds(List<String> ids) {
		this.ids = ids;
	}

	public List<String> getCategories() {
		return categories;
	}

	public void setCategories(List<String> categories) {
		this.categories = categories;
	}

	public Integer getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(Integer totalCount) {
		this.totalCount = totalCount;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public List<String> getMessages() {
		return messages;
	}

	public void setMessages(List<String> messages) {
		this.messages = messages;
	}

	public List<Integer> getListIds() {
		return listIds;
	}

	public void setListIds(List<Integer> listIds) {
		this.listIds = listIds;
	}

	public String getFileHeaderName() {
		return fileHeaderName;
	}

	public void setFileHeaderName(String fileHeaderName) {
		this.fileHeaderName = fileHeaderName;
	}

	public List<Integer> getUserIds() {
		return userIds;
	}

	public void setUserIds(List<Integer> userIds) {
		this.userIds = userIds;
	}

	public List<Integer> getItemIds() {
		return itemIds;
	}

	public void setItemIds(List<Integer> itemIds) {
		this.itemIds = itemIds;
	}

	public boolean isInActiveItems() {
		return inActiveItems;
	}

	public void setInActiveItems(boolean inActiveItems) {
		this.inActiveItems = inActiveItems;
	}

	public String getFromDate() {
		return fromDate;
	}

	public void setFromDate(String fromDate) {
		this.fromDate = fromDate;
	}

	public String getToDate() {
		return toDate;
	}

	public void setToDate(String toDate) {
		this.toDate = toDate;
	}

	public String getIsComeFrom() {
		return isComeFrom;
	}

	public void setIsComeFrom(String isComeFrom) {
		this.isComeFrom = isComeFrom;
	}

	public List<Short> getTypes() {
		return types;
	}

	public void setTypes(List<Short> types) {
		this.types = types;
	}

	public byte[] getAttachment() {
		return attachment;
	}

	public void setAttachment(byte[] attachment) {
		this.attachment = attachment;
	}

	public Integer getId1() {
		return id1;
	}

	public void setId1(Integer id1) {
		this.id1 = id1;
	}

	public Integer getId2() {
		return id2;
	}

	public void setId2(Integer id2) {
		this.id2 = id2;
	}

	public Long getIdL() {
		return idL;
	}

	public void setIdL(Long idL) {
		this.idL = idL;
	}

	public List<Long> getListIdLs() {
		return listIdLs;
	}

	public void setListIdLs(List<Long> listIdLs) {
		this.listIdLs = listIdLs;
	}

	public Long getIdL1() {
		return idL1;
	}

	public void setIdL1(Long idL1) {
		this.idL1 = idL1;
	}

	public Long getIdL2() {
		return idL2;
	}

	public void setIdL2(Long idL2) {
		this.idL2 = idL2;
	}

	public boolean isBlnFlag() {
		return blnFlag;
	}

	public void setBlnFlag(boolean blnFlag) {
		this.blnFlag = blnFlag;
	}

	public String getTxtRole() {
		return txtRole;
	}

	public void setTxtRole(String txtRole) {
		this.txtRole = txtRole;
	}

	public String getTxtType() {
		return txtType;
	}

	public void setTxtType(String txtType) {
		this.txtType = txtType;
	}

	public Integer getNumlimit() {
		return numlimit;
	}

	public void setNumlimit(Integer numlimit) {
		this.numlimit = numlimit;
	}

	public String getTxtQuery() {
		return txtQuery;
	}

	public void setTxtQuery(String txtQuery) {
		this.txtQuery = txtQuery;
	}

}
