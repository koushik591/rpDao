package com.chrysler.rp.dao;

import java.util.List;

public class PageResult <X> {
	private List<X> results;
	private int totalCount;
	
	public PageResult(List<X> results, int totalCount) {
		this.results = results;
		this.totalCount = totalCount;
	}
	
	public List<X> getResults() {
		return results;
	}
	public void setResults(List<X> results) {
		this.results = results;
	}
	public int getTotalCount() {
		return totalCount;
	}
	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}
}
