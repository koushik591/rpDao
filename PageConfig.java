package com.chrysler.rp.dao;

public class PageConfig {
	private String searchField;
	private String searchValue;
	private String sortField;
	private boolean isAscending;
	private int offset;
	private int limit;
	boolean calculateTotal;
	private int totalRecords;

	public PageConfig () {
		
	}
	
	public PageConfig(String searchField, String searchValue, String sortField, boolean isAscending, int offset, int limit,
			boolean calculateTotal, int totalRecords) {
		this.searchField = searchField;
		this.searchValue = searchValue;
		this.sortField = sortField;
		this.isAscending = isAscending;
		this.offset = offset;
		this.limit = limit;
		this.calculateTotal = calculateTotal;
		this.totalRecords = totalRecords;
	}

	public String getSearchField() {
		return searchField;
	}

	public void setSearchField(String searchField) {
		this.searchField = searchField;
	}

	public String getSearchValue() {
		return searchValue;
	}

	public void setSearchValue(String searchValue) {
		this.searchValue = searchValue;
	}

	public String getSortField() {
		return sortField;
	}

	public void setSortField(String sortField) {
		this.sortField = sortField;
	}

	public boolean isAscending() {
		return isAscending;
	}

	public void setAscending(boolean isAscending) {
		this.isAscending = isAscending;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public boolean isCalculateTotal() {
		return calculateTotal;
	}

	public void setCalculateTotal(boolean calculateTotal) {
		this.calculateTotal = calculateTotal;
	}

	public int getTotalRecords() {
		return totalRecords;
	}

	public void setTotalRecords(int totalRecords) {
		this.totalRecords = totalRecords;
	}
	
}
